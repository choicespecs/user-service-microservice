/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */ 

package com.choicespecs.e_commerce_proj_user_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.choicespecs.e_commerce_proj_user_service.constants.ErrorMessageConstants;
import com.choicespecs.e_commerce_proj_user_service.constants.UserFieldConstants;
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
    private static final String USER_QUEUE = "user-service-queue";


    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = USER_QUEUE)
    public void receiveMessage(JsonNode jsonNode,
                               @Header(name="x-request-id", required=false) String requestId) {
        try {
            if (!jsonNode.has(ACTION_FIELD)) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
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
                case GET:
                    getUser(jsonNode, requestId);
                    break;
            }
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_PROCESSING_FAIL, e);
        }
    }

    private void createUser(JsonNode jsonNode) {
        try {
            if (!jsonNode.has(UserFieldConstants.USER_FIELD)) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
            }
            JsonNode userJson = jsonNode.get(UserFieldConstants.USER_FIELD);
            User user = objectMapper.treeToValue(userJson, User.class);
            userService.createUser(user);
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_CREATE_USER_FAIL, e);
        }
    }

    private String requireText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || !v.isTextual()) {
            throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
        }
        return v.asText();
    }

    private void deleteUser(JsonNode node) {
        try {
            String email = requireText(node, UserFieldConstants.EMAIL_FIELD);
            userService.deleteUser(email);
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_DELETE_USER_FAIL, e);
        }
    }

    private void updateUser(JsonNode jsonNode) {
        try {
            if (!jsonNode.has(UserFieldConstants.USER_FIELD)) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
            }
            JsonNode userJson = jsonNode.get(UserFieldConstants.USER_FIELD);
            String username = requireText(userJson, UserFieldConstants.USERNAME_FIELD);
            userService.updateUser(username, userJson);
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_UPDATE_USER_FAIL, e);
        }
    }

    private void getUser(JsonNode jsonNode, String headerReqId) {
        try {
            if (headerReqId == null || headerReqId.isBlank()) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_HEADER);
            }
            if (!jsonNode.has(UserFieldConstants.USER_FIELD)) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
            }
            JsonNode userJson = jsonNode.get(UserFieldConstants.USER_FIELD);
            userService.getUser(userJson, headerReqId);
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_GET_USER_FAIL, e);
        }
    }


}
