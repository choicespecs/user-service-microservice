/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.event;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 *
 * @author christopherlee
 */
public class UserServiceGetEvent extends UserServiceEvent {

    public UserServiceGetEvent(UserEntity user) {
        super(user);
    }
}
