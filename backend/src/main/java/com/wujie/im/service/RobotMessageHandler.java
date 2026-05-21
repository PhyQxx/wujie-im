package com.wujie.im.service;

import com.wujie.im.entity.*;
import com.wujie.im.service.ai.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RobotMessageHandler {

    @Autowired
    private RobotService robotService;
    @Autowired
    private AiService aiService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ConversationService conversationService;

    @Async
    public void handlePrivateChat(Long robotVirtualUserId, Long senderId, String content) {
        try {
            Robot robot = robotService.getRobotByVirtualUserId(robotVirtualUserId);
            if (robot == null || !"ACTIVE".equals(robot.getStatus())) return;

            String reply = generateReply(robot, content);
            if (reply == null) return;

            Conversation robotConv = conversationService.getOrCreateSingleConversation(robotVirtualUserId, senderId);
            messageService.sendMessage(robotVirtualUserId, robotConv.getId(), reply, "AI");
        } catch (Exception e) {
            log.error("机器人私聊处理失败: robotVirtualUserId={}, senderId={}", robotVirtualUserId, senderId, e);
        }
    }

    @Async
    public void handleGroupChat(Long robotVirtualUserId, Long groupId, Long senderId, String content, boolean isMentioned) {
        try {
            Robot robot = robotService.getRobotByVirtualUserId(robotVirtualUserId);
            if (robot == null || !"ACTIVE".equals(robot.getStatus())) return;

            String responseMode = robot.getResponseMode();
            if ("DELAYED".equals(responseMode)) return;
            if ("MENTION".equals(responseMode) && !isMentioned) return;

            String reply = generateReply(robot, content);
            if (reply == null) return;

            Conversation robotConv = conversationService.getOrCreateGroupConversation(robotVirtualUserId, groupId);
            messageService.sendMessage(robotVirtualUserId, robotConv.getId(), reply, "AI");
        } catch (Exception e) {
            log.error("机器人群聊处理失败: robotVirtualUserId={}, groupId={}", robotVirtualUserId, groupId, e);
        }
    }

    private String generateReply(Robot robot, String content) {
        String matched = robotService.matchRule(robot.getId(), content);
        if (matched != null) return matched;

        AiConfig config = robotService.getAiConfig(robot.getId());
        if (config == null) {
            log.warn("机器人未配置AI: robotId={}", robot.getId());
            return null;
        }
        try {
            return aiService.chat(config, new ArrayList<>(), content);
        } catch (Exception e) {
            log.error("AI对话失败: robotId={}", robot.getId(), e);
            return "抱歉，AI服务暂时不可用，请稍后重试。";
        }
    }
}
