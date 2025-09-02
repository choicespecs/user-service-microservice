/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.choicespecs.e_commerce_proj_user_service.constants.ErrorMessageConstants;
import com.choicespecs.e_commerce_proj_user_service.constants.FieldConstants;
import com.choicespecs.e_commerce_proj_user_service.constants.RabbitMQConstants;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;
import com.choicespecs.e_commerce_proj_user_service.model.ActionType;

/**
 * Unit tests for EventPublisher.
 */
@ExtendWith(MockitoExtension.class)
public class EventPublisherTest {

    @Mock
    RabbitTemplate rabbitTemplate;

    @InjectMocks
    EventPublisher publisher;

    @Captor
    ArgumentCaptor<Object> payloadCaptor;

    @Captor
    ArgumentCaptor<MessagePostProcessor> mppCaptor;

    private UserEntity sampleUser() {
        UserEntity u = new UserEntity();
        // Populate only if your event constructors require fields.
        return u;
    }

    @Nested
    @DisplayName("publishUserEvent(action, payload)")
    class PublishUserEventTests {

        @Test
        void routesCreate() {
            Object payload = new Object();
            publisher.publishUserEvent(ActionType.CREATE.name(), payload);
            verify(rabbitTemplate).convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_CREATED_ROUTING_KEY, payload);
        }

        @Test
        void routesDelete() {
            Object payload = new Object();
            publisher.publishUserEvent(ActionType.DELETE.name(), payload);
            verify(rabbitTemplate).convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_DELETED_ROUTING_KEY, payload);
        }

        @Test
        void routesUpdate() {
            Object payload = new Object();
            publisher.publishUserEvent(ActionType.UPDATE.name(), payload);
            verify(rabbitTemplate).convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_UPDATED_ROUTING_KEY, payload);
        }

        @Test
        void routesGet() {
            Object payload = new Object();
            publisher.publishUserEvent(ActionType.GET.name(), payload);
            verify(rabbitTemplate).convertAndSend(RabbitMQConstants.USER_EXCHANGE, RabbitMQConstants.USER_READ_ROUTING_KEY, payload);
        }

        @Test
        void throwsOnUnsupported_alignWithActionTypeFromString() {
            String bad = "NOPE";
            assertThatThrownBy(() -> publisher.publishUserEvent(bad, new Object()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ErrorMessageConstants.ERROR_INVALID_ACTION_TYPE) // from ActionType.fromString
                .hasMessageContaining(bad);
        }
    }

    @Nested
    @DisplayName("Specific event helpers")
    class HelperMethodTests {

        @Test
        void publishUserCreatedEvent_sendsCreatedEvent() {
            UserEntity user = sampleUser();

            publisher.publishUserCreatedEvent(user);

            verify(rabbitTemplate).convertAndSend(eq(RabbitMQConstants.USER_EXCHANGE), eq(RabbitMQConstants.USER_CREATED_ROUTING_KEY), payloadCaptor.capture());
            assertThat(payloadCaptor.getValue()).isInstanceOf(UserServiceCreatedEvent.class);
        }

        @Test
        void publishUserDeletedEvent_sendsDeletedEvent() {
            UserEntity user = sampleUser();

            publisher.publishUserDeletedEvent(user);

            verify(rabbitTemplate).convertAndSend(eq(RabbitMQConstants.USER_EXCHANGE), eq(RabbitMQConstants.USER_DELETED_ROUTING_KEY), payloadCaptor.capture());
            assertThat(payloadCaptor.getValue()).isInstanceOf(UserServiceDeletedEvent.class);
        }

        @Test
        void publishUserUpdatedEvent_sendsUpdatedEvent() {
            UserEntity user = sampleUser();

            publisher.publishUserUpdatedEvent(user);

            verify(rabbitTemplate).convertAndSend(eq(RabbitMQConstants.USER_EXCHANGE), eq(RabbitMQConstants.USER_UPDATED_ROUTING_KEY), payloadCaptor.capture());
            assertThat(payloadCaptor.getValue()).isInstanceOf(UserServiceUpdatedEvent.class);
        }

        @Test
        void publishUserReadEvent_setsHeadersAndContentType() {
            String requestId = "req-123";
            UserEntity user = sampleUser();

            publisher.publishUserReadEvent(requestId, user);

            verify(rabbitTemplate).convertAndSend(eq(RabbitMQConstants.USER_EXCHANGE), eq(RabbitMQConstants.USER_READ_ROUTING_KEY), payloadCaptor.capture(), mppCaptor.capture());
            assertThat(payloadCaptor.getValue()).isInstanceOf(UserServiceGetEvent.class);

            // Apply the captured MPP and validate headers/content-type
            MessageProperties props = new MessageProperties();
            Message msg = new Message(new byte[0], props);
            Message processed = mppCaptor.getValue().postProcessMessage(msg);

            assertThat(processed.getMessageProperties().getHeaders()
                .get(FieldConstants.HEADER_REQUEST_ID_FIELD)).isEqualTo(requestId);
            assertThat(processed.getMessageProperties().getContentType())
                .isEqualTo(FieldConstants.JSON_CONTENT_TYPE);
        }

        @Test
        void publishUserGetNotFound_setsHeaderOnly_contentTypeUnchanged() {
            String requestId = "req-404";

            publisher.publishUserGetNotFound(requestId);

            verify(rabbitTemplate).convertAndSend(eq(RabbitMQConstants.USER_EXCHANGE), eq(RabbitMQConstants.USER_READ_ROUTING_KEY), payloadCaptor.capture(), mppCaptor.capture());
            assertThat(payloadCaptor.getValue()).isInstanceOf(UserServiceGetEvent.class);

            MessageProperties props = new MessageProperties();
            Message msg = new Message(new byte[0], props);
            String before = props.getContentType(); // whatever default is (often "application/octet-stream")

            Message processed = mppCaptor.getValue().postProcessMessage(msg);

            assertThat(processed.getMessageProperties().getHeaders()
                .get(FieldConstants.HEADER_REQUEST_ID_FIELD)).isEqualTo(requestId);
            // Content type should remain unchanged (MPP doesn't set it in NotFound)
            assertThat(processed.getMessageProperties().getContentType()).isEqualTo(before);
        }

        @Test
        void publishUserGetError_setsHeaderOnly_contentTypeUnchanged() {
            String requestId = "req-500";
            String errorMessage = "Boom";

            publisher.publishUserGetError(requestId, errorMessage);

            verify(rabbitTemplate).convertAndSend(eq(RabbitMQConstants.USER_EXCHANGE), eq(RabbitMQConstants.USER_READ_ROUTING_KEY), payloadCaptor.capture(), mppCaptor.capture());
            assertThat(payloadCaptor.getValue()).isInstanceOf(UserServiceGetEvent.class);

            MessageProperties props = new MessageProperties();
            Message msg = new Message(new byte[0], props);
            String before = props.getContentType(); 
            Message processed = mppCaptor.getValue().postProcessMessage(msg);

            assertThat(processed.getMessageProperties().getHeaders()
                .get(FieldConstants.HEADER_REQUEST_ID_FIELD)).isEqualTo(requestId);
            // Content type should remain unchanged (MPP doesn't set it in Error)
            assertThat(processed.getMessageProperties().getContentType()).isEqualTo(before);
        }
    }
}