/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */ 

package com.choicespecs.e_commerce_proj_user_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.choicespecs.e_commerce_proj_user_service.model.ActionType;
import com.choicespecs.e_commerce_proj_user_service.model.User;
import com.choicespecs.e_commerce_proj_user_service.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 *
 * @author metal
 */
@Component
public class UserServiceListener {

    private static final Logger log = LoggerFactory.getLogger(UserServiceListener.class);
    private static final String ACTION_FIELD = "action";
    private static final String USER_FIELD = "user";
    private static final String EMAIL_FIELD = "email";
    private static final String USERNAME_FIELD = "username";


    private static final String ERROR_MISSING_FIELD = "Missing required fields in message";
    private static final String ERROR_PROCESSING_FAIL = "Failed to process message";
    private static final String ERROR_CREATE_USER_FAIL = "Failed to create user";
    private static final String ERROR_DELETE_USER_FAIL = "Failed to delete user";
    private static final String ERROR_UPDATE_USER_FAIL = "Failed to update user";

    private static final String USER_QUEUE = "user-service-queue";


    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = USER_QUEUE)
    public void receiveMessage(JsonNode jsonNode) {
        try {
            if (!jsonNode.has(ACTION_FIELD)) {
                throw new IllegalArgumentException(ERROR_MISSING_FIELD);
            }
            String actionString = jsonNode.get(ACTION_FIELD).asText().toLowerCase();
            ActionType action = ActionType.fromString(actionString);
            switch (action) {
                case CREATE:
                    createUser(jsonNode);             
                    break;
                case DELETE:
                    deleteUser(jsonNode);
                    break;
                case UPDATE:
                    updateUser(jsonNode);
                    break;
            }
        } catch (Exception e) {
            log.error(ERROR_PROCESSING_FAIL, e);
        }
    }

    private void createUser(JsonNode jsonNode) {
        try {
            if (!jsonNode.has(USER_FIELD)) {
                throw new IllegalArgumentException(ERROR_MISSING_FIELD);
            }
            JsonNode userJson = jsonNode.get(USER_FIELD);
            User user = objectMapper.treeToValue(userJson, User.class);
            userService.createUser(user);
        } catch (Exception e) {
            log.error(ERROR_CREATE_USER_FAIL, e);
        }
    }

    private String requireText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || !v.isTextual()) {
            throw new IllegalArgumentException(ERROR_MISSING_FIELD);
        }
        return v.asText();
    }

    private void deleteUser(JsonNode node) {
        try {
            String email = requireText(node, EMAIL_FIELD);
            userService.deleteUser(email);
        } catch (Exception e) {
            log.error(ERROR_DELETE_USER_FAIL, e);
        }
    }

    private void updateUser(JsonNode jsonNode) {
        try {
            String username = requireText(jsonNode, USERNAME_FIELD);
            if (!jsonNode.has(USER_FIELD)) {
                throw new IllegalArgumentException(ERROR_MISSING_FIELD);
            }
            JsonNode userJson = jsonNode.get(USER_FIELD);
            userService.updateUser(username, userJson);
        } catch (Exception e) {
            log.error(ERROR_UPDATE_USER_FAIL, e);
        }
    }
}
