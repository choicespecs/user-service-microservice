/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.model;

/**
 *
 * @author metal
 */
public class User {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    

    public User() {}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id) &&
                username != null && username.equals(user.username) &&
                email != null && email.equals(user.email) &&
                phone != null && phone.equals(user.phone) &&
                firstName != null && firstName.equals(user.firstName) &&
                lastName != null && lastName.equals(user.lastName);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() :
                0);
        result = 31 * result + (email != null ? email.hashCode() :
                0);
        result = 31 * result + (phone != null ? phone.hashCode() :
                0); 
        result = 31 * result + (firstName != null ? firstName.hashCode() :
                0);
        result = 31 * result + (lastName != null ? lastName.hashCode() :
                0);
        return result;  
    }
}
 