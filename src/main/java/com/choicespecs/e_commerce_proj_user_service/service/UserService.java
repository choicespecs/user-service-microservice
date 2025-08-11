/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.choicespecs.e_commerce_proj_user_service.dto.UserUpdateRequest;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;
import com.choicespecs.e_commerce_proj_user_service.event.EventPublisher;
import com.choicespecs.e_commerce_proj_user_service.model.User;
import com.choicespecs.e_commerce_proj_user_service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 *
 * @author metal
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;


    public UserService(UserRepository userRepository, EventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
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

    public void updateUser(String username, JsonNode jsonNode) throws JsonProcessingException {
        UserEntity userEntity = userRepository.findByUsernameIgnoreCase(username);
        UserUpdateRequest request = objectMapper.treeToValue(jsonNode, UserUpdateRequest.class);
        request.applyTo(userEntity);
        userEntity.setUpdatedAt(Instant.now());
        userRepository.save(userEntity);
        eventPublisher.publishUserUpdatedEvent(userEntity);
    }
}
