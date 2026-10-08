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
    public Result<Void> sendRequest(@RequestBody Map<String, Object> params,
                                    jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 身份一律取自 token（拦截器已鉴权），body 中的 fromUserId 仅作兼容忽略
            Long fromUserId = currentUserId(request);
            Long toUserId = params.get("toUserId") != null ? Long.valueOf(params.get("toUserId").toString()) : null;
            if (fromUserId == null || toUserId == null) {
                return Result.error(400, "缺少必要参数 toUserId");
            }

            friendService.sendRequest(fromUserId, toUserId, (String) params.get("reason"));
            return Result.success("申请已发送", null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/requests/{userId}")
    public Result<List<FriendRequest>> getRequests(@PathVariable Long userId,
                                                   jakarta.servlet.http.HttpServletRequest request) {
        // 路径参数仅保留兼容，实际身份以 token 为准（防越权查他人申请）
        userId = currentUserId(request);
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
    public Result<List<Map<String, Object>>> getFriends(@PathVariable Long userId,
                                                        jakarta.servlet.http.HttpServletRequest request) {
        // 实际身份以 token 为准
        userId = currentUserId(request);
        return Result.success(friendService.getFriends(userId));
    }

    @DeleteMapping("/{userId}/{friendId}")
    public Result<Void> deleteFriend(@PathVariable Long userId, @PathVariable Long friendId,
                                     jakarta.servlet.http.HttpServletRequest request) {
        userId = currentUserId(request);
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }

    @PutMapping("/move")
    public Result<Void> moveFriendToGroup(@RequestBody Map<String, Object> params,
                                          jakarta.servlet.http.HttpServletRequest request) {
        friendService.moveFriendToGroup(
                currentUserId(request),
                Long.valueOf(params.get("friendId").toString()),
                Long.valueOf(params.get("groupId").toString())
        );
        return Result.success();
    }

    @PutMapping("/remark")
    public Result<Void> setFriendRemark(@RequestBody Map<String, Object> params,
                                        jakarta.servlet.http.HttpServletRequest request) {
        friendService.setFriendRemark(
                currentUserId(request),
                Long.valueOf(params.get("friendId").toString()),
                (String) params.get("remark")
        );
        return Result.success();
    }

    private Long currentUserId(jakarta.servlet.http.HttpServletRequest request) {
        return (Long) request.getAttribute(com.wujie.im.common.JwtAuthInterceptor.ATTR_USER_ID);
    }
}
