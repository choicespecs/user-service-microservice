/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */ 

package com.choicespecs.e_commerce_proj_user_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.choicespecs.e_commerce_proj_user_service.model.User;
import com.choicespecs.e_commerce_proj_user_service.service.UserProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 *
 * @author metal
 */
@Component
public class UserCreatedListener {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserProcessor userProcessor;

    @RabbitListener(queues = "${queue.user-created}")
    public void receiveMessage(String message) {
        try {
            User user = objectMapper.readValue(message, User.class);
            userProcessor.handle(user);
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

}
