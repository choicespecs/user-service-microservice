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
 * Publishes domain events from the User Service to RabbitMQ.
 * <p>
 * This component centralizes all messaging concerns for user-related
 * actions (create, update, delete, get, search). Each method builds the
 * appropriate event payload and sends it to {@link RabbitMQConstants#USER_EXCHANGE}
 * with the routing key defined in {@link RabbitMQConstants}.
 * <p>
 * Common headers such as a request correlation ID are attached where applicable
 * to support end-to-end tracing across services.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Translate high-level actions into routing keys.</li>
 *   <li>Publish typed event payloads (e.g., {@code UserServiceCreatedEvent}).</li>
 *   <li>Attach headers such as {@code requestId} and content type.</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Ensure your payload types are serializable by the configured message converter.</li>
 *   <li>Consider enabling publisher confirms/returns for delivery guarantees if needed.</li>
 * </ul>
 *
 * @author christopherlee
 */
@Component
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    /**
     * Creates an {@code EventPublisher}.
     *
     * @param rabbitTemplate the Spring AMQP template used to send messages
    */
    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    /**
     * Publishes a user event using a generic action string that is mapped to a routing key.
     * <p>
     * Valid action strings are defined by {@link ActionType}. Unsupported actions
     * result in an {@link IllegalArgumentException}.
     *
     * @param action  the action to publish (e.g., "CREATE", "UPDATE", "DELETE", "GET")
     * @param payload the event payload object to send; must be compatible with the configured message converter
     * @throws IllegalArgumentException if the action is not supported
     */
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

    /**
     * Publishes a "user created" event.
     *
     * @param user the created user entity
     */
    public void publishUserCreatedEvent(UserEntity user) {
        UserServiceEvent event = new UserServiceCreatedEvent(user);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_UPDATED_ROUTING_KEY,event);
    }

    /**
     * Publishes a "user deleted" event.
     *
     * @param user the deleted user entity (or a minimal entity carrying identifiers)
     */
    public void publishUserDeletedEvent(UserEntity user) {
        UserServiceEvent event = new UserServiceDeletedEvent(user);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_DELETED_ROUTING_KEY,event);
    }

    /**
     * Publishes a "user updated" event.
     *
     * @param user the updated user entity
     */
    public void publishUserUpdatedEvent(UserEntity user) {
        UserServiceEvent event = new UserServiceUpdatedEvent(user);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_UPDATED_ROUTING_KEY,event);
    }

    /**
     * Publishes a "user read (found)" event for a GET request.
     * Adds {@code requestId} and content type headers for tracing and contract clarity.
     *
     * @param requestId a correlation identifier associated with the inbound request
     * @param user      the found user entity
     */
    public void publishUserReadEvent(String requestId, UserEntity user) {
        UserServiceGetEvent event = UserServiceGetEvent.found(requestId, user);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_READ_ROUTING_KEY, event, msg -> {
                msg.getMessageProperties().setHeader(FieldConstants.HEADER_REQUEST_ID_FIELD, requestId);
                msg.getMessageProperties().setContentType(FieldConstants.JSON_CONTENT_TYPE);
                return msg;
        });
    }

    /**
     * Publishes a "user read (not found)" event for a GET request.
     *
     * @param requestId a correlation identifier associated with the inbound request
     */
    public void publishUserGetNotFound(String requestId) {
        UserServiceGetEvent event = UserServiceGetEvent.notFound(requestId);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_READ_ROUTING_KEY, event, msg -> {
                msg.getMessageProperties().setHeader(FieldConstants.HEADER_REQUEST_ID_FIELD, requestId);
                return msg;
        });
    }


    /**
     * Publishes a "user read (error)" event for a GET request.
     *
     * @param requestId a correlation identifier associated with the inbound request
     * @param message   an error message describing the failure
     */
    public void publishUserGetError(String requestId, String message) {
        UserServiceGetEvent event = UserServiceGetEvent.error(requestId, message);
        rabbitTemplate.convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_READ_ROUTING_KEY, event, msg -> {
                msg.getMessageProperties().setHeader(FieldConstants.HEADER_REQUEST_ID_FIELD, requestId);
                return msg;
        });
    }

    /**
     * Publishes a "user search (success)" event containing paging metadata and results.
     * Sets {@code requestId} and content type headers.
     *
     * @param requestId a correlation identifier associated with the search request
     * @param req       the original search request criteria
     * @param page      a Spring Data page of {@link UserEntity} results
     */
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


    /**
     * Publishes a "user search (error)" event with details of the failure.
     *
     * @param requestId a correlation identifier associated with the search request
     * @param req       the original search request criteria
     * @param message   an error message describing the failure
     */
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
