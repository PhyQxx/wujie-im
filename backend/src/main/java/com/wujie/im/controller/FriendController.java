package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.FriendRequest;
import com.wujie.im.entity.User;
import com.wujie.im.mapper.UserMapper;
import com.wujie.im.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
public class FriendController {
    @Autowired
    private FriendService friendService;
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/request")
    public Result<Void> sendRequest(@RequestBody Map<String, Object> params) {
        try {
            Long fromUserId = params.get("fromUserId") != null ? Long.valueOf(params.get("fromUserId").toString()) : null;
            Long toUserId = params.get("toUserId") != null ? Long.valueOf(params.get("toUserId").toString()) : null;
            if (fromUserId == null || toUserId == null) {
                return Result.error(400, "缺少必要参数 fromUserId 或 toUserId");
            }
            friendService.sendRequest(fromUserId, toUserId, (String) params.get("reason"));
            return Result.success("申请已发送", null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/requests/{userId}")
    public Result<List<FriendRequest>> getRequests(@PathVariable Long userId) {
        List<FriendRequest> requests = friendService.getRequests(userId);
        // 填充申请人用户信息
        for (FriendRequest req : requests) {
            User fromUser = userMapper.selectById(req.getFromUserId());
            if (fromUser != null) fromUser.setPassword(null);
            req.setFromUser(fromUser);
        }
        return Result.success(requests);
    }

    @PutMapping("/request/{requestId}")
    public Result<Void> handleRequest(@PathVariable Long requestId, @RequestParam String action) {
        friendService.handleRequest(requestId, action);
        return Result.success();
    }

    @GetMapping("/list/{userId}")
    public Result<List<Map<String, Object>>> getFriends(@PathVariable Long userId) {
        return Result.success(friendService.getFriends(userId));
    }

    @DeleteMapping("/{userId}/{friendId}")
    public Result<Void> deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }

    @PutMapping("/move")
    public Result<Void> moveFriendToGroup(@RequestBody Map<String, Object> params) {
        friendService.moveFriendToGroup(
                Long.valueOf(params.get("userId").toString()),
                Long.valueOf(params.get("friendId").toString()),
                Long.valueOf(params.get("groupId").toString())
        );
        return Result.success();
    }

    @PutMapping("/remark")
    public Result<Void> setFriendRemark(@RequestBody Map<String, Object> params) {
        friendService.setFriendRemark(
                Long.valueOf(params.get("userId").toString()),
                Long.valueOf(params.get("friendId").toString()),
                (String) params.get("remark")
        );
        return Result.success();
    }
}
