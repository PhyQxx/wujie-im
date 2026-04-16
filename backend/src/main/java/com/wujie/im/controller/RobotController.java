package com.wujie.im.controller;

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

    @PostMapping("/create")
    public Result<Robot> createRobot(@RequestBody Map<String, Object> params) {
        return Result.success(robotService.createRobot(
                (String) params.get("name"),
                (String) params.get("avatar"),
                (String) params.get("type"),
                Long.valueOf(params.get("ownerId").toString())
        ));
    }

    @GetMapping("/list")
    public Result<List<Robot>> listRobots(@RequestParam Long ownerId) {
        return Result.success(robotService.listRobots(ownerId));
    }

    @GetMapping("/config/{robotId}")
    public Result<AiConfig> getAiConfig(@PathVariable Long robotId) {
        return Result.success(robotService.getAiConfig(robotId));
    }

    @PostMapping("/config")
    public Result<Void> saveAiConfig(@RequestBody AiConfig config) {
        robotService.saveAiConfig(config);
        return Result.success();
    }

    @GetMapping("/rules/{robotId}")
    public Result<List<RobotRule>> getRules(@PathVariable Long robotId) {
        return Result.success(robotService.getRules(robotId));
    }

    @PostMapping("/chat/{robotId}")
    public Result<Map<String, String>> chat(@PathVariable Long robotId, @RequestBody Map<String, Object> params) {
        AiConfig config = robotService.getAiConfig(robotId);
        if (config == null) return Result.error("机器人未配置AI");

        String content = (String) params.get("content");
        String matched = robotService.matchRule(robotId, content);
        String reply;
        if (matched != null) {
            reply = matched;
        } else {
            @SuppressWarnings("unchecked")
            List<String> history = (List<String>) params.getOrDefault("history", new ArrayList<>());
            reply = aiService.chat(config, history, content);
        }

        Map<String, String> result = new HashMap<>();
        result.put("reply", reply);
        return Result.success(result);
    }
}
