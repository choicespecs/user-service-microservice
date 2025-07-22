/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.model;

import lombok.Data;

/**
 *
 * @author metal
 */
@Data
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
}
 