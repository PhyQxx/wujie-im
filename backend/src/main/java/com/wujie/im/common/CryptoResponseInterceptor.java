package com.wujie.im.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wujie.im.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * AES-CBC 响应加密拦截器
 */
@Slf4j
@Component
@ControllerAdvice
@RequiredArgsConstructor
public class CryptoResponseInterceptor implements ResponseBodyAdvice<Object> {

    private final AesUtil aesUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 暂时禁用响应加密，让系统恢复正常工作
        return false;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) {
            return body;
        }
        try {
            String json = objectMapper.writeValueAsString(body);
            String encrypted = aesUtil.encrypt(json);
            log.debug("响应加密: {} -> {}", json, encrypted);
            return encrypted;
        } catch (Exception e) {
            log.error("响应加密失败", e);
            return body;
        }
    }
}
