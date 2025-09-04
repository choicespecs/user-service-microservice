package com.choicespecs.e_commerce_proj_user_service.config;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.choicespecs.e_commerce_proj_user_service.constants.RabbitMQConstants;


/**
 * RabbitMQConfiguration for Spring Boot Application
 * 
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Create an exchange for the user service
     * Durable = true which survives broker restarts.
     * Auto-delete = false which is not deleted automatically.
     * 
     * In RabbitMQ, producers don’t send messages directly to queues.
     * Instead, producers publish messages to an exchange.
     * The exchange then decides, based on rules (bindings), which queue(s) should receive the message.
     * So exchanges act as routers between producers and queues.
     * @return TopicExchange
     */
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(RabbitMQConstants.USER_EXCHANGE, true, false);
    }

    /**
     * Create an queue for the user service
     * Durable = true → survives broker restarts.
     * 
     * In RabbitMQ, A queue is where messages actually sit until a consumer processes them.
     * Exchanges (like your TopicExchange) route messages into queues based on routing keys.
     * Consumers subscribe to queues, not exchanges.
     * 
     * So this queue is the “mailbox” for your user service
     * @return Queue
     */
    @Bean
    public Queue userQueue() {
        return new Queue(RabbitMQConstants.USER_QUEUE, true);
    }

    /**
     * Connects the queue to the exchange.
     * Routing key pattern this queue will receive any message whose routing key starts with the USER_COMMAND_PATTERN
     * 
     * this is where the “wiring” in RabbitMQ happens
     * A binding in RabbitMQ is like saying:
     * “Deliver messages from this exchange into this queue if the routing key matches this pattern.”
     * In this case:
     *  - Exchange: userExchange (a TopicExchange).
     *  - Queue: userCommandQueue.
     *  - Routing key pattern: USER_COMMAND_PATTERN (probably "user.*").
     * 
     * When a producer publishes to the user.exchange with a routing key (e.g., "user.created" or "user.deleted"):
     *   1.	The exchange receives the message.
     *   2.	It checks all bindings (rules).
     *   3.	If the routing key matches "user.*", the message is routed into userCommandQueue.
     *   4.	Consumers listening to that queue will receive the message.
     * 
     * If the routing key doesn’t match the binding, the message does not go into that queue 
     * (it might go to other queues bound with different patterns).
     * 
     * Exchange = a mail sorter in a post office.
     * Routing key = the address on the envelope.
     * Queue = a mailbox.
     * Binding = the rule that says:
     * “All letters addressed to user.* should go into this mailbox.”
     * 
     * Without bindings, your queue would sit empty (no messages routed).
     * With this binding, your service gets only the messages it cares about (all user-related events).
     * You can have multiple queues bound with different patterns for different consumers.
     * 
     * This line connects your queue to your exchange with a filter rule 
     * (routing key pattern), so only the right messages flow into your service’s inbox.
     * @param userExchange
     * @return
     */
    @Bean
    public Binding bindUserCommands(Queue userCommandQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userCommandQueue).to(userExchange).with(RabbitMQConstants.USER_COMMAND_PATTERN);
    }

    /**
     * Converts messages between Java objects ↔ JSON automatically.
     * trustedPackages("*") → allows deserialization from any package (useful for flexibility, but can be risky for security).
     * This way, you don’t have to manually serialize/deserialize payloads.
     * 
     * Registers a {@link Jackson2JsonMessageConverter} as a Spring bean.
     * Spring will automatically use this converter with RabbitMQ (via RabbitTemplate or listeners).
     * 
     * It automatically handles serialization (Java → JSON) when publishing messages and deserialization (JSON → Java) when consuming.
     * 
     * @return
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        // Uses Jackson (the popular JSON library) to handle serialization/deserialization.
        // Without this, RabbitTemplate would just send raw byte[] or Strings — you’d have to convert manually.
        Jackson2JsonMessageConverter conv = new Jackson2JsonMessageConverter();

        // Helps RabbitMQ figure out what Java type a JSON payload should be deserialized into.
        DefaultJackson2JavaTypeMapper mapper = new DefaultJackson2JavaTypeMapper();

        // Normally, Spring AMQP restricts which packages it will deserialize to 
        // (to prevent security issues with malicious payloads).
        // "*" means: trust all packages.
        // Pro: flexible — you don’t need to whitelist every DTO package.
        // Con: less safe — someone could try to trick your app into deserializing to an unexpected type.
        mapper.setTrustedPackages("*");

        // Controls how the converter decides what Java type to deserialize into.
        // INFERRED → infer the type from the JSON structure (instead of requiring explicit type headers in the message).
        // Example: if JSON looks like { "userId": "123" }, and your consumer expects a UserCreatedEvent, it can deserialize without an extra _type header.
        mapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);

        // Tells the converter to use this custom type-mapper configuration.
        conv.setClassMapper(mapper);
        return conv;
    }

    /**
     * Defines the {@link RabbitTemplate} bean used for publishing and consuming messages.
     * 
     * The template is configured with a custom JSON message converter so that
     * Java objects can be automatically serialized to JSON when sending, and
     * deserialized back into Java objects when receiving.
     * 
     * This bean abstracts away connection handling and low-level RabbitMQ
     * operations, allowing simple calls like:
     * {@code rabbitTemplate.convertAndSend("user.exchange", "user.created", userEvent);}
     *
     * @param connectionFactory the factory that provides connections to RabbitMQ
     * @return a configured {@link RabbitTemplate} for message operations
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
