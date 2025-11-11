package com.api.redis.repo;

import com.api.redis.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.OffsetDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {

    // find users updated after a given time
    @Query("select u from User u where u.updatedAt > :since")
    List<User> findUsersUpdatedAfter(OffsetDateTime since);
}