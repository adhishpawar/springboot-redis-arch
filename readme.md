
# 🚀 Spring Boot + PostgreSQL + Redis Cache-Aside System (Advanced CRUD + TTL + LRU + Invalidation)

## 🧩 **Overview**

This project extends the basic Redis CRUD architecture into a **FinTech-grade cache-aside system** using:

- ✅ PostgreSQL (Source of Truth)
- ✅ Redis (High‑speed Cache)
- ✅ Automatic Cache Invalidation
- ✅ TTL-based caching
- ✅ Redis LRU Eviction
- ✅ Background Scheduler (1‑minute DB change detector)
- ✅ CRUD operations with logs
- ✅ Cache logs stored in Redis LIST

This system ensures **data accuracy**, **performance**, and **cache freshness**.

---

# 🏗 **System Architecture**

```
Client → Controller → Service → DAO → PostgreSQL
                               ↘→ Redis Cache (TTL, LRU)
```

### ✔ *Cache-aside pattern* implemented:
- Read: check cache → if MISS → load from DB → save to cache
- Write: update DB → delete cache key
- Scheduler: detect DB changes every 1 minute → clear stale cache
- Redis eviction: allkeys-lru (server configured)

---

# 📦 **Technologies**
- Spring Boot 3.x
- Java 17
- Spring Data Redis + Lettuce
- PostgreSQL
- Redis Server (with LRU eviction)
- Maven
- Scheduler (Spring @Scheduled)

---

# 🧱 **Project Structure**

```
src/main/java/com/api/redis/
│
├── config/
│   └── RedisConfig.java
│
├── controller/
│   └── UserController.java
│
├── service/
│   └── UserService.java
│
├── dao/
│   ├── UserDao.java (PostgreSQL)
│   ├── RedisCacheDao.java (Redis)
│
├── scheduler/
│   └── CacheInvalidationScheduler.java
│
└── models/
    └── User.java
```

---

# ⚙️ **Redis Configuration (LRU Enabled)**

```java
@PostConstruct
public void attemptSetLRUPolicy() {
    try {
        RedisConnection conn = redisConnectionFactory().getConnection();
        conn.setConfig("maxmemory-policy", "allkeys-lru");
        System.out.println("✅ Redis eviction policy set to: allkeys-lru");
    } catch (Exception ex) {
        System.out.println("⚠ Could not apply Redis LRU eviction policy.");
    }
}
```

---

# 🔥 **Key Features**

## 1️⃣ GET /users/cache/{id} (Cache‑Aside Fetch)
```
Cache HIT  → return from Redis  
Cache MISS → load from DB → store in Redis → return  
TTL = 60 sec
```

---

## 2️⃣ POST /users (Create User)
- Creates DB record
- Does NOT store in cache
- Log added: `"USER_CREATED"`

---

## 3️⃣ PUT /users/{id} (Update User)
- Update DB
- Delete Redis cache key
- Add log: `"CACHE_INVALIDATED_BY_PUT"`

---

## 4️⃣ PATCH /users/{id} (Partial Update)
- Same as PUT
- Invalidates cache
- Does NOT repopulate Redis

---

## 5️⃣ 1‑Minute Background Scheduler
```
Every 60s:
  - Check DB rows updated in last minute
  - Delete Redis cache key
  - Push log entry
```

---

## 6️⃣ Redis LRU Eviction
When Redis memory is full:
```
Least Recently Used key is automatically removed.
```

---

## 7️⃣ Redis Cache Logs
Stored inside LIST:

```
LPUSH CACHE_LOGS "Cache cleared for USER:123"
LPUSH CACHE_LOGS "DB updated_at detected → cache invalidated"
```

---

# 📡 **Endpoints Summary**

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/users` | Create new user |
| GET | `/users/{id}` | Fetch directly from DB |
| GET | `/users/cache/{id}` | Cache‑aside fetch with TTL |
| PUT | `/users/{id}` | Update (DB + Clear Cache) |
| PATCH | `/users/{id}` | Partial update (DB + Clear Cache) |
| GET | `/users` | Get all users (DB) |
| DELETE | `/users/{id}` | Delete user (DB + Cache Delete) |

---

# 📄 **Sample API Request & Response**

---

## 1️⃣ Create User
### **POST /users**
### Request
```json
{
  "name": "Adhish",
  "email": "adhish@example.com"
}
```
### Response
```json
{
  "userId": "8b02b2c2-85d1-4c62-9b5b-99d209",
  "name": "Adhish",
  "email": "adhish@example.com",
  "updatedAt": "2025-11-11T14:22:19"
}
```

---

## 2️⃣ Get User (Cache Aside)
### **GET /users/cache/{id}**
### Response (1st time)
```json
{
  "status": "CACHE_MISS",
  "source": "DB",
  "data": {
    "userId": "123",
    "name": "Adhish"
  }
}
```

### Response (2nd time)
```json
{
  "status": "CACHE_HIT",
  "source": "REDIS",
  "data": {
    "userId": "123",
    "name": "Adhish"
  }
}
```

---

## 3️⃣ Update User
### **PUT /users/{id}**
### Response
```json
{
  "message": "User updated. Cache invalidated.",
  "user": {
    "userId": "123",
    "name": "Updated Name"
  }
}
```

---

## 4️⃣ Scheduler Log Output
```
[Scheduler] Found 1 updated record. Clearing cache → USER:123
[Scheduler] Cache log added → CACHE_LOGS
```

---

# 🚀 **How to Run**

### Start PostgreSQL
### Start Redis (ensure LRU is enabled)
### Run Spring Boot
```
mvn spring-boot:run
```

---

# ✅ **This Project Demonstrates**

| Feature | Status |
|--------|--------|
| Cache-aside pattern | ✅ |
| Redis TTL | ✅ |
| Redis LRU | ✅ |
| DB-triggered invalidation | ✅ |
| PUT/PATCH invalidation | ✅ |
| Scheduler-based invalidation | ✅ |
| Redis logs | ✅ |
| Clean MVC | ✅ |

--