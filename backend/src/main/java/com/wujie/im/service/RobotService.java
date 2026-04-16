package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.*;
import com.wujie.im.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class RobotService {
    @Autowired
    private RobotMapper robotMapper;
    @Autowired
    private RobotRuleMapper robotRuleMapper;
    @Autowired
    private AiConfigMapper aiConfigMapper;

    public Robot createRobot(String name, String avatar, String type, Long ownerId) {
        Robot robot = new Robot();
        robot.setName(name);
        robot.setAvatar(avatar);
        robot.setType(type != null ? type : "AI");
        robot.setOwnerId(ownerId);
        robot.setStatus("ACTIVE");
        robot.setResponseMode("MENTION");
        robot.setContextSize(20);
        robotMapper.insert(robot);
        return robot;
    }

    public void updateRobot(Long robotId, Map<String, Object> params) {
        Robot robot = robotMapper.selectById(robotId);
        if (robot != null && params != null) {
            if (params.containsKey("status")) robot.setStatus((String) params.get("status"));
            if (params.containsKey("name")) robot.setName((String) params.get("name"));
            if (params.containsKey("avatar")) robot.setAvatar((String) params.get("avatar"));
            if (params.containsKey("description")) robot.setDescription((String) params.get("description"));
            robotMapper.updateById(robot);
        }
    }

    public void deleteRobot(Long robotId) {
        robotMapper.deleteById(robotId);
        robotRuleMapper.delete(new LambdaQueryWrapper<RobotRule>().eq(RobotRule::getRobotId, robotId));
        aiConfigMapper.delete(new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getRobotId, robotId));
    }

    public List<Robot> listRobots(Long ownerId) {
        LambdaQueryWrapper<Robot> q = new LambdaQueryWrapper<Robot>().eq(Robot::getOwnerId, ownerId);
        return robotMapper.selectList(q);
    }

    public AiConfig getAiConfig(Long robotId) {
        return aiConfigMapper.selectOne(
                new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getRobotId, robotId)
        );
    }

    public void saveAiConfig(AiConfig config) {
        AiConfig exist = aiConfigMapper.selectOne(
                new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getRobotId, config.getRobotId())
        );
        if (exist != null) {
            config.setId(exist.getId());
            aiConfigMapper.updateById(config);
        } else {
            aiConfigMapper.insert(config);
        }
    }

    public List<RobotRule> getRules(Long robotId) {
        return robotRuleMapper.selectList(
                new LambdaQueryWrapper<RobotRule>()
                        .eq(RobotRule::getRobotId, robotId)
                        .eq(RobotRule::getEnabled, 1)
                        .orderByDesc(RobotRule::getPriority)
        );
    }

    public RobotRule addRule(Long robotId, RobotRule rule) {
        rule.setRobotId(robotId);
        rule.setEnabled(1);
        if (rule.getPriority() == null) rule.setPriority(0);
        robotRuleMapper.insert(rule);
        return rule;
    }

    public void updateRule(Long ruleId, RobotRule rule) {
        rule.setId(ruleId);
        robotRuleMapper.updateById(rule);
    }

    public void deleteRule(Long ruleId) {
        robotRuleMapper.deleteById(ruleId);
    }

    public String matchRule(Long robotId, String content) {
        List<RobotRule> rules = getRules(robotId);
        for (RobotRule rule : rules) {
            if ("KEYWORD".equals(rule.getRuleType()) && rule.getKeyword() != null
                    && content.contains(rule.getKeyword())) {
                return replaceVariables(rule.getReplyContent(), content);
            }
            if ("REGEX".equals(rule.getRuleType()) && rule.getPattern() != null) {
                try {
                    if (content.matches(rule.getPattern())) {
                        return replaceVariables(rule.getReplyContent(), content);
                    }
                } catch (Exception e) {
                    log.warn("正则匹配失败: {}", rule.getPattern(), e);
                }
            }
            if ("WEBHOOK".equals(rule.getRuleType()) && rule.getWebhookUrl() != null) {
                try {
                    String reply = callWebhook(rule.getWebhookUrl(), content);
                    if (reply != null) return reply;
                } catch (Exception e) {
                    log.error("Webhook调用失败: {}", rule.getWebhookUrl(), e);
                    return "机器人响应超时，请稍后重试";
                }
            }
        }
        return null;
    }

    private String replaceVariables(String template, String rawText) {
        if (template == null) return "";
        String result = template;
        result = result.replace("{{text}}", rawText);
        result = result.replace("{{time}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result = result.replace("{{date}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return result;
    }

    private String callWebhook(String webhookUrl, String text) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            Map<String, Object> body = new HashMap<>();
            body.put("text", text);
            body.put("time", LocalDateTime.now().toString());
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                try (java.util.Scanner s = new java.util.Scanner(conn.getInputStream())) {
                    String resp = s.useDelimiter("\\A").next();
                    try {
                        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp);
                        return node.has("reply") ? node.get("reply").asText() : node.has("content") ? node.get("content").asText() : resp;
                    } catch (Exception e) {
                        return resp;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Webhook调用失败: " + e.getMessage());
        }
    }
}
