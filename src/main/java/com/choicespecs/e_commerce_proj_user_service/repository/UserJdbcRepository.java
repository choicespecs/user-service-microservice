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
 * JDBC-based read/search repository for the {@code users} table.
 *
 * <p>This repository uses {@link NamedParameterJdbcTemplate} to issue
 * parameterized queries (protecting against SQL injection) and returns either
 * a single {@link UserEntity} or a paginated {@link Page} of {@link UserEntity}s.</p>
 *
 * <h2>Main responsibilities</h2>
 * <ul>
 *   <li><b>getUser</b>: fetch exactly one user by a single selector
 *       (username <i>or</i> email <i>or</i> phone), enforcing {@code deleted = false}.</li>
 *   <li><b>searchUserPage</b>: filtered search (free-text {@code q} or structured fields)
 *       with allow-listed sorting and pagination.</li>
 *   <li><b>countSearch</b>: count the total rows matching the same filters (for pagination).</li>
 * </ul>
 *
 * <h2>Safety & correctness</h2>
 * <ul>
 *   <li><b>Named parameters</b> prevent SQL injection.</li>
 *   <li><b>Allow-listed sort columns</b> avoid arbitrary ORDER BY injection.</li>
 *   <li><b>LIKE escaping</b> for {@code _}, {@code %}, and {@code \\} ensures user input
 *       can’t change pattern semantics.</li>
 *   <li>Case-insensitive matches are implemented with {@code LOWER(column)} and lowercased params.</li>
 * </ul>
 */
@Repository
public class UserJdbcRepository {
    private final NamedParameterJdbcTemplate jdbc;


    /**
     * Base SELECT list; includes a {@code WHERE 1=1} so callers can freely append {@code AND ...} clauses.
     * <p><b>Note:</b> Using {@code WHERE 1=1} simplifies dynamic query building.</p>
     */
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
        WHERE 1=1
        """;
    
    public UserJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    /**
     * Allow-list for external sort keys to physical column names.
     * <p>Prevents arbitrary ORDER BY injection; unknown keys fall back to {@code created_at}.</p>
     */
    private static final Map<String, String> SORT_COLUMNS = Map.of(
        "username",   "username",
        "email",      "email",
        "firstName",  "first_name",
        "lastName",   "last_name",
        "createdAt",  "created_at",
        "updatedAt",  "updated_at"
    );


    /**
     * Maps an external sort key to a known column; defaults to {@code created_at}.
     */
    private String resolveSortBy(String sortBy) {
        return SORT_COLUMNS.getOrDefault(sortBy, "created_at");
    }

    /**
     * Normalizes sort direction; only {@code desc} yields {@code DESC}, otherwise {@code ASC}.
     */
    private String resolveSortDir(String sortDir) {
        // default ASC; only allow "desc" explicitly
        return "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * Escapes a value used with SQL LIKE to treat special characters literally.
     * <p>Escapes: {@code \\}, {@code _}, and {@code %}. Must pair with {@code ESCAPE '\\'} in SQL.</p>
     */
    private String escapeLike(String s) {
        return s.replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%");
    }
    

    /**
     * Fetches a single user by exactly one selector (username OR email OR phone).
     * <p>
     * Enforces {@code deleted = false}. If zero rows match, returns {@link Optional#empty()}.
     * If more than one selector is provided, throws {@link IllegalArgumentException} with
     * {@link ErrorMessageConstants#ERROR_MORE_THAN_ONE_SELECTOR}.
     * </p>
     *
     * @param request the user request containing one and only one selector
     * @return an optional user entity
     * @throws IllegalArgumentException if not exactly one selector is provided
     */
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


    /**
     * Maps a result-set row to a {@link UserEntity}.
     * <p>Assumes non-null timestamps; guard if your schema allows nulls.</p>
     */
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
    
    /**
     * Appends WHERE filters for search operations.
     * <ul>
     *   <li>If {@code includeDeleted} is not {@code true}, enforces {@code deleted = false}.</li>
     *   <li>If {@code q} is present, applies a case-insensitive OR across username, email, first/last name,
     *       plus a phone LIKE (no lowercasing for phone).</li>
     *   <li>Otherwise, applies structured filters from {@code req.user} (exact matches for username/email/phone,
     *       LIKE for first/last name).</li>
     * </ul>
     *
     * @param sql the SQL builder (already contains {@code WHERE 1=1})
     * @param p   the parameter map to populate
     * @param req the search request
     */
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


    /**
     * Counts rows matching {@code UserSearchRequest} filters.
     *
     * @param req the search request
     * @return total number of matching rows
     */
    public long countSearch(UserSearchRequest req) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
        Map<String, Object> p = new HashMap<>();
        appendFilters(sql, p, req);
        return jdbc.queryForObject(sql.toString(), p, Long.class);
    }

    /**
     * Executes a paginated search using the provided criteria, sort, and paging info.
     *
     * <p>Defaults: page = 0 when null/negative; size = 50 when null/invalid; maximum size = 200.</p>
     *
     * @param req the search request (criteria, sortBy/sortDir, page/size)
     * @return a {@link Page} of {@link UserEntity} results
     */
    public Page<UserEntity> searchUserPage(UserSearchRequest req) {
        int page = (req.getPage() != null && req.getPage() >= 0) ? req.getPage() : 0;
        int size = (req.getSize() != null && req.getSize() > 0 && req.getSize() <= 200) ? req.getSize() : 50;

        String orderBy = resolveSortBy(req.getSortBy());
        String dir = resolveSortDir(req.getSortDir());

        StringBuilder sql = new StringBuilder(SELECT_BASE);
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
