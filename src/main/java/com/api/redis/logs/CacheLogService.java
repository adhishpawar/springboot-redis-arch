package com.api.redis.logs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CacheLogService {

    private static final String LOG_KEY = "CACHE_LOGS";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public void log(String message) {
        String entry = String.format("[%s] %s", OffsetDateTime.now().format(FMT), message);
        // Print to console
        System.out.println(entry);
        // store in Redis list
        try {
            redisTemplate.opsForList().leftPush(LOG_KEY, entry);

            redisTemplate.opsForList().trim(LOG_KEY, 0, 499);
        } catch (Exception e) {
            System.out.println("Failed to push cache log to redis: " + e.getMessage());
        }
    }
}

