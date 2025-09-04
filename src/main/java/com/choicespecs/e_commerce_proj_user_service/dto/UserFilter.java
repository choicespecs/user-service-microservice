package com.choicespecs.e_commerce_proj_user_service.dto;

/**
 * This is used within {@link UserSearchRequest}
 * In the search request, this is just used to keep the filter for search query
 * specifically as it related to the User
 * @author christopherlee
 */
public class UserFilter {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String username;

    public UserFilter() {}

    public UserFilter(String firstName, String lastName, String phone, String email, String username) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.username = username;
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
