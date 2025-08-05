/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 *
 * @author christopherlee
 */
@Component
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    private static final String USER_EXCHANGE = "user.exchange";
    private static final String USER_CREATED_ROUTING_KEY = "user.created";

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserCreatedEvent(UserEntity user) {
        UserServiceCreatedEvent event = new UserServiceCreatedEvent(user);
        rabbitTemplate.convertAndSend(USER_EXCHANGE, USER_CREATED_ROUTING_KEY,event);
    }

}
