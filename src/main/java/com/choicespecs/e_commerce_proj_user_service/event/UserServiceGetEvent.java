/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.event;
import java.time.Instant;

import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;
import com.choicespecs.e_commerce_proj_user_service.model.User;


/**
 *
 * @author christopherlee
 */
public class UserServiceGetEvent extends UserServiceEvent {

    public enum Status {
        FOUND,
        NOT_FOUND,
        ERROR
    }

    private User user;
    private String requestId;
    private Status status;
    private String error;
    
    public UserServiceGetEvent() {
        super();
    }

    public UserServiceGetEvent(UserEntity user) {
        super(user);
        this.user = user.toUser();
    }

    public static UserServiceGetEvent found(String requestId, UserEntity entity) {
        UserServiceGetEvent event = new UserServiceGetEvent(entity);
        event.setRequestId(requestId);
        event.setStatus(Status.FOUND);
        event.setUser(entity.toUser());
        return event;
    }

    public static UserServiceGetEvent notFound(String requestId) {
        UserServiceGetEvent event = new UserServiceGetEvent();
        event.setRequestId(requestId);
        event.setStatus(Status.NOT_FOUND);
        event.setCreatedAt(Instant.now());
        event.setUpdatedAt(Instant.now());
        return event;
    }

    public static UserServiceGetEvent error(String requestId, String error) {
        UserServiceGetEvent event = new UserServiceGetEvent();
        event.setRequestId(requestId);
        event.setStatus(Status.ERROR);
        event.setCreatedAt(Instant.now());
        event.setUpdatedAt(Instant.now());
        event.setError(error);
        return event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }


}
