package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.GroupInfo;
import com.wujie.im.entity.GroupMember;
import com.wujie.im.entity.GroupJoinRequest;
import com.wujie.im.entity.User;
import com.wujie.im.mapper.UserMapper;
import com.wujie.im.service.GroupService;
import com.wujie.im.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/group")
public class GroupController {
    @Autowired
    private GroupService groupService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public Result<GroupInfo> createGroup(@RequestBody Map<String, Object> params) {
        return Result.success(groupService.createGroup(
                (String) params.get("name"),
                (String) params.get("avatar"),
                (String) params.get("type"),
                Long.valueOf(params.get("ownerId").toString())
        ));
    }

    @GetMapping("/list/{userId}")
    public Result<List<GroupInfo>> getUserGroups(@PathVariable Long userId) {
        List<GroupInfo> groups = groupService.getUserGroups(userId);
        for (GroupInfo g : groups) {
            User owner = userMapper.selectById(g.getOwnerId());
            if (owner != null) g.setOwnerName(owner.getUsername());
            g.setMemberCount(groupService.getGroupMemberIds(g.getId()).size());
        }
        return Result.success(groups);
    }

    @GetMapping("/{groupId}")
    public Result<GroupInfo> getGroup(@PathVariable Long groupId) {
        return Result.success(groupService.getGroupById(groupId));
    }

    @GetMapping("/members/{groupId}")
    public Result<List<Map<String, Object>>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMember> members = groupService.getGroupMembers(groupId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupMember m : members) {
            User user = userMapper.selectById(m.getUserId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", m.getId());
            item.put("userId", m.getUserId());
            item.put("role", m.getRole());
            item.put("muted", m.getMuted());
            item.put("mutedUntil", m.getMutedUntil());
            item.put("joinTime", m.getJoinTime());
            if (user != null) {
                user.setPassword(null);
                item.put("user", user);
            }
            result.add(item);
        }
        return Result.success(result);
    }

    @PostMapping("/join")
    public Result<Void> joinGroup(@RequestBody Map<String, Object> params) {
        groupService.joinGroup(
                Long.valueOf(params.get("groupId").toString()),
                Long.valueOf(params.get("userId").toString()),
                (String) params.get("reason")
        );
        return Result.success();
    }

    @GetMapping("/join-requests/{groupId}")
    public Result<List<GroupJoinRequest>> getJoinRequests(@PathVariable Long groupId) {
        return Result.success(groupService.getJoinRequests(groupId));
    }

    @GetMapping("/my-requests/{userId}")
    public Result<List<GroupJoinRequest>> getUserJoinRequests(@PathVariable Long userId) {
        return Result.success(groupService.getUserJoinRequests(userId));
    }

    @PutMapping("/join-request/{requestId}")
    public Result<Void> handleJoinRequest(@PathVariable Long requestId,
                                          @RequestParam Long reviewerId,
                                          @RequestParam String action) {
        groupService.handleJoinRequest(requestId, reviewerId, action);
        return Result.success();
    }

    @PutMapping("/update/{groupId}")
    public Result<Void> updateGroup(@PathVariable Long groupId,
                                    @RequestBody Map<String, Object> params,
                                    @RequestParam Long operatorId) {
        groupService.updateGroup(
                groupId,
                (String) params.get("name"),
                (String) params.get("avatar"),
                (String) params.get("announcement"),
                operatorId
        );
        return Result.success();
    }

    @PostMapping("/invite/{groupId}")
    public Result<Void> inviteMembers(@PathVariable Long groupId,
                                       @RequestBody Map<String, Object> params,
                                       @RequestParam Long inviterId) {
        @SuppressWarnings("unchecked")
        List<Number> userIds = (List<Number>) params.get("userIds");
        List<Long> ids = userIds.stream().map(Number::longValue).toList();
        groupService.inviteMembers(groupId, ids, inviterId);
        return Result.success();
    }

    @DeleteMapping("/member/{groupId}/{userId}")
    public Result<Void> removeMember(@PathVariable Long groupId,
                                      @PathVariable Long userId,
                                      @RequestParam Long operatorId) {
        groupService.removeMember(groupId, userId, operatorId);
        return Result.success();
    }

    @PostMapping("/leave/{groupId}")
    public Result<Void> leaveGroup(@PathVariable Long groupId, @RequestParam Long userId) {
        groupService.leaveGroup(groupId, userId);
        return Result.success();
    }

    @PutMapping("/mute/{groupId}/{targetUserId}")
    public Result<Void> muteMember(@PathVariable Long groupId,
                                    @PathVariable Long targetUserId,
                                    @RequestParam(required = false) Integer minutes,
                                    @RequestParam Long operatorId) {
        groupService.muteMember(groupId, targetUserId, minutes, operatorId);
        return Result.success();
    }

    @PutMapping("/unmute/{groupId}/{targetUserId}")
    public Result<Void> unmuteMember(@PathVariable Long groupId,
                                      @PathVariable Long targetUserId,
                                      @RequestParam Long operatorId) {
        groupService.unmuteMember(groupId, targetUserId, operatorId);
        return Result.success();
    }

    @PutMapping("/admin/{groupId}/{targetUserId}")
    public Result<Void> setAdmin(@PathVariable Long groupId,
                                  @PathVariable Long targetUserId,
                                  @RequestParam Long operatorId,
                                  @RequestParam boolean isAdmin) {
        groupService.setAdmin(groupId, targetUserId, operatorId, isAdmin);
        return Result.success();
    }

    @DeleteMapping("/{groupId}")
    public Result<Void> dissolveGroup(@PathVariable Long groupId, @RequestParam Long userId) {
        groupService.dissolveGroup(groupId, userId);
        return Result.success();
    }
}
