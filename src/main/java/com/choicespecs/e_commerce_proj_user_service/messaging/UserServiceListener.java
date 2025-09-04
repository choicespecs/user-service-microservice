package com.choicespecs.e_commerce_proj_user_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.choicespecs.e_commerce_proj_user_service.constants.ErrorMessageConstants;
import com.choicespecs.e_commerce_proj_user_service.constants.FieldConstants;
import com.choicespecs.e_commerce_proj_user_service.constants.RabbitMQConstants;
import com.choicespecs.e_commerce_proj_user_service.dto.UserRequest;
import com.choicespecs.e_commerce_proj_user_service.dto.UserSearchRequest;
import com.choicespecs.e_commerce_proj_user_service.model.ActionType;
import com.choicespecs.e_commerce_proj_user_service.model.User;
import com.choicespecs.e_commerce_proj_user_service.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * RabbitMQ command listener for the User Service.
 *
 * <p>This component consumes JSON messages from {@link RabbitMQConstants#USER_QUEUE}
 * and dispatches to {@link UserService} based on an {@code action} field in the payload.</p>
 *
 * <h2>Expected message formats</h2>
 * <ul>
 *   <li><b>CREATE</b>:
 *     <pre>{@code
 *     { "action":"create", "user": { "username": "...", "email": "...", ... } }
 *     }</pre>
 *     Deserialized to {@link User} and passed to {@link UserService#createUser(User)}.</li>
 *   <li><b>UPDATE</b>:
 *     <pre>{@code
 *     { "action":"update", "user": { "username":"...", "email": "...", ... } }
 *     }</pre>
 *     The {@code user.username} is required (path param), the rest becomes {@link UserRequest}.</li>
 *   <li><b>DELETE</b>:
 *     <pre>{@code
 *     { "action":"delete", "email":"..." }
 *     }</pre>
 *     Requires {@code email} and calls {@link UserService#deleteUser(String)}.</li>
 *   <li><b>GET</b>:
 *     <pre>{@code
 *     { "action":"get", "user": { ...selector fields... } }
 *     }</pre>
 *     Requires header {@code x-request-id}. Calls {@link UserService#getUser(UserRequest, String)}.</li>
 *   <li><b>SEARCH</b>:
 *     <pre>{@code
 *     { "action":"search", ...criteria... }
 *     }</pre>
 *     Requires header {@code x-request-id}. Calls {@link UserService#searchUser(UserSearchRequest, String)}.</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Actions are parsed case-insensitively via {@link ActionType#fromString(String)}.</li>
 *   <li>For GET/SEARCH, a correlation header {@code x-request-id} is mandatory for traceability.</li>
 *   <li>On validation/conversion errors, the listener logs a domain-specific message and returns
 *       (messages are not requeued here; configure DLQ/retry at the container level if desired).</li>
 * </ul>
 */
@Component
public class UserServiceListener {

    private static final Logger log = LoggerFactory.getLogger(UserServiceListener.class);


    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Entry point for messages arriving on {@link RabbitMQConstants#USER_QUEUE}.
     *
     * <p>Validates the presence of the {@code action} field, maps it to {@link ActionType},
     * and routes to the corresponding handler. For actions that produce response events
     * (GET/SEARCH), a {@code x-request-id} header is required.</p>
     *
     * @param jsonNode  the raw JSON payload
     * @param requestId optional correlation id (required for GET/SEARCH)
     */
    @RabbitListener(queues = RabbitMQConstants.USER_QUEUE)
    public void receiveMessage(JsonNode jsonNode,
                               @Header(name=FieldConstants.HEADER_REQUEST_ID_FIELD, required=false) String requestId) {
        try {
            if (!jsonNode.has(FieldConstants.ACTION_FIELD)) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
            }
            String actionString = jsonNode.get(FieldConstants.ACTION_FIELD).asText().toLowerCase();
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
                case SEARCH:
                    searchUser(jsonNode, requestId);
                    break;

            }
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_PROCESSING_FAIL, e);
        }
    }

    /**
     * Handles {@code create} action.
     * <p>Requires a {@code user} node. Converts it to {@link User} and delegates to {@link UserService#createUser(User)}.</p>
     *
     * @param jsonNode incoming message
     */
    private void createUser(JsonNode jsonNode) {
        try {
            if (!jsonNode.has(FieldConstants.USER_FIELD)) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
            }
            JsonNode userJson = jsonNode.get(FieldConstants.USER_FIELD);
            User user = objectMapper.treeToValue(userJson, User.class);
            userService.createUser(user);
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_CREATE_USER_FAIL, e);
        }
    }

    /**
     * Utility to fetch a required text field.
     *
     * @param node  parent node
     * @param field required field name
     * @return the text value
     * @throws IllegalArgumentException if missing, null, or non-text
     */
    private String requireText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || !v.isTextual()) {
            throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
        }
        return v.asText();
    }

    /**
     * Handles {@code delete} action.
     * <p>Requires {@code email} at the root level.</p>
     *
     * @param node incoming message
     */
    private void deleteUser(JsonNode node) {
        try {
            String email = requireText(node, FieldConstants.EMAIL_FIELD);
            userService.deleteUser(email);
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_DELETE_USER_FAIL, e);
        }
    }

    /**
     * Handles {@code update} action.
     * <p>Requires a {@code user} node; extracts {@code username} and maps the rest to {@link UserRequest}.</p>
     *
     * @param jsonNode incoming message
     */
    private void updateUser(JsonNode jsonNode) {
        try {
            if (!jsonNode.has(FieldConstants.USER_FIELD)) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
            }
            JsonNode userJson = jsonNode.get(FieldConstants.USER_FIELD);
            UserRequest request = objectMapper.treeToValue(userJson, UserRequest.class);
            String username = requireText(userJson, FieldConstants.USERNAME_FIELD);
            userService.updateUser(username, request);
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_UPDATE_USER_FAIL, e);
        }
    }


    /**
     * Handles {@code get} action (request-response via events).
     * <p>Requires header {@code x-request-id} and a {@code user} selector node which is converted to {@link UserRequest}.</p>
     *
     * @param jsonNode    incoming message
     * @param headerReqId correlation id used by downstream event publisher
     */
    private void getUser(JsonNode jsonNode, String headerReqId) {
        try {
            if (headerReqId == null || headerReqId.isBlank()) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_HEADER);
            }
            if (!jsonNode.has(FieldConstants.USER_FIELD)) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_FIELD);
            }
            JsonNode userJson = jsonNode.get(FieldConstants.USER_FIELD);
            UserRequest request = objectMapper.treeToValue(userJson, UserRequest.class);
            userService.getUser(request, headerReqId);
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_GET_USER_FAIL, e);
        }
    }

    /**
     * Handles {@code search} action (request-response via events).
     * <p>Requires header {@code x-request-id}. Converts the entire payload to {@link UserSearchRequest}.</p>
     *
     * @param jsonNode    incoming message (criteria at the root)
     * @param headerReqId correlation id used by downstream event publisher
     */
    private void searchUser(JsonNode jsonNode, String headerReqId) {
        try {
            if (headerReqId == null || headerReqId.isBlank()) {
                throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MISSING_HEADER);
            }
            UserSearchRequest userSearchRequest = objectMapper.treeToValue(jsonNode, UserSearchRequest.class);
            userService.searchUser(userSearchRequest, headerReqId);
        } catch (Exception e) {
            log.error(ErrorMessageConstants.ERROR_SEARCH_USER_FAIL, e);
        }
    }


}
