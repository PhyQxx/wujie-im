package com.wujie.im.common;

import com.wujie.im.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 解密后的 HTTP 输入消息
 */
@Slf4j
@RequiredArgsConstructor
public class DecryptedHttpInputMessage implements HttpInputMessage {

    private final HttpInputMessage original;
    private final AesUtil aesUtil;

    @Override
    public HttpHeaders getHeaders() {
        return original.getHeaders();
    }

    @Override
    public java.io.InputStream getBody() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(original.getBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        String encrypted = sb.toString();
        log.debug("请求解密: {}", encrypted);

        // 解密并返回
        String decrypted = aesUtil.decrypt(encrypted);
        return new ByteArrayInputStream(decrypted.getBytes(StandardCharsets.UTF_8));
    }
}
