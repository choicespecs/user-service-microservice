/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.model;

/**
 *
 * @author christopherlee
 */
public enum UserField {
    EMAIL, USERNAME, PHONE, FIRSTNAME, LASTNAME;

    public static ActionType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserField cannot be null or blank");
        }

        for (ActionType action : ActionType.values()) {
            if (action.name().equalsIgnoreCase(value)) {
                return action;
            }
        }

        throw new IllegalArgumentException("Invalid UserField: " + value);
    }
}
