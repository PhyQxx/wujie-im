package com.wujie.im.service.ai;

import com.wujie.im.entity.AiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DeepSeekService {
    @Value("${wujie.im.ai.deepseek.api-url}")
    private String apiUrl;

    public String chat(AiConfig config, List<String> history, String userMessage) {
        try {
            String targetUrl = (config.getApiUrl() != null && !config.getApiUrl().isBlank()) ? config.getApiUrl() : apiUrl;
            
            // 自动修正符合 OpenAI 规范的 URL
            if (targetUrl != null && !targetUrl.contains("/chat/completions")) {
                if (targetUrl.endsWith("/")) {
                    targetUrl += "chat/completions";
                } else {
                    targetUrl += "/chat/completions";
                }
            }
            
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Map<String, Object> body = new HashMap<>();
            body.put("model", config.getModel());
            body.put("temperature", config.getTemperature());
            body.put("max_tokens", config.getMaxTokens());

            List<Map<String, String>> messages = new ArrayList<>();
            if (config.getSystemPrompt() != null && !config.getSystemPrompt().isBlank()) {
                Map<String, String> sysMsg = new HashMap<>();
                sysMsg.put("role", "system");
                sysMsg.put("content", config.getSystemPrompt());
                messages.add(sysMsg);
            }
            for (String h : history) {
                Map<String, String> m = new HashMap<>();
                if (h.startsWith("assistant:")) {
                    m.put("role", "assistant");
                    m.put("content", h.substring(10));
                } else if (h.startsWith("user:")) {
                    m.put("role", "user");
                    m.put("content", h.substring(5));
                } else {
                    m.put("role", "user");
                    m.put("content", h);
                }
                messages.add(m);
            }
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", userMessage);
            messages.add(msg);
            body.put("messages", messages);

            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                try (java.util.Scanner s = new java.util.Scanner(conn.getInputStream())) {
                    String resp = s.useDelimiter("\\A").next();
                    return parseDeepSeekResponse(resp);
                }
            } else {
                try (java.util.Scanner s = new java.util.Scanner(conn.getErrorStream())) {
                    String err = s.hasNext() ? s.useDelimiter("\\A").next() : "无错误详情";
                    log.error("DeepSeek API error: url={}, code={}, response={}", targetUrl, code, err);
                    return "请求失败: " + code + " (" + err + ")";
                }
            }
        } catch (Exception e) {
            log.error("DeepSeek API error", e);
            return "AI服务异常: " + e.getMessage();
        }
    }

    private String parseDeepSeekResponse(String resp) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp);
            return node.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            return resp;
        }
    }
}
