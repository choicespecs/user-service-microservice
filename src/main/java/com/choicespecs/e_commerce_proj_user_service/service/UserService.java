package com.choicespecs.e_commerce_proj_user_service.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.choicespecs.e_commerce_proj_user_service.dto.UserRequest;
import com.choicespecs.e_commerce_proj_user_service.dto.UserSearchRequest;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;
import com.choicespecs.e_commerce_proj_user_service.event.EventPublisher;
import com.choicespecs.e_commerce_proj_user_service.model.User;
import com.choicespecs.e_commerce_proj_user_service.repository.UserJdbcRepository;
import com.choicespecs.e_commerce_proj_user_service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Application service for user operations.
 *
 * <p>This service coordinates persistence using Spring Data's {@link UserRepository}
 * for writes (create/update/soft-delete) and a JDBC repository {@link UserJdbcRepository}
 * for read-optimized lookups (GET and SEARCH). After successful operations it
 * publishes domain events via {@link EventPublisher} so downstream services can
 * react asynchronously.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Create a new user and emit a {@code user.created} event.</li>
 *   <li>Soft-delete a user (set {@code deleted=true}) and emit a {@code user.deleted} event.</li>
 *   <li>Update mutable fields on a user and emit a {@code user.updated} event.</li>
 *   <li>Handle GET flows by querying via JDBC and emitting one of:
 *       <ul>
 *         <li>{@code user.get} (found)</li>
 *         <li>{@code user.get} (not-found)</li>
 *         <li>{@code user.get} (error)</li>
 *       </ul>
 *   </li>
 *   <li>Handle SEARCH flows by executing a paginated query and emitting either
 *       {@code user.search} (success) or {@code user.search} (error).</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Timestamps: {@code updatedAt} is refreshed on update and delete.</li>
 *   <li>Error policy for GET/SEARCH: exceptions are caught and converted to error events
 *       (callers rely on events rather than thrown exceptions).</li>
 * </ul>
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserJdbcRepository userJdbcRepository;
    private final EventPublisher eventPublisher;



    /**
     * Creates a {@link UserService}.
     *
     * @param userRepository     Spring Data repository for writes/basic reads
     * @param userJdbcRepository Read-optimized JDBC repository for GET/SEARCH
     * @param eventPublisher     Domain event publisher
     */
    public UserService(UserRepository userRepository, UserJdbcRepository userJdbcRepository, EventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.userJdbcRepository = userJdbcRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new user and publishes a {@code user.created} event.
     *
     * <p>Converts the inbound model to an entity, persists it, then emits the event.
     * Consider adding uniqueness checks (e.g., username/email) at the DB or service layer
     * to prevent duplicates.</p>
     *
     * @param user API/model object with required fields
     */
    public void createUser(User user) {
        UserEntity userEntity = user.toEntity();
        userRepository.save(userEntity);
        eventPublisher.publishUserCreatedEvent(userEntity);
    }

        /**
     * Soft-deletes a user by email and publishes a {@code user.deleted} event.
     *
     * <p>Marks {@code deleted=true} and updates {@code updatedAt}. If no user exists
     * for the provided email, {@link UserRepository#findByEmail(String)} may return
     * {@code null}; consider hardening to avoid {@link NullPointerException}.</p>
     *
     * @param email unique email of the user to delete
     */
    public void deleteUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        userEntity.setDeleted(true);
        userEntity.setUpdatedAt(Instant.now());
        userRepository.save(userEntity);    
        eventPublisher.publishUserDeletedEvent(userEntity);
    }

    /**
     * Updates an existing user identified by {@code username} and publishes a {@code user.updated} event.
     *
     * <p>Applies a partial update from {@link UserRequest#applyTo(UserEntity)}, refreshes the
     * {@code updatedAt} timestamp, persists, then emits the event. If the username isn't found,
     * {@link UserRepository#findByUsernameIgnoreCase(String)} may return {@code null}; consider
     * enforcing a not-found policy.</p>
     *
     * @param username case-insensitive username of the target user
     * @param request  fields to update
     * @throws JsonProcessingException if any serialization logic inside {@code request.applyTo(...)} throws
     */
    public void updateUser(String username, UserRequest request) throws JsonProcessingException {
        UserEntity userEntity = userRepository.findByUsernameIgnoreCase(username);
        request.applyTo(userEntity);
        userEntity.setUpdatedAt(Instant.now());
        userRepository.save(userEntity);
        eventPublisher.publishUserUpdatedEvent(userEntity);
    }


    /**
     * Executes a GET flow and publishes one of: found / not-found / error events.
     *
     * <p>Uses the JDBC repository to enforce “exactly one selector” semantics and to filter
     * out deleted rows. Results are sent back over Rabbit via {@link EventPublisher} with the
     * provided {@code headerReqId} for correlation.</p>
     *
     * @param request     selection criteria (exactly one of username/email/phone)
     * @param headerReqId correlation id (from message header) used in the emitted event
     * @throws JsonProcessingException included for parity with surrounding API; not thrown here directly
     */
    public void getUser(UserRequest request, String headerReqId) throws JsonProcessingException{
        try {
            Optional<UserEntity> opt = userJdbcRepository.getUser(request);
            if (opt.isPresent()) {
                eventPublisher.publishUserReadEvent(headerReqId, opt.get());
            } else {
                eventPublisher.publishUserGetNotFound(headerReqId);
            }   
        } catch (Exception e) {
            eventPublisher.publishUserGetError(headerReqId, e.getMessage());
        }
    }

    /**
     * Executes a SEARCH flow and publishes success or error events.
     *
     * <p>Delegates to the JDBC repository for filtered, paginated results, then emits
     * a {@code user.search} event carrying the page payload and metadata.</p>
     *
     * @param req       search criteria, sort, and paging options
     * @param requestId correlation id carried through to the emitted event
     */
    public void searchUser(UserSearchRequest req, String requestId) {
        try {
            Page<UserEntity> page = userJdbcRepository.searchUserPage(req);
            eventPublisher.publishUserSearchSuccess(requestId, req, page);
        } catch (Exception e) {
            eventPublisher.publishUserSearchError(requestId, req, e.getMessage());
        }
    }
}
