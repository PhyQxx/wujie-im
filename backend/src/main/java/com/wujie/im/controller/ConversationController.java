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
    public Result<Conversation> createSingleConversation(@RequestBody Map<String, Long> params) {
        return Result.success(conversationService.getOrCreateSingleConversation(
                params.get("userId"), params.get("otherUserId")
        ));
    }

    @GetMapping("/{id}/messages")
    public Result<List<Message>> getMessages(
            @PathVariable Long id,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "50") int limit) {
        List<Message> messages = messageService.getMessages(id, beforeId, limit);
        messages.forEach(m -> m.setContent(null)); // 内容已在列表显示，省略
        return Result.success(messages);
    }
}
