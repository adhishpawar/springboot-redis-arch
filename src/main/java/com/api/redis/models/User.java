package com.api.redis.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;


@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    private String name;
    private String email;
    private String role;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public User() {}

    // getters / setters
    // ensure you update updatedAt on update operations in service or DB trigger
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
