package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.Notification;
import com.wujie.im.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    public void sendNotification(Long userId, String type, String title, String content, Long sourceId) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(type);
        notif.setTitle(title);
        notif.setContent(content);
        notif.setSourceId(sourceId);
        notif.setIsRead(0);
        notificationMapper.insert(notif);
    }

    public List<Notification> getNotifications(Long userId) {
        return notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime)
        );
    }

    public List<Notification> getUnread(Long userId) {
        return notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
                        .orderByDesc(Notification::getCreateTime)
        );
    }

    public void markAsRead(Long notificationId) {
        Notification notif = notificationMapper.selectById(notificationId);
        if (notif != null) {
            notif.setIsRead(1);
            notificationMapper.updateById(notif);
        }
    }
}
