
package com.choicespecs.e_commerce_proj_user_service.event;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 * Event to be published when a user is updated
 */
public class UserServiceUpdatedEvent extends UserServiceEvent {
    public UserServiceUpdatedEvent(UserEntity user) {
        super(user);
    }

}
