package com.wujie.im.service.ai;

import com.wujie.im.entity.AiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MiniMaxService {
    @Value("${wujie.im.ai.minimax.api-url}")
    private String apiUrl;

    public String chat(AiConfig config, String userMessage) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Map<String, Object> body = new HashMap<>();
            body.put("model", config.getModel());
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", userMessage);
            body.put("messages", new Object[]{msg});
            body.put("temperature", config.getTemperature());
            body.put("max_tokens", config.getMaxTokens());

            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                try (java.util.Scanner s = new java.util.Scanner(conn.getInputStream())) {
                    String resp = s.useDelimiter("\\A").next();
                    return parseMiniMaxResponse(resp);
                }
            }
            return "请求失败: " + code;
        } catch (Exception e) {
            log.error("MiniMax API error", e);
            return "AI服务异常: " + e.getMessage();
        }
    }

    private String parseMiniMaxResponse(String resp) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp);
            return node.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            return resp;
        }
    }
}
