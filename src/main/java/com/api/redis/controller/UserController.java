package com.api.redis.controller;

import com.api.redis.models.User;
import com.api.redis.service.UserService;
import com.api.redis.logs.CacheLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CacheLogService cacheLogService;

    // Create user (POST /users)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.ok(created);
    }

    // GET /users/{id}  (original DB read w/o caching)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") String id) {
        Optional<User> u = userService.getUserWithCache(id); // you can choose to use non-cache version
        return u.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET with explicit cache-aside endpoint (per your design)
    @GetMapping("/cache/{id}")
    public ResponseEntity<User> getUserWithCache(@PathVariable("id") String id) {
        Optional<User> u = userService.getUserWithCache(id);
        return u.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET all users (from DB)
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        // For simplicity return DB list (avoid caching large list)
        List<User> all = userService.findUsersUpdatedAfter(java.time.OffsetDateTime.MIN); // or userRepository.findAll()
        return ResponseEntity.ok(all);
    }

    // PUT /users/{id} full update - invalidates cache and does NOT reload cache
    @PutMapping("/{id}")
    public ResponseEntity<User> putUpdateUser(@PathVariable("id") String id, @RequestBody User user) {
        Optional<User> updated = userService.updateUserPut(id, user);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PATCH /users/{id} partial update - invalidates cache and does NOT reload cache
    @PatchMapping("/{id}")
    public ResponseEntity<User> patchUpdateUser(@PathVariable("id") String id, @RequestBody User patch) {
        Optional<User> updated = userService.updateUserPatch(id, patch);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Expose cache logs
    @GetMapping("/logs")
    public ResponseEntity<String> getCacheLogs() {
        // This just returns a small message; retrieving complete logs via redis-cli or another endpoint can be added
        return ResponseEntity.ok("Check Redis list key: CACHE_LOGS or tail console logs.");
    }
}
