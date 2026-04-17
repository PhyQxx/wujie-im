package com.wujie.im.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.*;
import com.wujie.im.mapper.*;
import com.wujie.im.websocket.WsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private MessageReadMapper messageReadMapper;
    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    @Autowired
    @org.springframework.context.annotation.Lazy
    private WsHandler wsHandler;

    public Message sendMessage(Long senderId, Long conversationId, String content, String contentType) {
        return sendMessage(senderId, conversationId, content, contentType, null, null);
    }

    public Message sendMessage(Long senderId, Long conversationId, String content, String contentType, String metaStr) {
        return sendMessage(senderId, conversationId, content, contentType, metaStr, null);
    }

    public Message sendMessage(Long senderId, Long conversationId, String content, String contentType, String metaStr, Long replyId) {
        // 检查群聊禁言
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv != null && "GROUP".equals(conv.getType())) {
            GroupMember member = groupMemberMapper.selectOne(
                    new LambdaQueryWrapper<GroupMember>()
                            .eq(GroupMember::getGroupId, conv.getTypeId())
                            .eq(GroupMember::getUserId, senderId)
            );
            if (member != null && member.getMuted() != null && member.getMuted() == 1) {
                throw new RuntimeException("您已被禁言");
            }
        }

        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setContentType(contentType != null ? contentType : "TEXT");
        msg.setMeta(metaStr);
        msg.setReplyId(replyId);
        msg.setStatus("SENT");
        messageMapper.insert(msg);

        // 更新会话最后消息
        conversationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Conversation>()
                        .eq(Conversation::getId, conversationId)
                        .set(Conversation::getLastMessageId, msg.getId())
                        .set(Conversation::getLastMessageContent, content != null && content.length() > 50 ? content.substring(0, 50) : content)
                        .set(Conversation::getLastMessageTime, msg.getCreateTime())
        );

        // 推送消息给会话成员
        pushMessageToConversation(conv, msg, senderId);
        return msg;
    }

    private void pushMessageToConversation(Conversation conv, Message msg, Long excludeUserId) {
        if (conv == null) return;
        String pushJson = JSONUtil.toJsonStr(Map.of("type", "message", "data", msg));

        if ("SINGLE".equals(conv.getType())) {
            Long otherUserId = conv.getTypeId();
            if (!otherUserId.equals(excludeUserId)) {
                wsHandler.sendToUser(otherUserId, pushJson);
            }
            wsHandler.sendToUser(excludeUserId, pushJson);
        } else if ("GROUP".equals(conv.getType())) {
            List<GroupMember> members = groupMemberMapper.selectList(
                    new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, conv.getTypeId())
            );
            for (GroupMember m : members) {
                if (!m.getUserId().equals(excludeUserId)) {
                    wsHandler.sendToUser(m.getUserId(), pushJson);
                }
            }
            wsHandler.sendToUser(excludeUserId, pushJson);
        }
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
        List<Message> msgs = messageMapper.selectList(q);
        Collections.reverse(msgs);
        return msgs;
    }

    public Map<String, Object> getConversationReadStatus(Long conversationId, Long userId) {
        MessageRead read = messageReadMapper.selectOne(
                new LambdaQueryWrapper<MessageRead>()
                        .eq(MessageRead::getUserId, userId)
                        .eq(MessageRead::getConversationId, conversationId)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("lastReadId", read != null ? read.getLastReadId() : 0);
        result.put("lastReadTime", read != null ? read.getUpdateTime() : null);
        return result;
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
        // 通知消息发送者已读
        Message msg = messageMapper.selectById(messageId);
        if (msg != null) {
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "message_read");
            pushData.put("data", Map.of(
                    "messageId", messageId,
                    "conversationId", conversationId,
                    "readBy", userId
            ));
            wsHandler.sendToUser(msg.getSenderId(), JSONUtil.toJsonStr(pushData));
        }
    }

    public void recallMessage(Long userId, Long messageId) {
        Message msg = messageMapper.selectById(messageId);
        if (msg == null) return;
        if (!msg.getSenderId().equals(userId)) return;

        msg.setRecall(1);
        msg.setStatus("RECALLED");
        messageMapper.updateById(msg);

        // 通知会话中的其他成员
        Conversation conv = conversationMapper.selectById(msg.getConversationId());
        if (conv != null) {
            String pushJson = JSONUtil.toJsonStr(Map.of(
                    "type", "message_recalled",
                    "data", Map.of("messageId", messageId, "conversationId", msg.getConversationId())
            ));
            if ("SINGLE".equals(conv.getType())) {
                if (!conv.getTypeId().equals(userId)) wsHandler.sendToUser(conv.getTypeId(), pushJson);
            } else if ("GROUP".equals(conv.getType())) {
                List<GroupMember> members = groupMemberMapper.selectList(
                        new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, conv.getTypeId())
                );
                for (GroupMember m : members) {
                    if (!m.getUserId().equals(userId)) {
                        wsHandler.sendToUser(m.getUserId(), pushJson);
                    }
                }
            }
        }
        wsHandler.sendToUser(userId, JSONUtil.toJsonStr(Map.of("type", "message_recalled", "data", messageId)));
    }
}
