package com.api.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // TTL seconds (1 minute)
    public static final long CACHE_TTL_SECONDS = 60L;

    private String userKey(String userId) {
        return "USER:" + userId;
    }

    public void saveUserToCache(String userId, Object user) {
        String key = userKey(userId);
        redisTemplate.opsForValue().set(key, user, Duration.ofSeconds(CACHE_TTL_SECONDS));
    }

    public Object getUserFromCache(String userId) {
        return redisTemplate.opsForValue().get(userKey(userId));
    }

    public void deleteUserCache(String userId) {
        redisTemplate.delete(userKey(userId));
    }

    // For admin debug: clear all user keys (dangerous)
    public void deleteAllUserCaches() {
        // this implementation uses keys() - avoid in production with many keys
        try {
            for (String key : redisTemplate.keys("USER:*")) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            // keys may not be allowed on cluster; handle accordingly   -->GPT gen
        }
    }
}
