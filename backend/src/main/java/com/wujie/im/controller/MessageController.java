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
    public Result<Message> sendMessage(@RequestBody Map<String, Object> params,
                                       jakarta.servlet.http.HttpServletRequest request) {
        if (params.get("conversationId") == null || params.get("content") == null) {
            return Result.error("参数错误");
        }
        String meta = params.get("meta") != null ? (String) params.get("meta") : null;
        Long replyId = params.get("replyId") != null ? Long.valueOf(params.get("replyId").toString()) : null;
        // 发送者身份以 token 为准，body 中的 senderId 仅作兼容忽略
        return Result.success(messageService.sendMessage(
                currentUserId(request),
                Long.valueOf(params.get("conversationId").toString()),
                (String) params.get("content"),
                (String) params.get("contentType"),
                meta,
                replyId
        ));
    }

    @GetMapping("/list/{conversationId}")
    public Result<List<Message>> getMessages(@PathVariable Long conversationId,
                                            @RequestParam(required = false) Long beforeId,
                                            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(messageService.getMessages(conversationId, beforeId, limit));
    }

    @GetMapping("/search")
    public Result<List<Message>> searchMessages(@RequestParam Long conversationId, @RequestParam String keyword) {
        return Result.success(messageService.searchMessages(conversationId, keyword));
    }

    @PutMapping("/read")
    public Result<Void> markAsRead(@RequestBody Map<String, Long> params,
                                    jakarta.servlet.http.HttpServletRequest request) {
        messageService.markAsRead(currentUserId(request), params.get("conversationId"), params.get("messageId"));
        return Result.success();
    }

    @PutMapping("/recall/{messageId}")
    public Result<Void> recallMessage(@PathVariable Long messageId, @RequestParam Long userId,
                                      jakarta.servlet.http.HttpServletRequest request) {
        messageService.recallMessage(currentUserId(request), messageId);
        return Result.success();
    }
    private Long currentUserId(jakarta.servlet.http.HttpServletRequest request) {
        return (Long) request.getAttribute(com.wujie.im.common.JwtAuthInterceptor.ATTR_USER_ID);
    }
}
