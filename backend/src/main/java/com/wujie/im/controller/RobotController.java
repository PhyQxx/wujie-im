package com.wujie.im.controller;

import com.wujie.im.common.Encrypt;
import com.wujie.im.common.Result;
import com.wujie.im.entity.Robot;
import com.wujie.im.entity.RobotRule;
import com.wujie.im.entity.AiConfig;
import com.wujie.im.service.RobotService;
import com.wujie.im.service.ai.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/robot")
public class RobotController {
    @Autowired
    private RobotService robotService;
    @Autowired
    private AiService aiService;

    @Encrypt
    @PostMapping("/create")
    public Result<Robot> createRobot(@RequestBody Map<String, Object> params) {
        return Result.success(robotService.createRobot(
                (String) params.get("name"),
                (String) params.get("avatar"),
                (String) params.get("type"),
                Long.valueOf(params.get("ownerId").toString())
        ));
    }

    @Encrypt
    @GetMapping("/list")
    public Result<List<Robot>> listRobots(@RequestParam Long ownerId) {
        return Result.success(robotService.listRobots(ownerId));
    }

    @Encrypt
    @PutMapping("/{robotId}")
    public Result<Void> updateRobot(@PathVariable Long robotId, @RequestBody Map<String, Object> params) {
        robotService.updateRobot(robotId, params);
        return Result.success();
    }

    @Encrypt
    @DeleteMapping("/{robotId}")
    public Result<Void> deleteRobot(@PathVariable Long robotId) {
        robotService.deleteRobot(robotId);
        return Result.success();
    }

    @Encrypt
    @GetMapping("/{robotId}/ai-config")
    public Result<AiConfig> getAiConfig(@PathVariable Long robotId) {
        return Result.success(robotService.getAiConfig(robotId));
    }

    @Encrypt
    @PostMapping("/{robotId}/ai-config")
    public Result<Void> saveAiConfig(@PathVariable Long robotId, @RequestBody AiConfig config) {
        config.setRobotId(robotId);
        robotService.saveAiConfig(config);
        return Result.success();
    }

    @Encrypt
    @GetMapping("/rules/{robotId}")
    public Result<List<RobotRule>> getRules(@PathVariable Long robotId) {
        return Result.success(robotService.getRules(robotId));
    }

    @Encrypt
    @PostMapping("/rules/{robotId}")
    public Result<RobotRule> addRule(@PathVariable Long robotId, @RequestBody RobotRule rule) {
        return Result.success(robotService.addRule(robotId, rule));
    }

    @Encrypt
    @PutMapping("/rules/{ruleId}")
    public Result<Void> updateRule(@PathVariable Long ruleId, @RequestBody RobotRule rule) {
        robotService.updateRule(ruleId, rule);
        return Result.success();
    }

    @Encrypt
    @DeleteMapping("/rules/{ruleId}")
    public Result<Void> deleteRule(@PathVariable Long ruleId) {
        robotService.deleteRule(ruleId);
        return Result.success();
    }

    @Encrypt
    @PostMapping("/chat/{robotId}")
    public Result<Map<String, String>> chat(@PathVariable Long robotId, @RequestBody Map<String, Object> params) {
        String content = (String) params.get("content");

        // 先匹配自定义规则（关键字/正则/Webhook）
        String matched = robotService.matchRule(robotId, content);
        String reply;
        if (matched != null) {
            reply = matched;
        } else {
            // 再尝试 AI 对话
            AiConfig config = robotService.getAiConfig(robotId);
            if (config == null) return Result.error("机器人未配置AI");
            @SuppressWarnings("unchecked")
            List<String> history = (List<String>) params.getOrDefault("history", new ArrayList<>());
            reply = aiService.chat(config, history, content);
        }

        Map<String, String> result = new HashMap<>();
        result.put("reply", reply);
        return Result.success(result);
    }
}
