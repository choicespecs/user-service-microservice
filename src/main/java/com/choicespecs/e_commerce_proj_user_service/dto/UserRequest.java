/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.dto;

import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 *
 * @author christopherlee
 */
public class UserRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String username;

    public UserRequest() {}

    public UserRequest(String firstName, String lastName, String phone, String email, String username) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.username = username;
    }

    public void applyTo(UserEntity entity) {
        if (firstName != null) entity.setFirstName(firstName);
        if (lastName != null) entity.setLastName(lastName);
        if (phone != null) entity.setPhone(phone);
        if (email != null) entity.setEmail(email);
        if (username != null) entity.setUsername(username);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
