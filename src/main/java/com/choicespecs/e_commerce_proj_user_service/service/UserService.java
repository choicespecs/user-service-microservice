/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.service;

import org.springframework.stereotype.Service;

import com.choicespecs.e_commerce_proj_user_service.event.EventPublisher;
import com.choicespecs.e_commerce_proj_user_service.model.User;
import com.choicespecs.e_commerce_proj_user_service.repository.UserRepository;

/**
 *
 * @author metal
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public UserService(UserRepository userRepository, EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public void createUser(User user) {
        userRepository.save(user.toEntity());
        eventPublisher.publishUserCreatedEvent(this);
    }
}
