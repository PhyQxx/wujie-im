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

    public Map<String, String> register(String username, String password, String phone, String email, String nickname, String userType) {
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
        user.setUserType(userType != null ? userType : "PERSONAL");
        user.setUserStatus("OFFLINE");
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setNickname(nickname != null ? nickname : username);
        userProfileMapper.insert(profile);

        Map<String, String> result = new HashMap<>();
        result.put("userId", String.valueOf(user.getId()));
        return result;
    }

    /**
     * pnkx 统一登录（OIDC）：按 sso_id 查号，未命中 JIT 建号（user + user_profile），
     * 签发本系统双 token。用户名冲突追加后缀，绝不绑定同名本地账号（防接管）。
     */
    public Map<String, String> loginWithSso(String ssoId, String preferredUsername, String displayName, boolean pnkxAdmin) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getSsoId, ssoId)
        );
        if (user == null) {
            String username = (preferredUsername != null && !preferredUsername.isBlank())
                    ? preferredUsername : "pnkx-" + ssoId;
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username)
            );
            if (count != null && count > 0) {
                username = username + "_" + ssoId;
            }
            user = new User();
            user.setUsername(username);
            user.setSsoId(ssoId);
            // 随机密码占位：SSO 用户不走本地密码登录
            user.setPassword(encoder.encode(java.util.UUID.randomUUID().toString()));
            user.setStatus(1);
            user.setRole(pnkxAdmin ? "ADMIN" : "USER");
            user.setUserType("PERSONAL");
            user.setUserStatus("OFFLINE");
            userMapper.insert(user);

            UserProfile profile = new UserProfile();
            profile.setUserId(user.getId());
            profile.setNickname((displayName != null && !displayName.isBlank()) ? displayName : username);
            userProfileMapper.insert(profile);
            log.info("SSO JIT user created: username={} ssoId={} admin={}", username, ssoId, pnkxAdmin);
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

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public Long getUserIdFromToken(String token) {
        return jwtUtil.getUserId(token);
    }
}
