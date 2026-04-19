package com.wujie.im.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wujie.im.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 支持加密请求的 HttpMessageConverter
 */
@Slf4j
public class CryptoJackson2HttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final AesUtil aesUtil;
    private final ObjectMapper objectMapper;

    // 不需要解密的路径
    private static final String[] NO_DECRYPT_PATHS = {
        "/auth/login", "/auth/register", "/auth/refresh", "/auth/debug-verify", "/upload/"
    };

    public CryptoJackson2HttpMessageConverter(AesUtil aesUtil) {
        super(MediaType.APPLICATION_JSON);
        this.aesUtil = aesUtil;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        String path = "";
        try {
            if (inputMessage instanceof org.springframework.http.server.ServletServerHttpRequest) {
                path = ((org.springframework.http.server.ServletServerHttpRequest) inputMessage).getServletRequest().getRequestURI();
            }
        } catch (Exception e) {
            log.debug("无法获取请求路径", e);
        }

        // 检查是否需要解密
        if (!shouldDecrypt(path)) {
            return objectMapper.readValue(inputMessage.getBody(), Map.class);
        }

        // 读取 body
        String body = readBody(inputMessage.getBody());
        log.info("收到加密请求: {} body长度={}", path, body.length());

        // 解密
        String decrypted = aesUtil.decrypt(body);
        if (decrypted == null) {
            log.error("解密失败");
            throw new HttpMessageNotReadableException("解密失败");
        }
        log.info("解密成功: {}", decrypted);

        // 解析解密后的 JSON
        return objectMapper.readValue(decrypted, Map.class);
    }

    private boolean shouldDecrypt(String path) {
        for (String noDecryptPath : NO_DECRYPT_PATHS) {
            if (path.contains(noDecryptPath)) {
                return false;
            }
        }
        return true;
    }

    private String readBody(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    @Override
    protected void writeInternal(Object object, org.springframework.http.HttpOutputMessage outputMessage) throws IOException {
        // 响应加密：序列化为JSON后加密
        String json = objectMapper.writeValueAsString(object);
        log.info("响应JSON长度: {}", json.length());
        String encrypted = aesUtil.encrypt(json);
        log.info("加密后长度: {}", encrypted.length());
        outputMessage.getHeaders().setContentType(MediaType.parseMediaType("text/plain;charset=UTF-8"));
        outputMessage.getBody().write(encrypted.getBytes(StandardCharsets.UTF_8));
    }
}
