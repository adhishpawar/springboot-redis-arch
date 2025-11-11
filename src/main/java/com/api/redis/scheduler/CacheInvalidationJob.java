package com.api.redis.scheduler;

import com.api.redis.logs.CacheLogService;
import com.api.redis.models.User;
import com.api.redis.service.CacheService;
import com.api.redis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.util.List;
import jakarta.annotation.PostConstruct;

@Component
public class CacheInvalidationJob {

    private volatile OffsetDateTime lastCheck;

    @Autowired
    private UserService userService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CacheLogService cacheLogService;

    @PostConstruct
    public void init() {
        lastCheck = OffsetDateTime.now().minusMinutes(1); // initial window
    }

    // run every 60 seconds
    @Scheduled(fixedRate = 60000)
    public void checkForUpdates() {
        try {
            OffsetDateTime now = OffsetDateTime.now();
            List<User> changed = userService.findUsersUpdatedAfter(lastCheck);
            if (changed != null && !changed.isEmpty()) {
                for (User u : changed) {
                    String userId = u.getId();
                    cacheService.deleteUserCache(userId);
                    cacheLogService.log("Cache cleared for user " + userId + " due to DB update (cron job).");
                }
            } else {
                cacheLogService.log("Cron job: No user updates found since " + lastCheck);
            }
            lastCheck = now;
        } catch (Exception e) {
            cacheLogService.log("Cron job error: " + e.getMessage());
        }
    }
}
