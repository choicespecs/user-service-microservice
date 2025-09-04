
package com.choicespecs.e_commerce_proj_user_service.event;
import java.time.Instant;
import java.util.UUID;

import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 * Abstract class for events that are published
 * All events should inherit from this
 */
public abstract class UserServiceEvent {
    private UUID eventId;
    private UUID userId;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

    protected UserServiceEvent() {}


    public UserServiceEvent(UserEntity user) {
        this.eventId = UUID.randomUUID();
        this.userId = user.getId();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getUserId() {
        return userId;
    }  

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
}
