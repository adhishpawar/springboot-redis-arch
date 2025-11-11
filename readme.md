
# Spring Boot Redis CRUD – README

## 🧩 Overview
This project is a **basic Redis CRUD implementation using Spring Boot**, inspired by the tutorial:  
https://youtu.be/lXK8RS40v9c

It demonstrates:
- Redis installation & setup
- Spring Boot + Redis integration
- Full CRUD using Redis Hash
- MVC + Flowcharts + Architecture diagrams

---

# 📦 Technologies
- Spring Boot 3.x
- Spring Data Redis
- Redis (In-memory DB)
- Lettuce Client
- Java 17
- Maven

---

# 🔧 Redis Installation

## Windows
```
Option 1 → Redis MSI Installer  
https://github.com/microsoftarchive/redis/releases
After install → Redis runs as Windows service.

Option 2 → WSL
wsl --install
sudo apt update
sudo apt install redis-server
sudo service redis-server start
```

## MacOS
```
brew install redis
brew services start redis
```

## Linux
```
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

### Verify Redis
```
redis-cli ping   → PONG
redis-cli --version
redis-cli
SET test "Hello"
GET test
```

---

# 📁 Project Structure
```
src/main/java/com/api/redis/
│
├── config/
│   └── RedisConfig.java
│
├── controller/
│   └── UserController.java
│
├── dao/
│   └── UserDao.java
│
└── models/
    └── User.java
```

---

# ⚙️ RedisConfig – Detailed Explanation

## RedisConfig.java
```
@Bean
public RedisConnectionFactory connectionFactory() {
    return new LettuceConnectionFactory();
}

@Bean
public RedisTemplate<String,Object> redisTemplate() {
    RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory());
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return redisTemplate;
}
```

---

# 🧠 How RedisConfig Works – Flowchart

```
            ┌────────────────────────────┐
            │ RedisConfig.java           │
            └───────────────┬────────────┘
                            │
                            ▼
        ┌────────────────────────────────────┐
        │ Create LettuceConnectionFactory()  │
        │ - Connects to localhost:6379       │
        └───────────────────┬────────────────┘
                            │
                            ▼
        ┌────────────────────────────────────┐
        │ Create RedisTemplate()             │
        │ - Set connection factory           │
        │ - Set key serializer (String)      │
        │ - Set value serializer (JSON)      │
        └───────────────────┬────────────────┘
                            │
                            ▼
        ┌────────────────────────────────────┐
        │ Spring Boot Container registers    │
        │ redisTemplate bean                 │
        └────────────────────────────────────┘
```

---

# 🏛 MVC Architecture – Graphical Representation

```
                    CLIENT (Postman/Browser)
                               │
                               ▼
 ┌──────────────────────────────────────────────────────┐
 │               CONTROLLER (UserController)            │
 │  - Accepts HTTP requests                             │
 │  - Generates UUID for User                           │
 │  - Calls UserDao methods                             │
 └───────────────────────────┬──────────────────────────┘
                             │
                             ▼
 ┌──────────────────────────────────────────────────────┐
 │                      DAO (UserDao)                   │
 │  - Interacts with Redis via RedisTemplate            │
 │  - Performs HSET, HGET, HGETALL, HDEL               │
 └───────────────────────────┬──────────────────────────┘
                             │
                             ▼
 ┌──────────────────────────────────────────────────────┐
 │                 REDIS TEMPLATE LAYER                 │
 │  opsForHash()                                        │
 │  - put() → HSET                                      │
 │  - get() → HGET                                      │
 │  - entries() → HGETALL                               │
 │  - delete() → HDEL                                   │
 └───────────────────────────┬──────────────────────────┘
                             │
                             ▼
 ┌──────────────────────────────────────────────────────┐
 │                  REDIS SERVER (6379)                 │
 │  Stores users in HASH format:                        │
 │  KEY: USER123                                        │
 │   ├── uuid1 → {...}                                  │
 │   ├── uuid2 → {...}                                  │
 │   └── uuid3 → {...}                                  │
 └──────────────────────────────────────────────────────┘
```

---

# 📌 Controllers & Flow

## UserController.java

### Create User Flow
```
POST /users
         │
         ▼
Generate UUID
         │
         ▼
userDao.save(user)
         │
         ▼
Redis → HSET USER123 uuid {...}
```

### Get User Flow
```
GET /users/{id}
         │
         ▼
userDao.get(id)
         │
         ▼
Redis → HGET USER123 id
```

### Get All Users Flow
```
GET /users
         │
         ▼
userDao.findAll()
         │
         ▼
Redis → HGETALL USER123
```

### Delete User Flow
```
DELETE /users/{id}
         │
         ▼
userDao.delete(id)
         │
         ▼
Redis → HDEL USER123 id
```

---

# 🗄 Redis Hash Structure
```
127.0.0.1:6379> HGETALL USER123

"uuid-1"
"{ user JSON }"

"uuid-2"
"{ user JSON }"
```

---

# 🚀 Running the Application

```
mvn clean install
mvn spring-boot:run
```

Test APIs using Postman:
```
POST    /users
GET     /users/{id}
GET     /users
DELETE  /users/{id}
```

---

# Redis Advanced Functionalities (TTL, Locking, Cache-Aside, Pub/Sub)

## 1. TTL (Expiry-based Cache)

Used to store heavy objects temporarily.

### Example:

``` java
public void cacheHeavyData(String key, Object data, long ttlInSeconds) {
    redisTemplate.opsForValue().set(key, data, Duration.ofSeconds(ttlInSeconds));
}
```

### Usage:

``` java
cacheHeavyData("LARGE_FILE_DATA", obj, 300); // 5 min
```

### Controller Example:

``` java
@GetMapping("/cache-heavy")
public Object getHeavyFileData() {
    String key = "LARGE_FILE_DATA";

    Object cached = redisTemplate.opsForValue().get(key);

    if (cached != null) return cached;

    Object heavyResponse = heavyFileService.processHugeFile();

    cacheHeavyData(key, heavyResponse, 300);

    return heavyResponse;
}
```

------------------------------------------------------------------------

## 2. Distributed Locking

``` java
Boolean locked = redisTemplate.opsForValue().setIfAbsent("FILE_LOCK", "LOCKED", 20, TimeUnit.SECONDS);

if Boolean.FALSE.equals(locked) {
    return "Already processing!";
}

// After processing
redisTemplate.delete("FILE_LOCK");
```

------------------------------------------------------------------------

## 3. Caching List / Map / Set

### Store List

``` java
redisTemplate.opsForList().leftPush("RECENT_LOGS", logText);
```

### Get List

``` java
redisTemplate.opsForList().range("RECENT_LOGS", 0, 10);
```

------------------------------------------------------------------------

## 4. Pub/Sub Messaging

### Publisher

``` java
redisTemplate.convertAndSend("USER_CHANNEL", user);
```

### Subscriber

Use `MessageListenerAdapter` + `RedisMessageListenerContainer`.

------------------------------------------------------------------------

## 5. Cache-Aside Pattern (Best Practice)

``` java
public User getUserWithCache(String userId) {
    String key = "USER_CACHE:" + userId;

    User cached = (User) redisTemplate.opsForValue().get(key);
    if (cached != null) return cached;

    User user = fetchFromDatabase(userId);

    redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(5));

    return user;
}
```

------------------------------------------------------------------------

## Summary

✔ True Redis speed\
✔ Automatic expiry\
✔ Prevents duplicate processing\
✔ Real-time updates\
✔ Industry-standard caching


