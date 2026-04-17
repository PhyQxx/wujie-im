package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.User;
import com.wujie.im.entity.UserProfile;
import com.wujie.im.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<User>> listUsers(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long excludeId) {
        return Result.success(userService.listUsers(keyword, excludeId));
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) return Result.error(404, "用户不存在");
        return Result.success(user);
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody Map<String, String> params) {
        Long userId = Long.parseLong(params.get("userId"));
        String status = params.get("status");
        userService.updateStatus(userId, status);
        return Result.success();
    }

    @GetMapping("/profile/{userId}")
    public Result<UserProfile> getProfile(@PathVariable Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserProfile profile) {
        userService.updateProfile(profile);
        return Result.success();
    }
}
