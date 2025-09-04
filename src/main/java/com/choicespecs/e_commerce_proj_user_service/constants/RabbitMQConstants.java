package com.choicespecs.e_commerce_proj_user_service.constants;

/**
 * Constants for RabbitMQ 
 */
public class RabbitMQConstants {
    private RabbitMQConstants() {}

    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";
    public static final String USER_DELETED_ROUTING_KEY = "user.deleted";
    public static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    public static final String USER_READ_ROUTING_KEY = "user.get";
    public static final String USER_SEARCH_ROUTING_KEY = "user.search";
    public static final String USER_QUEUE = "user-service-queue";
    public static final String USER_COMMAND_PATTERN = "user.*";
}
