package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.Conversation;
import com.wujie.im.entity.GroupInfo;
import com.wujie.im.entity.Message;
import com.wujie.im.entity.User;
import com.wujie.im.mapper.GroupInfoMapper;
import com.wujie.im.mapper.UserMapper;
import com.wujie.im.service.ConversationService;
import com.wujie.im.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GroupInfoMapper groupInfoMapper;

    @GetMapping("/list/{userId}")
    public Result<List<Conversation>> getConversations(@PathVariable Long userId) {
        List<Conversation> list = conversationService.getConversations(userId);
        // 填充目标用户/群组信息
        for (Conversation conv : list) {
            if ("SINGLE".equals(conv.getType())) {
                User target = userMapper.selectById(conv.getTypeId());
                if (target != null) target.setPassword(null);
                conv.setTargetUser(target);
            } else {
                GroupInfo group = groupInfoMapper.selectById(conv.getTypeId());
                conv.setGroupInfo(group);
            }
        }
        return Result.success(list);
    }

    @PostMapping("/single")
    public Result<Conversation> createSingleConversation(@RequestBody Map<String, Object> params) {
        return Result.success(conversationService.getOrCreateSingleConversation(
                toLong(params.get("userId")), toLong(params.get("otherUserId"))
        ));
    }

    @PostMapping("/group")
    public Result<Conversation> createGroupConversation(@RequestBody Map<String, Object> params) {
        Conversation conv = conversationService.getOrCreateGroupConversation(
                toLong(params.get("userId")), toLong(params.get("groupId"))
        );
        GroupInfo group = groupInfoMapper.selectById(conv.getTypeId());
        conv.setGroupInfo(group);
        return Result.success(conv);
    }

    @GetMapping("/{id}/messages")
    public Result<List<Message>> getMessages(
            @PathVariable Long id,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "50") int limit) {
        Conversation conv = conversationService.getConversationById(id);
        List<Message> messages;
        if (conv != null && "SINGLE".equals(conv.getType())) {
            // 单聊：双方共享 typeId 指向对方的会话，消息分布在各自的 conversationId 下
            Long otherUserId = conv.getTypeId();
            Conversation otherConv = conversationService.getConversationByTypeId(otherUserId);
            if (otherConv != null) {
                messages = messageService.getMessagesBetweenUsers(id, otherConv.getId(), beforeId, limit);
            } else {
                messages = messageService.getMessages(id, beforeId, limit);
            }
        } else if (conv != null && "GROUP".equals(conv.getType())) {
            // 群聊：查询所有群成员会话中的消息
            messages = messageService.getGroupMessages(conv.getTypeId(), beforeId, limit);
        } else {
            messages = messageService.getMessages(id, beforeId, limit);
        }
        return Result.success(messages);
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        return Long.parseLong(val.toString());
    }
}
