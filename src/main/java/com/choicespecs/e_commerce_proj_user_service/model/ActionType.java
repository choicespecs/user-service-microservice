
package com.choicespecs.e_commerce_proj_user_service.model;
import com.choicespecs.e_commerce_proj_user_service.constants.ErrorMessageConstants;

/**
 * Enum for the different actions that the User Service Microservice can take 
 */
public enum ActionType {
    CREATE, UPDATE, DELETE, GET, SEARCH;

    public static ActionType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(ErrorMessageConstants.ERROR_ACTION_TYPE_NULL);
        }

        for (ActionType action : ActionType.values()) {
            if (action.name().equalsIgnoreCase(value)) {
                return action;
            }
        }

        throw new IllegalArgumentException(ErrorMessageConstants.ERROR_INVALID_ACTION_TYPE + value);
    }
}
