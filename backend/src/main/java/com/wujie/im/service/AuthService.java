package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.common.JwtUtil;
import com.wujie.im.entity.User;
import com.wujie.im.entity.UserProfile;
import com.wujie.im.mapper.UserMapper;
import com.wujie.im.mapper.UserProfileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserProfileMapper userProfileMapper;
    @Autowired
    private JwtUtil jwtUtil;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Map<String, String> register(String username, String password, String phone, String email) {
        User exist = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(1);
        user.setRole("USER");
        user.setUserStatus("ONLINE");
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setNickname(username);
        userProfileMapper.insert(profile);

        Map<String, String> result = new HashMap<>();
        result.put("userId", String.valueOf(user.getId()));
        return result;
    }

    public Map<String, String> login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .or()
                        .eq(User::getEmail, username)
        );
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        Map<String, String> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userId", String.valueOf(user.getId()));
        result.put("username", user.getUsername());
        result.put("role", user.getRole() != null ? user.getRole() : "USER");
        return result;
    }

    public Map<String, String> refresh(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token无效");
        }
        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }
}
