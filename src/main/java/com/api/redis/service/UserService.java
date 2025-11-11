package com.api.redis.service;

import com.api.redis.logs.CacheLogService;
import com.api.redis.models.User;
import com.api.redis.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CacheLogService cacheLogService;

    // Create user (also optionally cache upon creation)
    public User createUser(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        user.setUpdatedAt(OffsetDateTime.now());
        User saved = userRepository.save(user);
        // Do not force cache here — we prefer lazy load via GET endpoint
        cacheLogService.log("Created user " + saved.getId());
        return saved;
    }

    // GET with cache-aside + TTL: if cached return; else load from DB and store in cache.
    public Optional<User> getUserWithCache(String userId) {
        Object cached = cacheService.getUserFromCache(userId);
        if (cached != null && cached instanceof User) {
            cacheLogService.log("Cache HIT for user " + userId);
            return Optional.of((User) cached);
        }
        cacheLogService.log("Cache MISS for user " + userId + ". Loading from DB.");
        Optional<User> dbUser = userRepository.findById(userId);
        dbUser.ifPresent(user -> {
            cacheService.saveUserToCache(userId, user);
            cacheLogService.log("Saved user " + userId + " into cache with TTL " + CacheService.CACHE_TTL_SECONDS + "s");
        });
        return dbUser;
    }

    // Update via PUT (full replace) - invalidate cache only (do not reload). Also returns updated entity
    public Optional<User> updateUserPut(String userId, User newUser) {
        return userRepository.findById(userId).map(existing -> {
            // Replace fields (full update)
            existing.setName(newUser.getName());
            existing.setEmail(newUser.getEmail());
            existing.setRole(newUser.getRole());
            existing.setUpdatedAt(OffsetDateTime.now());
            User saved = userRepository.save(existing);
            // Clear cache for this user (do NOT reload)
            cacheService.deleteUserCache(userId);
            cacheLogService.log("Cache cleared for user " + userId + " due to PUT update");
            return saved;
        });
    }

    // Partial update (PATCH) example - also clear cache
    public Optional<User> updateUserPatch(String userId, User patch) {
        return userRepository.findById(userId).map(existing -> {
            if (patch.getName() != null) existing.setName(patch.getName());
            if (patch.getEmail() != null) existing.setEmail(patch.getEmail());
            if (patch.getRole() != null) existing.setRole(patch.getRole());
            existing.setUpdatedAt(OffsetDateTime.now());
            User saved = userRepository.save(existing);
            cacheService.deleteUserCache(userId);
            cacheLogService.log("Cache cleared for user " + userId + " due to PATCH update");
            return saved;
        });
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
        cacheService.deleteUserCache(userId);
        cacheLogService.log("User " + userId + " deleted from DB and cache cleared");
    }

    // used by scheduler to find recently updated users
    public List<User> findUsersUpdatedAfter(OffsetDateTime since) {
        return userRepository.findUsersUpdatedAfter(since);
    }
}
