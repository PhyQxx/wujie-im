package com.wujie.im.controller;

import com.wujie.im.common.Encrypt;
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

    @Encrypt
    @GetMapping("/list")
    public Result<List<User>> listUsers(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long excludeId) {
        return Result.success(userService.listUsers(keyword, excludeId));
    }

    @Encrypt
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) return Result.error(404, "用户不存在");
        return Result.success(user);
    }

    @Encrypt
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody Map<String, String> params) {
        Long userId = Long.parseLong(params.get("userId"));
        String status = params.get("status");
        userService.updateStatus(userId, status);
        return Result.success();
    }

    @Encrypt
    @GetMapping("/profile/{userId}")
    public Result<UserProfile> getProfile(@PathVariable Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    @Encrypt
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserProfile profile) {
        userService.updateProfile(profile);
        return Result.success();
    }

    @GetMapping("/debug-token")
    public Result<Map<String, Object>> debugToken(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        try {
            var claims = com.wujie.im.common.JwtUtil.class.getDeclaredField("secret");
            claims.setAccessible(true);
            return Result.success(Map.of(
                "tokenLength", token.length(),
                "tokenPrefix", token.substring(0, Math.min(50, token.length())),
                "note", "需要重启后端使代码生效"
            ));
        } catch (Exception e) {
            return Result.success(Map.of("error", e.getMessage()));
        }
    }
}
