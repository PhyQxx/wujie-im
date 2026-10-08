package com.wujie.im.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wujie.im.entity.User;
import com.wujie.im.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局 JWT 鉴权拦截器（原先项目无任何统一鉴权，身份靠 body/path 参数传递，
 * 存在越权风险）。现统一要求 /api/** 携带有效 Bearer token（/api/auth/** 除外），
 * 身份一律从 token 解析并放入 request attribute：
 *   userId (Long) / username (String)
 * /api/admin/** 额外要求用户表 role=ADMIN。
 */
@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_USERNAME = "username";

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return reject(response, 401, "未登录");
        }
        Claims claims;
        try {
            claims = jwtUtil.parseToken(auth.substring(7));
        } catch (Exception e) {
            return reject(response, 401, "登录已过期，请重新登录");
        }
        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            return reject(response, 401, "无效令牌");
        }

        // 管理端：校验用户角色（按需查库，管理操作频率低）
        if (request.getRequestURI().startsWith("/api/admin")) {
            User user = userMapper.selectById(userId);
            if (user == null || !"ADMIN".equals(user.getRole())) {
                return reject(response, 403, "无管理员权限");
            }
        }

        request.setAttribute(ATTR_USER_ID, userId);
        request.setAttribute(ATTR_USERNAME, claims.getSubject());
        return true;
    }

    private boolean reject(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED == code ? 401 : 403);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("msg", msg);
        body.put("data", null);
        response.getOutputStream().write(objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8));
        return false;
    }
}
