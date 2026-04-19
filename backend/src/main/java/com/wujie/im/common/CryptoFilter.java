package com.wujie.im.common;

import com.wujie.im.util.AesUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * 加密请求过滤器
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class CryptoFilter implements Filter {

    private final AesUtil aesUtil;

    // 不需要解密的路径
    private static final String[] NO_DECRYPT_PATHS = {
        "/auth/login", "/auth/register", "/auth/refresh", "/auth/debug-verify", "/upload/"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 禁用，改为使用 CryptoJackson2HttpMessageConverter
        chain.doFilter(request, response);
    }

    private boolean shouldDecrypt(String path) {
        for (String noDecryptPath : NO_DECRYPT_PATHS) {
            if (path.contains(noDecryptPath)) {
                return false;
            }
        }
        return true;
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
