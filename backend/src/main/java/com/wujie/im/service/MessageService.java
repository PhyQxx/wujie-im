package com.wujie.im.service;

import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.*;
import com.wujie.im.mapper.*;
import com.wujie.im.websocket.WsHandler;
import com.wujie.im.event.MessageSentEvent;
import com.wujie.im.event.MessageRecalledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private MessageReadMapper messageReadMapper;
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    @Autowired
    private WsHandler wsHandler;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserProfileMapper userProfileMapper;
    @Autowired
    @org.springframework.context.annotation.Lazy
    private RobotMessageHandler robotMessageHandler;
    @Autowired
    private com.wujie.im.util.SensitiveWordFilter sensitiveWordFilter;

    public Message sendMessage(Long senderId, Long conversationId, String content, String contentType) {
        // 敏感词过滤
        if ("TEXT".equals(contentType) || contentType == null) {
            content = sensitiveWordFilter.filter(content);
        }
        return sendMessage(senderId, conversationId, content, contentType, null, null);
    }

    public Message sendMessage(Long senderId, Long conversationId, String content, String contentType, String metaStr) {
        return sendMessage(senderId, conversationId, content, contentType, metaStr, null);
    }

    @Transactional
    public Message sendMessage(Long senderId, Long conversationId, String content, String contentType, String metaStr, Long replyId) {
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setContentType(contentType != null ? contentType : "TEXT");
        msg.setMeta(metaStr);
        msg.setStatus("SENT");
        msg.setReplyId(replyId);
        msg.setRecall(0);
        messageMapper.insert(msg);

        // 更新会话最后一条消息
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv != null) {
            conv.setLastMessageId(msg.getId());
            conv.setLastMessageContent(extractPreviewContent(content));
            conv.setLastMessageTime(msg.getCreateTime());
            conversationMapper.updateById(conv);

            // 清零发送者的未读数
            markAsRead(senderId, conversationId, msg.getId());

            // 处理推送 (通过事件解耦)
            eventPublisher.publishEvent(new MessageSentEvent(this, msg, conv, senderId));
        }

        return msg;
    }

    @Async
    public void pushMessageToConversationAsync(Conversation conv, Message msg, Long excludeUserId) {
        pushMessageToConversation(conv, msg, excludeUserId);
    }

    private void pushMessageToConversation(Conversation conv, Message msg, Long excludeUserId) {
        if (conv == null) return;

        // 获取发送者名称用于通知
        String senderName = null;
        User senderUser = userMapper.selectById(msg.getSenderId());
        if (senderUser != null) {
            senderName = senderUser.getUsername();
            // 优先使用昵称
            UserProfile profile = userProfileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, senderUser.getId())
            );
            if (profile != null && profile.getNickname() != null) {
                senderName = profile.getNickname();
            }
        }

        if ("SINGLE".equals(conv.getType())) {
            Long otherUserId = conv.getTypeId();
            if (!otherUserId.equals(excludeUserId)) {
                Conversation otherConv = conversationService.getOrCreateSingleConversation(otherUserId, excludeUserId);
                incrementUnreadCount(otherUserId, otherConv.getId());
                
                Message pushMsg = createPushMessage(msg, otherConv.getId(), senderName);
                wsHandler.sendToUser(otherUserId, "message", pushMsg);

                // 检测是否为机器人
                User otherUser = userMapper.selectById(otherUserId);
                if (otherUser != null && "ROBOT".equals(otherUser.getRole())) {
                    robotMessageHandler.handlePrivateChat(otherUserId, excludeUserId, msg.getContent());
                }
            }
            msg.setSenderName(senderName);
            wsHandler.sendToUser(excludeUserId, "message", msg);
        } else if ("GROUP".equals(conv.getType())) {
            List<GroupMember> members = groupMemberMapper.selectList(
                    new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, conv.getTypeId())
            );
            for (GroupMember m : members) {
                Conversation memberConv = conversationService.getOrCreateGroupConversation(m.getUserId(), conv.getTypeId());
                Message pushMsg = createPushMessage(msg, memberConv.getId(), senderName);

                if (!m.getUserId().equals(excludeUserId)) {
                    incrementUnreadCount(m.getUserId(), memberConv.getId());
                    wsHandler.sendToUser(m.getUserId(), "message", pushMsg);
                    checkAndPushMention(m.getUserId(), msg, senderName);
                } else {
                    wsHandler.sendToUser(excludeUserId, "message", pushMsg);
                }
            }

            // 检测机器人自动回复
            for (GroupMember m : members) {
                if (m.getUserId().equals(excludeUserId)) continue;
                User memberUser = userMapper.selectById(m.getUserId());
                if (memberUser != null && "ROBOT".equals(memberUser.getRole())) {
                    boolean isMentioned = isUserMentioned(msg, m.getUserId());
                    robotMessageHandler.handleGroupChat(m.getUserId(), conv.getTypeId(), excludeUserId, msg.getContent(), isMentioned);
                }
            }
        }
    }

    private Message createPushMessage(Message msg, Long conversationId, String senderName) {
        Message pushMsg = new Message();
        pushMsg.setId(msg.getId());
        pushMsg.setConversationId(conversationId);
        pushMsg.setSenderId(msg.getSenderId());
        pushMsg.setSenderName(senderName);
        pushMsg.setContent(msg.getContent());
        pushMsg.setContentType(msg.getContentType());
        pushMsg.setMeta(msg.getMeta());
        pushMsg.setStatus(msg.getStatus());
        pushMsg.setReplyId(msg.getReplyId());
        pushMsg.setCreateTime(msg.getCreateTime());
        pushMsg.setRecall(msg.getRecall());
        return pushMsg;
    }

    private boolean isUserMentioned(Message msg, Long userId) {
        if (msg.getMeta() == null) return false;
        try {
            JSONObject meta = JSONUtil.parseObj(msg.getMeta());
            if (meta.getBool("atAll", false)) return true;
            List<Long> atUserIds = JSONUtil.toList(meta.getJSONArray("atUserIds"), Long.class);
            return atUserIds != null && atUserIds.contains(userId);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void checkAndPushMention(Long targetUserId, Message msg, String senderName) {
        if (isUserMentioned(msg, targetUserId)) {
            Map<String, Object> atNotif = new HashMap<>();
            atNotif.put("type", "mention");
            atNotif.put("data", Map.of(
                "messageId", msg.getId(),
                "conversationId", msg.getConversationId(),
                "senderName", senderName != null ? senderName : "有人",
                "content", msg.getContent()
            ));
            wsHandler.sendToUser(targetUserId, JSONUtil.toJsonStr(atNotif));
        }
    }

    private void incrementUnreadCount(Long userId, Long conversationId) {
        conversationMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .eq(Conversation::getId, conversationId)
                        .setSql("unread_count = IFNULL(unread_count, 0) + 1")
        );
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

    public List<Message> getGroupMessages(Long groupId, Long beforeId, int limit) {
        List<Conversation> convs = conversationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getType, "GROUP")
                        .eq(Conversation::getTypeId, groupId)
        );
        if (convs.isEmpty()) return Collections.emptyList();
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

    public List<Message> searchMessages(Long conversationId, String keyword) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .like(Message::getContent, keyword)
                        .eq(Message::getRecall, 0)
                        .orderByDesc(Message::getId)
        );
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
        Conversation conv = conversationMapper.selectOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getId, conversationId)
                        .eq(Conversation::getUserId, userId)
        );
        if (conv != null && conv.getUnreadCount() != null && conv.getUnreadCount() > 0) {
            conv.setUnreadCount(0);
            conversationMapper.updateById(conv);
        }
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
        if (msg == null) throw new RuntimeException("消息不存在");
        
        boolean canRecall = false;
        if (msg.getSenderId().equals(userId)) {
            if (msg.getCreateTime().plusMinutes(2).isAfter(LocalDateTime.now())) {
                canRecall = true;
            } else {
                throw new RuntimeException("消息发送已超过2分钟，无法撤回");
            }
        } else {
            Conversation conv = conversationMapper.selectById(msg.getConversationId());
            if (conv != null && "GROUP".equals(conv.getType())) {
                GroupMember member = groupMemberMapper.selectOne(
                        new LambdaQueryWrapper<GroupMember>()
                                .eq(GroupMember::getGroupId, conv.getTypeId())
                                .eq(GroupMember::getUserId, userId)
                );
                if (member != null && ("OWNER".equals(member.getRole()) || "ADMIN".equals(member.getRole()))) {
                    canRecall = true;
                }
            }
        }

        if (!canRecall) throw new RuntimeException("无权撤回该消息");

        msg.setRecall(1);
        msg.setStatus("RECALLED");
        msg.setContent("消息已撤回");
        messageMapper.updateById(msg);

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

    private String extractPreviewContent(String content) {
        if (content == null) return null;
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
                return "[图片消息]";
            } catch (Exception e) { /* ignored */ }
        }
        return content.length() > 50 ? content.substring(0, 50) : content;
    }
}
