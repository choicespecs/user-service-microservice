/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.choicespecs.e_commerce_proj_user_service.constants.ErrorMessageConstants;
import com.choicespecs.e_commerce_proj_user_service.constants.FieldConstants;
import com.choicespecs.e_commerce_proj_user_service.constants.RabbitMQConstants;
import com.choicespecs.e_commerce_proj_user_service.dto.UserSearchRequest;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;
import com.choicespecs.e_commerce_proj_user_service.model.ActionType;

/**
 *
 * @author christopherlee
 */
@Component
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserEvent(String action, Object payload) {
        String routingKey;
        ActionType actionType = ActionType.fromString(action);
        switch (actionType) {
            case CREATE:
                routingKey = RabbitMQConstants.USER_CREATED_ROUTING_KEY;
                break;
            case DELETE:
                routingKey = RabbitMQConstants.USER_DELETED_ROUTING_KEY;
                break;
            case UPDATE:
                routingKey = RabbitMQConstants.USER_UPDATED_ROUTING_KEY;
                break;
            case GET:
                routingKey = RabbitMQConstants.USER_READ_ROUTING_KEY;
                break;
            default:
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_UNSUPPORTED_ACTION + action);
        }

        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, routingKey, payload);
    }

    public void publishUserCreatedEvent(UserEntity user) {
        UserServiceEvent event = new UserServiceCreatedEvent(user);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_UPDATED_ROUTING_KEY,event);
    }

    public void publishUserDeletedEvent(UserEntity user) {
        UserServiceEvent event = new UserServiceDeletedEvent(user);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_DELETED_ROUTING_KEY,event);
    }

    public void publishUserUpdatedEvent(UserEntity user) {
        UserServiceEvent event = new UserServiceUpdatedEvent(user);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_UPDATED_ROUTING_KEY,event);
    }

    public void publishUserReadEvent(String requestId, UserEntity user) {
        UserServiceGetEvent event = UserServiceGetEvent.found(requestId, user);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_READ_ROUTING_KEY, event, msg -> {
                msg.getMessageProperties().setHeader(FieldConstants.HEADER_REQUEST_ID_FIELD, requestId);
                msg.getMessageProperties().setContentType(FieldConstants.JSON_CONTENT_TYPE);
                return msg;
        });
    }

    public void publishUserGetNotFound(String requestId) {
        UserServiceGetEvent event = UserServiceGetEvent.notFound(requestId);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_READ_ROUTING_KEY, event, msg -> {
                msg.getMessageProperties().setHeader(FieldConstants.HEADER_REQUEST_ID_FIELD, requestId);
                return msg;
        });
    }

    public void publishUserGetError(String requestId, String message) {
        UserServiceGetEvent event = UserServiceGetEvent.error(requestId, message);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_READ_ROUTING_KEY, event, msg -> {
                msg.getMessageProperties().setHeader(FieldConstants.HEADER_REQUEST_ID_FIELD, requestId);
                return msg;
        });
    }


    public void publishUserSearchSuccess(String requestId, UserSearchRequest req, Page<UserEntity> page) {
        UserServiceSearchEvent event = UserServiceSearchEvent.success(
            requestId, req, page.getTotalElements(), page.getTotalPages(), page.getContent()
        );
        rabbitTemplate.convertAndSend(
            RabbitMQConstants.USER_EXCHANGE,
            RabbitMQConstants.USER_SEARCH_ROUTING_KEY,
            event,
            msg -> {
                msg.getMessageProperties().setHeader(FieldConstants.HEADER_REQUEST_ID_FIELD, requestId);
                msg.getMessageProperties().setContentType(FieldConstants.JSON_CONTENT_TYPE);
                return msg;
            }
        );
    }

    public void publishUserSearchError(String requestId, UserSearchRequest req, String message) {
        UserServiceSearchEvent event = UserServiceSearchEvent.error(requestId, req, message);
        rabbitTemplate.convertAndSend(
            RabbitMQConstants.USER_EXCHANGE,
            RabbitMQConstants.USER_SEARCH_ROUTING_KEY,
            event,
            msg -> {
                msg.getMessageProperties().setHeader(FieldConstants.HEADER_REQUEST_ID_FIELD, requestId);
                msg.getMessageProperties().setContentType(FieldConstants.JSON_CONTENT_TYPE);
                return msg;
            }
        );
    }

}
