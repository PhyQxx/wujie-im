package com.wujie.im.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OnlineUserService {
    private static final String ONLINE_KEY_PREFIX = "im:online:";
    private static final String SESSION_KEY_PREFIX = "im:session:";
    private static final long EXPIRE_MINUTES = 30;

    @Autowired
    private RedissonClient redissonClient;

    public void setOnline(Long userId, String serverId) {
        String onlineKey = ONLINE_KEY_PREFIX + userId;
        RBucket<String> bucket = redissonClient.getBucket(onlineKey);
        bucket.set(serverId, EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.debug("User {} online on server {}", userId, serverId);
    }

    public void setOffline(Long userId) {
        String onlineKey = ONLINE_KEY_PREFIX + userId;
        redissonClient.getBucket(onlineKey).delete();
        log.debug("User {} offline", userId);
    }

    public boolean isOnline(Long userId) {
        String onlineKey = ONLINE_KEY_PREFIX + userId;
        RBucket<String> bucket = redissonClient.getBucket(onlineKey);
        return bucket.isExists();
    }

    public String getServerId(Long userId) {
        String onlineKey = ONLINE_KEY_PREFIX + userId;
        RBucket<String> bucket = redissonClient.getBucket(onlineKey);
        return bucket.get();
    }

    public void refreshOnline(Long userId) {
        String onlineKey = ONLINE_KEY_PREFIX + userId;
        RBucket<String> bucket = redissonClient.getBucket(onlineKey);
        if (bucket.isExists()) {
            bucket.expire(EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }
}
