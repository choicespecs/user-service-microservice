package com.choicespecs.e_commerce_proj_user_service.config;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.choicespecs.e_commerce_proj_user_service.event.EventPublisher;

/**
 * This class configures the {@link EventPublisher} class to bean factory. 
 * This will wrap around the {@link RabbitTemplate} 
 * to make publishing events easier.
 */
@Configuration
public class EventPublisherConfig {
    @Bean
    public EventPublisher eventPublisher(RabbitTemplate rabbitTemplate) {
        return new EventPublisher(rabbitTemplate);
    }
}
