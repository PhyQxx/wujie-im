package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.Conversation;
import com.wujie.im.mapper.ConversationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class ConversationService {
    @Autowired
    private ConversationMapper conversationMapper;

    public Conversation getConversationById(Long conversationId) {
        return conversationMapper.selectById(conversationId);
    }

    public Conversation getConversationByTypeId(Long typeId) {
        List<Conversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, typeId)
                        .eq(Conversation::getType, "SINGLE")
                        .last("LIMIT 1")
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Conversation> getConversations(Long userId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .orderByDesc(Conversation::getLastMessageTime)
        );
    }

    public Conversation getOrCreateSingleConversation(Long userId, Long otherUserId) {
        // 双向查找：先查自己创建的，再查对方创建的（防止重复创建）
        List<Conversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .eq(Conversation::getType, "SINGLE")
                        .eq(Conversation::getTypeId, otherUserId)
        );
        if (!list.isEmpty()) return list.get(0);
        // 对方是否已创建过会话？查找 typeId 指向自己的会话
        List<Conversation> reverseList = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, otherUserId)
                        .eq(Conversation::getType, "SINGLE")
                        .eq(Conversation::getTypeId, userId)
        );
        if (!reverseList.isEmpty()) return reverseList.get(0);
        Conversation conv = new Conversation();
        conv.setType("SINGLE");
        conv.setTypeId(otherUserId);
        conv.setUserId(userId);
        conv.setUnreadCount(0);
        conversationMapper.insert(conv);
        return conv;
    }

    public void updateLastMessage(Long conversationId, Long messageId, String content) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv != null) {
            conv.setLastMessageId(messageId);
            conv.setLastMessageContent(content.length() > 500 ? content.substring(0, 500) : content);
            conv.setLastMessageTime(java.time.LocalDateTime.now());
            conversationMapper.updateById(conv);
        }
    }

    public Conversation getOrCreateGroupConversation(Long userId, Long groupId) {
        // 查找当前用户是否有该群的会话（每个用户都有自己的群会话记录）
        List<Conversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .eq(Conversation::getType, "GROUP")
                        .eq(Conversation::getTypeId, groupId)
        );
        if (!list.isEmpty()) return list.get(0);
        // 不存在则创建
        Conversation conv = new Conversation();
        conv.setType("GROUP");
        conv.setTypeId(groupId);
        conv.setUserId(userId);
        conv.setUnreadCount(0);
        conversationMapper.insert(conv);
        return conv;
    }

    public Conversation getGroupConversation(Long userId, Long groupId) {
        List<Conversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .eq(Conversation::getType, "GROUP")
                        .eq(Conversation::getTypeId, groupId)
        );
        return list.isEmpty() ? null : list.get(0);
    }
}
