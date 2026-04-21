package com.wujie.im.config;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wujie.im.util.AesUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MessageCryptoFilter implements Filter {

    @Autowired
    private AesUtil aesUtil;

    @Bean
    public FilterRegistrationBean<MessageCryptoFilter> registration() {
        FilterRegistrationBean<MessageCryptoFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(this);
        reg.setOrder(1); // 确保在所有Filter中最先执行
        return reg;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/message")) {
            chain.doFilter(request, response);
            return;
        }

        log.info("[MessageCrypto] 拦截请求: method={}, uri={}", request.getMethod(), uri);

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        String body = wrappedRequest.getBody();
        log.info("[MessageCrypto] 请求body: {}", body);

        try {
            JSONObject json = JSONUtil.parseObj(body);
            if (json.containsKey("data") && json.get("data") != null) {
                String dataStr = json.get("data").toString();
                log.info("[MessageCrypto] 密文data: {}", dataStr);
                String decrypted = aesUtil.decrypt(dataStr);
                log.info("[MessageCrypto] 解密结果: {}", decrypted);
                if (decrypted != null) {
                    // 把解密后的内容提升到顶层（去掉外层data包装）
                    JSONObject decryptedJson = JSONUtil.parseObj(decrypted);
                    json.putAll(decryptedJson);
                    json.remove("data");
                    log.info("[MessageCrypto] 解密并提升到顶层成功，解密后body: {}", json.toString());
                } else {
                    log.warn("[MessageCrypto] 解密返回null，密文可能不匹配");
                }
            }
            wrappedRequest.setBody(json.toString());
        } catch (Exception e) {
            log.error("[MessageCrypto] 解密请求data失败: {}", e.getMessage(), e);
        }

        chain.doFilter(wrappedRequest, response);
    }
}
