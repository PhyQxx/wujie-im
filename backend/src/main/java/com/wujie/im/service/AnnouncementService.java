package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.Announcement;
import com.wujie.im.entity.AnnouncementRead;
import com.wujie.im.mapper.AnnouncementMapper;
import com.wujie.im.mapper.AnnouncementReadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AnnouncementService {
    @Autowired
    private AnnouncementMapper announcementMapper;
    @Autowired
    private AnnouncementReadMapper announcementReadMapper;

    // ===== 管理员操作 =====

    public Announcement create(Announcement announcement) {
        announcementMapper.insert(announcement);
        return announcement;
    }

    public Announcement update(Announcement announcement) {
        announcementMapper.updateById(announcement);
        return announcement;
    }

    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }

    public List<Announcement> listAll() {
        return announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .orderByDesc(Announcement::getCreateTime)
        );
    }

    public void publish(Long id, Long publisherId) {
        Announcement ann = announcementMapper.selectById(id);
        if (ann != null) {
            ann.setStatus(1);
            ann.setPublisherId(publisherId);
            ann.setPublishTime(LocalDateTime.now());
            announcementMapper.updateById(ann);
        }
    }

    public void unpublish(Long id) {
        Announcement ann = announcementMapper.selectById(id);
        if (ann != null) {
            ann.setStatus(0);
            announcementMapper.updateById(ann);
        }
    }

    // ===== 用户操作 =====

    public List<Announcement> listPublished(Long userId) {
        List<Announcement> announcements = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getStatus, 1)
                        .orderByDesc(Announcement::getPublishTime)
        );
        List<AnnouncementRead> reads = announcementReadMapper.selectList(
                new LambdaQueryWrapper<AnnouncementRead>()
                        .eq(AnnouncementRead::getUserId, userId)
        );
        List<Long> readIds = new ArrayList<>();
        for (AnnouncementRead read : reads) {
            readIds.add(read.getAnnouncementId());
        }
        for (Announcement ann : announcements) {
            ann.setIsRead(readIds.contains(ann.getId()));
        }
        return announcements;
    }

    public List<Announcement> getUnread(Long userId) {
        List<Announcement> published = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getStatus, 1)
                        .orderByDesc(Announcement::getPublishTime)
        );
        List<AnnouncementRead> reads = announcementReadMapper.selectList(
                new LambdaQueryWrapper<AnnouncementRead>()
                        .eq(AnnouncementRead::getUserId, userId)
        );
        List<Long> readIds = new ArrayList<>();
        for (AnnouncementRead read : reads) {
            readIds.add(read.getAnnouncementId());
        }
        List<Announcement> unread = new ArrayList<>();
        for (Announcement ann : published) {
            if (!readIds.contains(ann.getId())) {
                ann.setIsRead(false);
                unread.add(ann);
            }
        }
        return unread;
    }

    public void markRead(Long announcementId, Long userId) {
        Long count = announcementReadMapper.selectCount(
                new LambdaQueryWrapper<AnnouncementRead>()
                        .eq(AnnouncementRead::getAnnouncementId, announcementId)
                        .eq(AnnouncementRead::getUserId, userId)
        );
        if (count == 0) {
            AnnouncementRead read = new AnnouncementRead();
            read.setAnnouncementId(announcementId);
            read.setUserId(userId);
            read.setReadTime(LocalDateTime.now());
            announcementReadMapper.insert(read);
        }
    }
}
