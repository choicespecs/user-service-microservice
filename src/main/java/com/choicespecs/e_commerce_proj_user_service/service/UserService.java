        /*
        * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
        * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
        */

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
         *
         * @author metal
         */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserJdbcRepository userJdbcRepository;
    private final EventPublisher eventPublisher;


    public UserService(UserRepository userRepository, UserJdbcRepository userJdbcRepository, EventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.userJdbcRepository = userJdbcRepository;
        this.eventPublisher = eventPublisher;
    }

    public void createUser(User user) {
        UserEntity userEntity = user.toEntity();
        userRepository.save(userEntity);
        eventPublisher.publishUserCreatedEvent(userEntity);
    }

    public void deleteUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        userEntity.setDeleted(true);
        userEntity.setUpdatedAt(Instant.now());
        userRepository.save(userEntity);    
        eventPublisher.publishUserDeletedEvent(userEntity);
    }

    public void updateUser(String username, UserRequest request) throws JsonProcessingException {
        UserEntity userEntity = userRepository.findByUsernameIgnoreCase(username);
        request.applyTo(userEntity);
        userEntity.setUpdatedAt(Instant.now());
        userRepository.save(userEntity);
        eventPublisher.publishUserUpdatedEvent(userEntity);
    }

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

    public void searchUser(UserSearchRequest req, String requestId) {
        try {
            Page<UserEntity> page = userJdbcRepository.searchUserPage(req);
            eventPublisher.publishUserSearchSuccess(requestId, req, page);
        } catch (Exception e) {
            eventPublisher.publishUserSearchError(requestId, req, e.getMessage());
        }
    }
}
