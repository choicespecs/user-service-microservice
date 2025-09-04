package com.choicespecs.e_commerce_proj_user_service.event;

import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 * Event to be published when a user is created
 */
public class UserServiceCreatedEvent extends UserServiceEvent {

    public UserServiceCreatedEvent(UserEntity user) {
        super(user);
    }
}
