/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.choicespecs.e_commerce_proj_user_service.event.UserServiceEvent;

/**
 *
 * @author christopherlee
 */
@Configuration
public class EventPublisherConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public ApplicationEventPublisher applicationEventPublisher() {
        return new ApplicationEventPublisher() {
            @Override
            public void publishEvent(ApplicationEvent event) {
                if (event instanceof UserServiceEvent) {
                    UserServiceEvent userCreatedEvent = (UserServiceEvent) event;
                    rabbitTemplate.convertAndSend("user.service", userCreatedEvent);
                }
            }

            @Override
            public void publishEvent(Object event) {
                if (event instanceof ApplicationEvent) {
                    publishEvent((ApplicationEvent) event);
                }
            }
        };
    }

}
