package com.choicespecs.e_commerce_proj_user_service.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.choicespecs.e_commerce_proj_user_service.constants.FieldConstants;
import com.choicespecs.e_commerce_proj_user_service.dto.UserRequest;
import com.choicespecs.e_commerce_proj_user_service.model.User;
import com.choicespecs.e_commerce_proj_user_service.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceListenerTest {

    @Mock
    UserService userService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    UserServiceListener listener;

    // Use a real mapper just to build JsonNodes for inputs (treeToValue is mocked on the listener's mapper)
    private static final ObjectMapper REAL = new ObjectMapper();

    private JsonNode obj(String json) {
        try {
            return REAL.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("receiveMessage dispatches by action")
    class DispatchTests {

        @Test
        void create_callsUserServiceCreateUser() throws Exception {
            // given
            JsonNode userJson = obj("{\"email\":\"j@e.com\",\"username\":\"john\"}");
            JsonNode payload = REAL.createObjectNode()
                    .put(FieldConstants.ACTION_FIELD, "CREATE")
                    .set(FieldConstants.USER_FIELD, userJson);

            User mapped = new User(); // adapt if your User requires fields
            when(objectMapper.treeToValue(userJson, User.class)).thenReturn(mapped);

            // when
            listener.receiveMessage(payload, "req-1");

            // then
            verify(userService).createUser(mapped);
            verifyNoMoreInteractions(userService);
        }

        @Test
        void delete_callsUserServiceDeleteUser() {
            // given
            String email = "x@example.com";
            JsonNode payload = obj("{"
                    + "\""+FieldConstants.ACTION_FIELD+"\":\"DELETE\","
                    + "\""+FieldConstants.EMAIL_FIELD+"\":\""+email+"\""
                    + "}");

            // when
            listener.receiveMessage(payload, "req-2");

            // then
            verify(userService).deleteUser(email);
            verifyNoMoreInteractions(userService);
        }

        @Test
        void update_callsUserServiceUpdateUser_withUserJsonAndUsername() throws Exception {
            // given
            JsonNode userJson = obj("{\"username\":\"alice\",\"other\":\"v\"}");
            JsonNode payload = REAL.createObjectNode()
                    .put(FieldConstants.ACTION_FIELD, "UPDATE")
                    .set(FieldConstants.USER_FIELD, userJson);
            UserRequest request = objectMapper.treeToValue(userJson, UserRequest.class);

            // when
            listener.receiveMessage(payload, "req-3");

            // then
            verify(userService).updateUser("alice", request);
            verifyNoMoreInteractions(userService);
        }

        @Test
        void get_callsUserServiceGetUser_requiresHeaderRequestId() throws Exception {
            // given
            JsonNode userJson = obj("{\"email\":\"get@example.com\"}");
            JsonNode payload = REAL.createObjectNode()
                    .put(FieldConstants.ACTION_FIELD, "GET")
                    .set(FieldConstants.USER_FIELD, userJson);
            UserRequest request = objectMapper.treeToValue(userJson, UserRequest.class);

            // when
            listener.receiveMessage(payload, "req-123");

            // then
            verify(userService).getUser(request, "req-123");
            verifyNoMoreInteractions(userService);
        }
    }

    @Nested
    @DisplayName("receiveMessage handles missing fields / errors gracefully")
    class ErrorPathTests {

        @Test
        void missingAction_doesNotCallService() {
            JsonNode payload = obj("{\"foo\":1}");
            listener.receiveMessage(payload, "req-x");
            verifyNoInteractions(userService);
        }

        @Test
        void unknownAction_doesNotCallService() {
            JsonNode payload = obj("{\""+FieldConstants.ACTION_FIELD+"\":\"NOPE\"}");
            listener.receiveMessage(payload, "req-x");
            verifyNoInteractions(userService);
        }

        @Test
        void create_missingUserField_doesNotCallService() {
            JsonNode payload = obj("{\""+FieldConstants.ACTION_FIELD+"\":\"CREATE\"}");
            listener.receiveMessage(payload, "req-x");
            verifyNoInteractions(userService);
        }

        @Test
        void delete_missingEmail_doesNotCallService() {
            JsonNode payload = obj("{\""+FieldConstants.ACTION_FIELD+"\":\"DELETE\"}");
            listener.receiveMessage(payload, "req-x");
            verifyNoInteractions(userService);
        }

        @Test
        void delete_nonTextEmail_doesNotCallService() {
            JsonNode payload = obj("{\""+FieldConstants.ACTION_FIELD+"\":\"DELETE\",\""+FieldConstants.EMAIL_FIELD+"\":123}");
            listener.receiveMessage(payload, "req-x");
            verifyNoInteractions(userService);
        }

        @Test
        void update_missingUserField_doesNotCallService() {
            JsonNode payload = obj("{\""+FieldConstants.ACTION_FIELD+"\":\"UPDATE\"}");
            listener.receiveMessage(payload, "req-x");
            verifyNoInteractions(userService);
        }

        @Test
        void update_missingUsername_doesNotCallService() {
            JsonNode userJson = obj("{\"notusername\":\"x\"}");
            JsonNode payload = REAL.createObjectNode()
                    .put(FieldConstants.ACTION_FIELD, "UPDATE")
                    .set(FieldConstants.USER_FIELD, userJson);

            listener.receiveMessage(payload, "req-x");
            verifyNoInteractions(userService);
        }

        @Test
        void get_missingHeaderRequestId_doesNotCallService() {
            JsonNode userJson = obj("{\"email\":\"a@b.c\"}");
            JsonNode payload = REAL.createObjectNode()
                    .put(FieldConstants.ACTION_FIELD, "GET")
                    .set(FieldConstants.USER_FIELD, userJson);

            listener.receiveMessage(payload, null);    // missing header
            listener.receiveMessage(payload, "   ");   // blank header

            verifyNoInteractions(userService);
        }

        @Test
        void get_missingUserField_doesNotCallService() {
            JsonNode payload = obj("{\""+FieldConstants.ACTION_FIELD+"\":\"GET\"}");
            listener.receiveMessage(payload, "req-ok");
            verifyNoInteractions(userService);
        }
    }
}