/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.event;

import java.time.Instant;
import java.util.UUID;

import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 *
 * @author christopherlee
 */
public class UserServiceCreatedEvent {
    private UUID userId;
    private String email;
    private Instant createdAt;

    public UserServiceCreatedEvent() {}

    public UserServiceCreatedEvent(UserEntity user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
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


}
