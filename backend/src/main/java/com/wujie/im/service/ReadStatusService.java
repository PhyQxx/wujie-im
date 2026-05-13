package com.wujie.im.service;

import com.wujie.im.entity.MessageRead;
import com.wujie.im.mapper.MessageReadMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReadStatusService {
    private static final String READ_STATUS_KEY = "im:read:status";

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private MessageReadMapper messageReadMapper;

    /**
     * 获取已读状态（优先从 Redis 缓存读取）
     */
    public Long getLastReadId(Long userId, Long conversationId) {
        RMapCache<String, Long> mapCache = redissonClient.getMapCache(READ_STATUS_KEY);
        String field = userId + ":" + conversationId;
        Long cached = mapCache.get(field);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，从数据库读取
        MessageRead read = messageReadMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MessageRead>()
                        .eq(MessageRead::getUserId, userId)
                        .eq(MessageRead::getConversationId, conversationId)
        );
        if (read != null) {
            mapCache.put(field, read.getLastReadId(), 24, TimeUnit.HOURS);
            return read.getLastReadId();
        }
        return 0L;
    }

    /**
     * 更新已读状态（写入 Redis 缓存，定时批量同步到数据库）
     */
    public void markAsReadAsync(Long userId, Long conversationId, Long messageId) {
        RMapCache<String, Long> mapCache = redissonClient.getMapCache(READ_STATUS_KEY);
        String field = userId + ":" + conversationId;
        Long current = mapCache.getOrDefault(field, 0L);
        if (messageId > current) {
            mapCache.put(field, messageId, 24, TimeUnit.HOURS);
        }
    }

    /**
     * 定时同步 Redis 已读状态到 MySQL（每 5 分钟）
     */
    @Scheduled(fixedRate = 300000)
    public synchronized void flushToDatabase() {
        RMapCache<String, Long> mapCache = redissonClient.getMapCache(READ_STATUS_KEY);
        if (mapCache.isEmpty()) {
            return;
        }

        log.info("开始同步已读状态到数据库, 缓存数量: {}", mapCache.size());
        int count = 0;

        for (var entry : mapCache.entrySet()) {
            String key = entry.getKey();
            Long messageId = entry.getValue();
            String[] parts = key.split(":");
            if (parts.length != 2) continue;

            Long userId = Long.parseLong(parts[0]);
            Long conversationId = Long.parseLong(parts[1]);

            MessageRead read = messageReadMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MessageRead>()
                            .eq(MessageRead::getUserId, userId)
                            .eq(MessageRead::getConversationId, conversationId)
            );

            if (read == null) {
                read = new MessageRead();
                read.setUserId(userId);
                read.setConversationId(conversationId);
                read.setLastReadId(messageId);
                messageReadMapper.insert(read);
            } else if (read.getLastReadId() < messageId) {
                read.setLastReadId(messageId);
                messageReadMapper.updateById(read);
            }
            count++;
        }

        log.info("已读状态同步完成, 处理 {} 条记录", count);
    }
}
