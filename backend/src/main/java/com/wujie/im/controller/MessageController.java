package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.Message;
import com.wujie.im.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Result<Message> sendMessage(@RequestBody Map<String, Object> params) {
        return Result.success(messageService.sendMessage(
                Long.valueOf(params.get("senderId").toString()),
                Long.valueOf(params.get("conversationId").toString()),
                (String) params.get("content"),
                (String) params.get("contentType")
        ));
    }

    @GetMapping("/list/{conversationId}")
    public Result<List<Message>> getMessages(@PathVariable Long conversationId,
                                            @RequestParam(required = false) Long beforeId,
                                            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(messageService.getMessages(conversationId, beforeId, limit));
    }

    @PutMapping("/read")
    public Result<Void> markAsRead(@RequestBody Map<String, Long> params) {
        messageService.markAsRead(params.get("userId"), params.get("conversationId"), params.get("messageId"));
        return Result.success();
    }

    @PutMapping("/recall/{messageId}")
    public Result<Void> recallMessage(@PathVariable Long messageId, @RequestParam Long userId) {
        messageService.recallMessage(userId, messageId);
        return Result.success();
    }
}
