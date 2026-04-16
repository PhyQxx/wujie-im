package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.Conversation;
import com.wujie.im.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    @Autowired
    private ConversationService conversationService;

    @GetMapping("/list/{userId}")
    public Result<List<Conversation>> getConversations(@PathVariable Long userId) {
        return Result.success(conversationService.getConversations(userId));
    }

    @PostMapping("/single")
    public Result<Conversation> createSingleConversation(@RequestBody Map<String, Long> params) {
        return Result.success(conversationService.getOrCreateSingleConversation(
                params.get("userId"), params.get("otherUserId")
        ));
    }
}
