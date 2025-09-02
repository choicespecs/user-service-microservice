/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.constants;

/**
 *
 * @author christopherlee
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
}
