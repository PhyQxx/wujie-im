package com.wujie.im.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.*;
import com.wujie.im.mapper.*;
import com.wujie.im.event.MessageSentEvent;
import com.wujie.im.event.MessageReadEvent;
import com.wujie.im.event.MessageRecalledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

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
    private ConversationService conversationService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private ReadStatusService readStatusService;

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
        String lastContent = extractPreviewContent(content);
        conversationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Conversation>()
                        .eq(Conversation::getId, conversationId)
                        .set(Conversation::getLastMessageId, msg.getId())
                        .set(Conversation::getLastMessageContent, lastContent)
                        .set(Conversation::getLastMessageTime, msg.getCreateTime())
        );

        // 发布消息发送事件，由 EventListener 处理 WebSocket 推送
        eventPublisher.publishEvent(new MessageSentEvent(this, msg, conv, senderId));
        return msg;
    }

    /**
     * 异步推送消息给会话成员（由 EventListener 调用）
     */
    public void pushMessageToConversationAsync(Conversation conv, Message msg, Long excludeUserId) {
        if (conv == null) return;

        if ("SINGLE".equals(conv.getType())) {
            Long otherUserId = conv.getTypeId();
            if (!otherUserId.equals(excludeUserId)) {
                conversationService.sendMessageToUser(otherUserId, msg);
            }
            conversationService.sendMessageToUser(excludeUserId, msg);
        } else if ("GROUP".equals(conv.getType())) {
            List<GroupMember> members = groupMemberMapper.selectList(
                    new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, conv.getTypeId())
            );
            for (GroupMember m : members) {
                Conversation memberConv = conversationService.getOrCreateGroupConversation(m.getUserId(), conv.getTypeId());
                Message pushMsg = new Message();
                pushMsg.setId(msg.getId());
                pushMsg.setConversationId(memberConv.getId());
                pushMsg.setSenderId(msg.getSenderId());
                pushMsg.setContent(msg.getContent());
                pushMsg.setContentType(msg.getContentType());
                pushMsg.setMeta(msg.getMeta());
                pushMsg.setStatus(msg.getStatus());
                pushMsg.setReplyId(msg.getReplyId());
                pushMsg.setCreateTime(msg.getCreateTime());
                pushMsg.setRecall(msg.getRecall());

                conversationService.sendMessageToUser(m.getUserId(), pushMsg);
            }
        }
    }

    public List<Message> getMessages(Long conversationId, Long beforeId, int limit) {
        // 限制 limit 范围，防止 SQL 注入
        int safeLimit = Math.min(Math.max(1, limit), 100);

        LambdaQueryWrapper<Message> q = new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .eq(Message::getRecall, 0)
                .orderByDesc(Message::getId)
                .last("LIMIT " + safeLimit);

        if (beforeId != null) {
            q.lt(Message::getId, beforeId);
        }

        List<Message> msgs = messageMapper.selectList(q);
        Collections.reverse(msgs);
        return msgs;
    }

    public List<Message> getGroupMessages(Long groupId, Long beforeId, int limit) {
        // 限制 limit 范围
        int safeLimit = Math.min(Math.max(1, limit), 100);

        // 查找该群所有成员的会话
        List<Conversation> convs = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getType, "GROUP")
                        .eq(Conversation::getTypeId, groupId)
        );
        if (convs.isEmpty()) return Collections.emptyList();

        // 收集所有 conversationId
        List<Long> convIds = convs.stream().map(Conversation::getId).toList();

        LambdaQueryWrapper<Message> q = new LambdaQueryWrapper<Message>()
                .in(Message::getConversationId, convIds)
                .eq(Message::getRecall, 0)
                .orderByDesc(Message::getId)
                .last("LIMIT " + safeLimit);

        if (beforeId != null) {
            q.lt(Message::getId, beforeId);
        }

        List<Message> msgs = messageMapper.selectList(q);
        Collections.reverse(msgs);
        return msgs;
    }

    public List<Message> getMessagesBetweenUsers(Long conversationId1, Long conversationId2, Long beforeId, int limit) {
        // 限制 limit 范围
        int safeLimit = Math.min(Math.max(1, limit), 100);

        LambdaQueryWrapper<Message> q = new LambdaQueryWrapper<Message>()
                .in(Message::getConversationId, List.of(conversationId1, conversationId2))
                .eq(Message::getRecall, 0)
                .orderByDesc(Message::getId)
                .last("LIMIT " + safeLimit);

        if (beforeId != null) {
            q.lt(Message::getId, beforeId);
        }

        List<Message> msgs = messageMapper.selectList(q);
        Collections.reverse(msgs);
        return msgs;
    }

    /**
     * 批量查询消息并填充发送者信息
     */
    public Map<Long, User> getSenderInfoForMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> senderIds = messages.stream()
                .map(Message::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (senderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userService.listByIds(senderIds);
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }
        q.last("LIMIT " + limit);
        List<Message> msgs = messageMapper.selectList(q);
        Collections.reverse(msgs);
        return msgs;
    }

    public List<Message> getGroupMessages(Long groupId, Long beforeId, int limit) {
        // 查找该群所有成员的会话
        List<Conversation> convs = conversationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getType, "GROUP")
                        .eq(Conversation::getTypeId, groupId)
        );
        if (convs.isEmpty()) return Collections.emptyList();

        // 收集所有conversationId
        List<Long> convIds = convs.stream().map(Conversation::getId).toList();

        LambdaQueryWrapper<Message> q = new LambdaQueryWrapper<Message>()
                .in(Message::getConversationId, convIds)
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

    public List<Message> getMessagesBetweenUsers(Long conversationId1, Long conversationId2, Long beforeId, int limit) {
        LambdaQueryWrapper<Message> q = new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getConversationId, conversationId1).or().eq(Message::getConversationId, conversationId2))
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
        Long lastReadId = readStatusService.getLastReadId(userId, conversationId);
        Map<String, Object> result = new HashMap<>();
        result.put("lastReadId", lastReadId);
        return result;
    }

    public void markAsRead(Long userId, Long conversationId, Long messageId) {
        readStatusService.markAsReadAsync(userId, conversationId, messageId);
        eventPublisher.publishEvent(new MessageReadEvent(this, userId, conversationId, messageId, userId));
    }

    public void recallMessage(Long userId, Long messageId) {
        Message msg = messageMapper.selectById(messageId);
        if (msg == null) return;
        if (!msg.getSenderId().equals(userId)) return;

        msg.setRecall(1);
        msg.setStatus("RECALLED");
        messageMapper.updateById(msg);

        // 发布撤回事件
        Conversation conv = conversationMapper.selectById(msg.getConversationId());
        if (conv != null) {
            eventPublisher.publishEvent(new MessageRecalledEvent(this, userId, messageId, conv));
        }
    }

    // 从消息内容中提取预览文本（支持图文混合内容的预览）
    private String extractPreviewContent(String content) {
        if (content == null) return null;
        // 如果是 JSON 数组，提取第一个文本块的内容
        if (content.startsWith("[")) {
            try {
                cn.hutool.json.JSONArray arr = JSONUtil.parseArray(content);
                for (Object item : arr) {
                    if (item instanceof cn.hutool.json.JSONObject) {
                        cn.hutool.json.JSONObject obj = (cn.hutool.json.JSONObject) item;
                        if ("text".equals(obj.getStr("type"))) {
                            String text = obj.getStr("content");
                            return text != null && text.length() > 50 ? text.substring(0, 50) : text;
                        }
                    }
                }
                // 没有文本块，返回 [图片] 等描述
                return "[图片消息]";
            } catch (Exception e) {
                // 解析失败，截断原始内容
            }
        }
        return content.length() > 50 ? content.substring(0, 50) : content;
    }
}
