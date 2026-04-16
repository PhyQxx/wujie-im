package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.GroupInfo;
import com.wujie.im.entity.GroupMember;
import com.wujie.im.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
public class GroupController {
    @Autowired
    private GroupService groupService;

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
        return Result.success(groupService.getUserGroups(userId));
    }

    @GetMapping("/members/{groupId}")
    public Result<List<GroupMember>> getGroupMembers(@PathVariable Long groupId) {
        return Result.success(groupService.getGroupMembers(groupId));
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

    @PutMapping("/join-request/{requestId}")
    public Result<Void> handleJoinRequest(@PathVariable Long requestId,
                                          @RequestParam Long reviewerId,
                                          @RequestParam String action) {
        groupService.handleJoinRequest(requestId, reviewerId, action);
        return Result.success();
    }
}
