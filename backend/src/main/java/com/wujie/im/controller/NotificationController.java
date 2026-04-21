package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.Notification;
import com.wujie.im.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list/{userId}")
    public Result<List<Notification>> getNotifications(@PathVariable Long userId) {
        return Result.success(notificationService.getNotifications(userId));
    }

    @GetMapping("/unread/{userId}")
    public Result<List<Notification>> getUnread(@PathVariable Long userId) {
        return Result.success(notificationService.getUnread(userId));
    }

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PostMapping("/send")
    public Result<Void> sendNotification(@RequestBody Map<String, Object> params) {
        notificationService.sendNotification(
                Long.valueOf(params.get("userId").toString()),
                (String) params.get("type"),
                (String) params.get("title"),
                (String) params.get("content"),
                params.get("sourceId") != null ? Long.valueOf(params.get("sourceId").toString()) : null
        );
        return Result.success();
    }
}
