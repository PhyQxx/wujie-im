package com.wujie.im.listener;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.*;
import com.wujie.im.event.MessageRecalledEvent;
import com.wujie.im.event.MessageReadEvent;
import com.wujie.im.event.MessageSentEvent;
import com.wujie.im.mapper.GroupMemberMapper;
import com.wujie.im.service.ConversationService;
import com.wujie.im.service.MessageService;
import com.wujie.im.websocket.WsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MessagePushEventListener {

    @Autowired
    private WsHandler wsHandler;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Async
    @EventListener
    public void handleMessageSentEvent(MessageSentEvent event) {
        messageService.pushMessageToConversationAsync(
                event.getConversation(),
                event.getMessage(),
                event.getExcludeUserId()
        );
    }

    @Async
    @EventListener
    public void handleMessageReadEvent(MessageReadEvent event) {
        Map<String, Object> pushData = Map.of(
                "type", "message_read",
                "data", Map.of(
                        "messageId", event.getMessageId(),
                        "conversationId", event.getConversationId(),
                        "readBy", event.getUserId()
                )
        );
        wsHandler.sendToUser(event.getMessageSenderId(), JSONUtil.toJsonStr(pushData));
    }

    @Async
    @EventListener
    public void handleMessageRecalledEvent(MessageRecalledEvent event) {
        Conversation conv = event.getConversation();
        Long userId = event.getUserId();
        Long messageId = event.getMessageId();

        if ("SINGLE".equals(conv.getType())) {
            if (!conv.getTypeId().equals(userId)) {
                wsHandler.sendToUser(conv.getTypeId(), JSONUtil.toJsonStr(Map.of(
                        "type", "message_recalled",
                        "data", Map.of("messageId", messageId, "conversationId", conv.getId())
                )));
            }
        } else if ("GROUP".equals(conv.getType())) {
            List<GroupMember> members = groupMemberMapper.selectList(
                    new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, conv.getTypeId())
            );
            for (GroupMember m : members) {
                if (!m.getUserId().equals(userId)) {
                    wsHandler.sendToUser(m.getUserId(), JSONUtil.toJsonStr(Map.of(
                            "type", "message_recalled",
                            "data", Map.of("messageId", messageId, "conversationId", conv.getId())
                    )));
                }
            }
        }
        wsHandler.sendToUser(userId, JSONUtil.toJsonStr(Map.of(
                "type", "message_recalled",
                "data", messageId
        )));
    }
}
