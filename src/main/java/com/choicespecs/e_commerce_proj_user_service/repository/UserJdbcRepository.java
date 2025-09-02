/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.choicespecs.e_commerce_proj_user_service.constants.ErrorMessageConstants;
import com.choicespecs.e_commerce_proj_user_service.constants.FieldConstants;
import com.choicespecs.e_commerce_proj_user_service.dto.UserRequest;
import com.choicespecs.e_commerce_proj_user_service.dto.UserSearchRequest;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 *
 * @author christopherlee
 */
@Repository
public class UserJdbcRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private static final String SELECT_BASE = """
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
        """;
    
    public UserJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final Map<String, String> SORT_COLUMNS = Map.of(
        "username",   "username",
        "email",      "email",
        "firstName",  "first_name",
        "lastName",   "last_name",
        "createdAt",  "created_at",
        "updatedAt",  "updated_at"
    );

    private String resolveSortBy(String sortBy) {
        return SORT_COLUMNS.getOrDefault(sortBy, "created_at");
    }

    private String resolveSortDir(String sortDir) {
        // default ASC; only allow "desc" explicitly
        return "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String escapeLike(String s) {
        return s.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%");
    }
    
    public Optional<UserEntity> getUser(UserRequest request) {
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        Map<String, Object> p = new HashMap<>();
        sql.append(" AND deleted = false");
        int selectors = 0;
        if (notBlank(request.getUsername())) {
            sql.append(" AND LOWER(username) = :username");
            p.put(FieldConstants.USERNAME_FIELD, request.getUsername());
            selectors++;
        }
        if (notBlank(request.getEmail())) {
            sql.append(" AND LOWER(email) = :email");
            p.put(FieldConstants.EMAIL_FIELD, request.getEmail());
            selectors++;
        }

        if (notBlank(request.getPhone())) {
            sql.append(" AND phone = :phone");
            p.put(FieldConstants.PHONE_FIELD, request.getPhone());
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
            e.setId(rs.getObject(FieldConstants.ID_FIELD, java.util.UUID.class));
            e.setFirstName(rs.getString(FieldConstants.FIRST_NAME_FIELD));
            e.setLastName(rs.getString(FieldConstants.LAST_NAME_FIELD));
            e.setPhone(rs.getString(FieldConstants.PHONE_FIELD));
            e.setEmail(rs.getString(FieldConstants.EMAIL_FIELD));
            e.setUsername(rs.getString(FieldConstants.USERNAME_FIELD));
            e.setDeleted(rs.getBoolean(FieldConstants.DELETED_FIELD));
            e.setCreatedAt(rs.getTimestamp(FieldConstants.CREATED_AT_FIELD).toInstant());
            e.setUpdatedAt(rs.getTimestamp(FieldConstants.UPDATED_AT_FIELD).toInstant());
            return e;
        };
    }
    
    private void appendFilters(StringBuilder sql, Map<String, Object> p, UserSearchRequest req) {
        boolean includeDeleted = Boolean.TRUE.equals(req.getIncludeDeleted());
        if (!includeDeleted) {
            sql.append(" AND deleted = false");
        }

        boolean hasQ = notBlank(req.getQ());
        if (hasQ) {
            String qp = "%" + escapeLike(req.getQ().toLowerCase()) + "%";
            sql.append("""
                AND (
                    LOWER(username)     LIKE :q ESCAPE '\\'
                    OR LOWER(email)     LIKE :q ESCAPE '\\'
                    OR LOWER(first_name) LIKE :q ESCAPE '\\'
                    OR LOWER(last_name)  LIKE :q ESCAPE '\\'
                    OR phone            LIKE :q_phone ESCAPE '\\'
                )
            """);
            p.put("q", qp);
            p.put("q_phone", "%" + escapeLike(req.getQ()) + "%");
        } else if (req.getUser() != null) {
            var f = req.getUser();
            if (notBlank(f.getUsername())) {
                sql.append(" AND LOWER(username) = LOWER(:username)");
                p.put("username", f.getUsername());
            }
            if (notBlank(f.getEmail())) {
                sql.append(" AND LOWER(email) = LOWER(:email)");
                p.put("email", f.getEmail());
            }
            if (notBlank(f.getFirstName())) {
                sql.append(" AND LOWER(first_name) LIKE :firstName ESCAPE '\\'");
                p.put("firstName", "%" + escapeLike(f.getFirstName().toLowerCase()) + "%");
            }
            if (notBlank(f.getLastName())) {
                sql.append(" AND LOWER(last_name) LIKE :lastName ESCAPE '\\'");
                p.put("lastName", "%" + escapeLike(f.getLastName().toLowerCase()) + "%");
            }
            if (notBlank(f.getPhone())) {
                sql.append(" AND phone = :phone");
                p.put("phone", f.getPhone());
            }
        }
    }

    public long countSearch(UserSearchRequest req) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
        Map<String, Object> p = new HashMap<>();
        appendFilters(sql, p, req);
        return jdbc.queryForObject(sql.toString(), p, Long.class);
    }

    public Page<UserEntity> searchUserPage(UserSearchRequest req) {
        int page = (req.getPage() != null && req.getPage() >= 0) ? req.getPage() : 0;
        int size = (req.getSize() != null && req.getSize() > 0 && req.getSize() <= 200) ? req.getSize() : 50;

        String orderBy = resolveSortBy(req.getSortBy());
        String dir = resolveSortDir(req.getSortDir());

        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1=1");
        Map<String, Object> p = new HashMap<>();
        appendFilters(sql, p, req);

        sql.append(" ORDER BY ").append(orderBy).append(" ").append(dir);
        sql.append(" LIMIT :limit OFFSET :offset");
        p.put("limit", size);
        p.put("offset", page * size);

        List<UserEntity> rows = jdbc.query(sql.toString(), p, rowMapper());
        long total = countSearch(req);

        return new PageImpl<>(rows, PageRequest.of(page, size), total);
    }
}
