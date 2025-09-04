
package com.choicespecs.e_commerce_proj_user_service.event;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 * Event to be published when a user is deleted
 */
public class UserServiceDeletedEvent extends UserServiceEvent {
    public UserServiceDeletedEvent(UserEntity user) {
        super(user);
    }
}
