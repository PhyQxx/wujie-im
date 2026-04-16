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

    public List<Conversation> getConversations(Long userId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .orderByDesc(Conversation::getLastMessageTime)
        );
    }

    public Conversation getOrCreateSingleConversation(Long userId, Long otherUserId) {
        List<Conversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .eq(Conversation::getType, "SINGLE")
                        .eq(Conversation::getTypeId, otherUserId)
        );
        if (!list.isEmpty()) return list.get(0);
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
}
