package com.wujie.im.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.Message;
import com.wujie.im.entity.MessageRead;
import com.wujie.im.mapper.MessageMapper;
import com.wujie.im.mapper.MessageReadMapper;
import com.wujie.im.websocket.WsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private MessageReadMapper messageReadMapper;
    @Autowired
    @org.springframework.context.annotation.Lazy
    private WsHandler wsHandler;

    public Message sendMessage(Long senderId, Long conversationId, String content, String contentType) {
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setContentType(contentType != null ? contentType : "TEXT");
        msg.setStatus("SENT");
        messageMapper.insert(msg);

        Map<String, Object> pushData = new HashMap<>();
        pushData.put("type", "message");
        pushData.put("data", msg);
        wsHandler.sendToUser(senderId, JSONUtil.toJsonStr(pushData));
        return msg;
    }

    public List<Message> getMessages(Long conversationId, Long beforeId, int limit) {
        LambdaQueryWrapper<Message> q = new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .eq(Message::getRecall, 0)
                .orderByDesc(Message::getId);
        if (beforeId != null) {
            q.lt(Message::getId, beforeId);
        }
        q.last("LIMIT " + limit);
        return messageMapper.selectList(q);
    }

    public void markAsRead(Long userId, Long conversationId, Long messageId) {
        MessageRead read = messageReadMapper.selectOne(
                new LambdaQueryWrapper<MessageRead>()
                        .eq(MessageRead::getUserId, userId)
                        .eq(MessageRead::getConversationId, conversationId)
        );
        if (read == null) {
            read = new MessageRead();
            read.setUserId(userId);
            read.setConversationId(conversationId);
            read.setLastReadId(messageId);
            messageReadMapper.insert(read);
        } else {
            read.setLastReadId(messageId);
            messageReadMapper.updateById(read);
        }
    }

    public void recallMessage(Long userId, Long messageId) {
        Message msg = messageMapper.selectById(messageId);
        if (msg != null && msg.getSenderId().equals(userId)) {
            msg.setRecall(1);
            msg.setStatus("RECALLED");
            messageMapper.updateById(msg);
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "recall_message");
            pushData.put("data", messageId);
            wsHandler.sendToUser(userId, JSONUtil.toJsonStr(pushData));
        }
    }
}
