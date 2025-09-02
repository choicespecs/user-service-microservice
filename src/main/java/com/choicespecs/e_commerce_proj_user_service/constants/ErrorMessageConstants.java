/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.constants;

/**
 *
 * @author christopherlee
 */
public class ErrorMessageConstants {

    private ErrorMessageConstants() {}

    public static final String ERROR_MISSING_FIELD = "Missing required fields in message";
    public static final String ERROR_MISSING_HEADER = "Missing required header in message";
    public static final String ERROR_PROCESSING_FAIL = "Failed to process message";
    public static final String ERROR_CREATE_USER_FAIL = "Failed to create user";
    public static final String ERROR_DELETE_USER_FAIL = "Failed to delete user";
    public static final String ERROR_UPDATE_USER_FAIL = "Failed to update user";
    public static final String ERROR_GET_USER_FAIL = "Failed to get user";
    public static final String ERROR_SEARCH_USER_FAIL = "Failed to search user";
    public static final String ERROR_UNSUPPORTED_ACTION = "Unsupported action: ";


    public static final String ERROR_ACTION_TYPE_NULL = "ActionType cannot be null or blank";
    public static final String ERROR_INVALID_ACTION_TYPE = "Invalid ActionType: ";

    public static final String ERROR_MORE_THAN_ONE_SELECTOR = "Provide exactly one selector";
}
