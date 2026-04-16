package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Result<Map<String, String>> register(@RequestBody Map<String, String> params) {
        try {
            return Result.success("注册成功", authService.register(
                    params.get("username"),
                    params.get("password"),
                    params.get("phone"),
                    params.get("email")
            ));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> params) {
        try {
            return Result.success(authService.login(params.get("username"), params.get("password")));
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> params) {
        try {
            return Result.success(authService.refresh(params.get("refreshToken")));
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }
}
