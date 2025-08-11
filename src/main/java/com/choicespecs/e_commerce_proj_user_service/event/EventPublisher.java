/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;
import com.choicespecs.e_commerce_proj_user_service.model.ActionType;

/**
 *
 * @author christopherlee
 */
@Component
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    private static final String USER_EXCHANGE = "user.exchange";
    private static final String USER_CREATED_ROUTING_KEY = "user.created";
    private static final String USER_DELETED_ROUTING_KEY = "user.deleted";
    private static final String USER_UPDATED_ROUTING_KEY = "user.updated";

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserEvent(String action, Object payload) {
        String routingKey;
        ActionType actionType = ActionType.fromString(action);
        switch (actionType) {
            case CREATE:
                routingKey = USER_CREATED_ROUTING_KEY;
                break;
            case DELETE:
                routingKey = USER_DELETED_ROUTING_KEY;
                break;
            case UPDATE:
                routingKey = USER_UPDATED_ROUTING_KEY;
                break;
            default:
                throw new IllegalArgumentException("Unsupported action: " + action);
        }

        rabbitTemplate.convertAndSend(USER_EXCHANGE, routingKey, payload);
    }

    public void publishUserCreatedEvent(UserEntity user) {
        UserServiceCreatedEvent event = new UserServiceCreatedEvent(user);
        rabbitTemplate.convertAndSend(USER_EXCHANGE, USER_CREATED_ROUTING_KEY,event);
    }

    public void publishUserDeletedEvent(UserEntity user) {
        UserServiceDeletedEvent event = new UserServiceDeletedEvent(user);
        rabbitTemplate.convertAndSend(USER_EXCHANGE, USER_DELETED_ROUTING_KEY,event);
    }

    public void publishUserUpdatedEvent(UserEntity user) {
        UserServiceUpdatedEvent event = new UserServiceUpdatedEvent(user);
        rabbitTemplate.convertAndSend(USER_EXCHANGE, USER_UPDATED_ROUTING_KEY,event);
    }

}
