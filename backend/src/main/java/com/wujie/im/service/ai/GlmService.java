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
public class GlmService {
    @Value("${wujie.im.ai.glm.api-url}")
    private String apiUrl;

    public String chat(AiConfig config, List<String> history, String userMessage) {
        try {
            URL url = new URL(apiUrl);
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
            if (config.getSystemPrompt() != null) {
                Map<String, String> sysMsg = new HashMap<>();
                sysMsg.put("role", "system");
                sysMsg.put("content", config.getSystemPrompt());
                messages.add(sysMsg);
            }
            for (int i = 0; i < history.size(); i++) {
                Map<String, String> m = new HashMap<>();
                m.put("role", i % 2 == 0 ? "user" : "assistant");
                m.put("content", history.get(i));
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
                    return parseGlmResponse(resp);
                }
            }
            return "请求失败: " + code;
        } catch (Exception e) {
            log.error("GLM API error", e);
            return "AI服务异常: " + e.getMessage();
        }
    }

    private String parseGlmResponse(String resp) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp);
            return node.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            return resp;
        }
    }
}
