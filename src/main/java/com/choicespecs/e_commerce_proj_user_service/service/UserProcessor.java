/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.service;

import org.springframework.stereotype.Service;
import com.choicespecs.e_commerce_proj_user_service.model.User;
/**
 *
 * @author metal
 */
@Service
public class UserProcessor {
    public void handle(User user) {
        System.out.println("Processing user: " + user.getUsername());
    }
}
