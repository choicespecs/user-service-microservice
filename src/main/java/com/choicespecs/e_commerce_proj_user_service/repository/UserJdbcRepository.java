/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.choicespecs.e_commerce_proj_user_service.constants.ErrorMessageConstants;
import com.choicespecs.e_commerce_proj_user_service.constants.FieldConstants;
import com.choicespecs.e_commerce_proj_user_service.dto.UserRequest;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 *
 * @author christopherlee
 */
@Repository
public class UserJdbcRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private static final String USER_SELECT_SQL = """
            SELECT 
                id, 
                first_name, 
                last_name, 
                phone, 
                email, 
                username, 
                deleted,
                created_at,
                updated_at
            FROM users
            WHERE deleted = false
            """;

    public UserJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
    
    public Optional<UserEntity> getUser(UserRequest request) {
        StringBuilder sql = new StringBuilder(USER_SELECT_SQL);
        Map<String, Object> p = new HashMap<>();
        int selectors = 0;
        if (notBlank(request.getUsername())) {
            sql.append(" AND LOWER(username) = :username");
            p.put(FieldConstants.USER_USERNAME_FIELD, request.getUsername().toLowerCase());
            selectors++;
        }
        if (notBlank(request.getEmail())) {
            sql.append(" AND LOWER(email) = :email");
            p.put(FieldConstants.USER_EMAIL_FIELD, request.getEmail().toLowerCase());
            selectors++;
        }

        if (notBlank(request.getPhone())) {
            sql.append(" AND phone = :phone");
            p.put(FieldConstants.USER_PHONE_FIELD, request.getPhone());
            selectors++;
        }

        if (selectors != 1) throw new IllegalArgumentException(ErrorMessageConstants.ERROR_MORE_THAN_ONE_SELECTOR);
        sql.append(" LIMIT 1");
        List<UserEntity> list = jdbc.query(sql.toString(), p, rowMapper());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

        private RowMapper<UserEntity> rowMapper() {
            return (rs, i) -> {
                UserEntity e = new UserEntity();
                e.setId(rs.getObject("id", java.util.UUID.class));
                e.setFirstName(rs.getString("first_name"));
                e.setLastName(rs.getString("last_name"));
                e.setPhone(rs.getString("phone"));
                e.setEmail(rs.getString("email"));
                e.setUsername(rs.getString("username"));
                e.setDeleted(rs.getBoolean("deleted"));
                e.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                e.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                return e;
            };
        }

}
