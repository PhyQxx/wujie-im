package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.AdminUser;
import com.wujie.im.entity.SensitiveWord;
import com.wujie.im.mapper.AdminUserMapper;
import com.wujie.im.mapper.SensitiveWordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminUserMapper adminUserMapper;
    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        AdminUser admin = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, params.get("username"))
        );
        if (admin == null || !encoder.matches(params.get("password"), admin.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("role", admin.getRole());
        return Result.success(result);
    }

    @GetMapping("/sensitive-words")
    public Result<List<SensitiveWord>> listSensitiveWords() {
        return Result.success(sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>()
        ));
    }

    @PostMapping("/sensitive-word")
    public Result<Void> addSensitiveWord(@RequestBody SensitiveWord word) {
        sensitiveWordMapper.insert(word);
        return Result.success();
    }

    @DeleteMapping("/sensitive-word/{id}")
    public Result<Void> deleteSensitiveWord(@PathVariable Long id) {
        sensitiveWordMapper.deleteById(id);
        return Result.success();
    }
}
