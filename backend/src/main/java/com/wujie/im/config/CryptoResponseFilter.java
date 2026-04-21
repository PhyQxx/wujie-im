package com.wujie.im.config;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wujie.im.util.AesUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class CryptoResponseFilter extends OncePerRequestFilter {

    @Autowired
    private AesUtil aesUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        log.info("[CryptoResponse] 拦截响应: method={}, uri={}", request.getMethod(), uri);

        if (!uri.startsWith("/api/message")) {
            log.info("[CryptoResponse] 非message路径，放行");
            filterChain.doFilter(request, response);
            return;
        }

        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            log.info("[CryptoResponse] 非JSON内容类型: {}, 放行", contentType);
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletResponse responseWrapper = new CachedBodyHttpServletResponse(response);

        try {
            filterChain.doFilter(request, responseWrapper);
        } catch (Exception e) {
            log.error("[CryptoResponse] 处理请求异常", e);
            throw e;
        }

        log.info("[CryptoResponse] filterChain执行完毕, committed={}", responseWrapper.isCommitted());

        if (responseWrapper.isCommitted()) {
            log.info("[CryptoResponse] 响应已提交，跳过加密");
            return;
        }

        try {
            String raw = responseWrapper.getCaptureString();
            log.info("[CryptoResponse] 原始响应: {}", raw);

            if (raw == null || raw.isEmpty()) {
                log.info("[CryptoResponse] 响应为空，跳过");
                return;
            }

            JSONObject json = JSONUtil.parseObj(raw);
            if (json.containsKey("data") && json.get("data") != null) {
                String dataStr = JSONUtil.toJsonStr(json.get("data"));
                log.info("[CryptoResponse] 待加密data: {}", dataStr);
                String encrypted = aesUtil.encrypt(dataStr);
                log.info("[CryptoResponse] 加密后data: {}", encrypted);
                json.set("data", encrypted);
            }

            String encryptedJson = json.toString();
            byte[] encryptedBytes = encryptedJson.getBytes(StandardCharsets.UTF_8);

            response.setContentLength(encryptedBytes.length);
            response.setContentType("application/json");
            response.getOutputStream().write(encryptedBytes);
            response.getOutputStream().flush();

            log.info("[CryptoResponse] 响应加密成功");
        } catch (Exception e) {
            log.error("[CryptoResponse] 加密响应失败: {}", e.getMessage(), e);
            responseWrapper.writeToClient();
        }
    }
}
