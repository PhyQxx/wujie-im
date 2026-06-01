package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.entity.Announcement;
import com.wujie.im.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;

    // ===== 管理员接口 =====

    @PostMapping("/admin/announcement")
    public Result<Announcement> create(@RequestBody Map<String, Object> params) {
        Announcement ann = new Announcement();
        ann.setTitle((String) params.get("title"));
        ann.setContent((String) params.get("content"));
        ann.setType((String) params.getOrDefault("type", "SYSTEM"));
        ann.setStatus(0);
        if (params.get("status") != null) {
            ann.setStatus(Integer.valueOf(params.get("status").toString()));
        }
        if (ann.getStatus() == 1) {
            ann.setPublishTime(LocalDateTime.now());
            ann.setPublisherId(params.get("publisherId") != null ? Long.valueOf(params.get("publisherId").toString()) : null);
        }
        announcementService.create(ann);
        return Result.success(ann);
    }

    @PutMapping("/admin/announcement/{id}")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Announcement ann = announcementService.listAll().stream()
                .filter(a -> a.getId().equals(id)).findFirst().orElse(null);
        if (ann == null) {
            return Result.error("公告不存在");
        }
        if (params.get("title") != null) ann.setTitle((String) params.get("title"));
        if (params.get("content") != null) ann.setContent((String) params.get("content"));
        if (params.get("type") != null) ann.setType((String) params.get("type"));
        announcementService.update(ann);
        return Result.success(ann);
    }

    @DeleteMapping("/admin/announcement/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success();
    }

    @GetMapping("/admin/announcement/list")
    public Result<List<Announcement>> listAll() {
        return Result.success(announcementService.listAll());
    }

    @PostMapping("/admin/announcement/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Long publisherId = params.get("publisherId") != null ? Long.valueOf(params.get("publisherId").toString()) : null;
        announcementService.publish(id, publisherId);
        return Result.success();
    }

    @PostMapping("/admin/announcement/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        announcementService.unpublish(id);
        return Result.success();
    }

    // ===== 用户接口 =====

    @GetMapping("/announcement/list")
    public Result<List<Announcement>> listPublished(@RequestParam Long userId) {
        return Result.success(announcementService.listPublished(userId));
    }

    @GetMapping("/announcement/unread")
    public Result<List<Announcement>> getUnread(@RequestParam Long userId) {
        return Result.success(announcementService.getUnread(userId));
    }

    @PostMapping("/announcement/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, @RequestParam Long userId) {
        announcementService.markRead(id, userId);
        return Result.success();
    }
}
