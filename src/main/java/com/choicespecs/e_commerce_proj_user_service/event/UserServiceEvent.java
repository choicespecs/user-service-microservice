/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.event;

import org.springframework.context.ApplicationEvent;

/**
 *
 * @author christopherlee
 */
public class UserServiceEvent extends ApplicationEvent {
    private EventType type;

    public UserServiceEvent(Object source, EventType type) {
        super(source);
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
        CREATED,
        DELETED,
        UPDATED,
        GET
    }


}
