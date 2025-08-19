/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.event;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;
import com.choicespecs.e_commerce_proj_user_service.model.User;


/**
 *
 * @author christopherlee
 */
public class UserServiceGetEvent extends UserServiceEvent {

    private User user;

    public UserServiceGetEvent(UserEntity user) {
        super(user);
        this.user = user.toUser();
    }
}
