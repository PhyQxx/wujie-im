package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.FriendRequest;
import com.wujie.im.entity.User;
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

    @PostMapping("/request")
    public Result<Void> sendRequest(@RequestBody Map<String, Object> params) {
        try {
            friendService.sendRequest(
                    Long.valueOf(params.get("fromUserId").toString()),
                    Long.valueOf(params.get("toUserId").toString()),
                    (String) params.get("reason")
            );
            return Result.success("申请已发送", null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/requests/{userId}")
    public Result<List<FriendRequest>> getRequests(@PathVariable Long userId) {
        return Result.success(friendService.getRequests(userId));
    }

    @PutMapping("/request/{requestId}")
    public Result<Void> handleRequest(@PathVariable Long requestId, @RequestParam String action) {
        friendService.handleRequest(requestId, action);
        return Result.success();
    }

    @GetMapping("/list/{userId}")
    public Result<List<User>> getFriends(@PathVariable Long userId) {
        return Result.success(friendService.getFriends(userId));
    }

    @DeleteMapping("/{userId}/{friendId}")
    public Result<Void> deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }
}
