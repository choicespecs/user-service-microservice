package com.choicespecs.e_commerce_proj_user_service.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.choicespecs.e_commerce_proj_user_service.dto.UserSearchRequest;
import com.choicespecs.e_commerce_proj_user_service.entity.UserEntity;

/**
 * Search event that does not correspond to a single UserEntity.
 * userId/email fields in the abstract base remain null intentionally.
 */
public class UserServiceSearchEvent extends UserServiceEvent {
    private String type;            // "SEARCH_SUCCESS" | "SEARCH_ERROR"
    private Instant at;
    private String requestId;

    // Echo back query params (good for audit/debug)
    private String q;
    private Boolean includeDeleted;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDir;

    // Results (for success)
    private Long totalElements;
    private Integer totalPages;
    private Integer returnedCount;
    private List<UserEntity> content;

    // Error (for error)
    private String error;

    // --- Constructors ---

    // No-arg for serializers
    public UserServiceSearchEvent() {
        super();
    }

    private UserServiceSearchEvent(
            String type,
            String requestId,
            UserSearchRequest req,
            Long totalElements,
            Integer totalPages,
            List<UserEntity> content,
            String error
    ) {
        super(); // leaves userId/email null (expected for search)
        // minimally populate base fields for traceability
        setEventId(UUID.randomUUID());
        setCreatedAt(Instant.now()); // reuse base timestamp; or keep separate via 'at' below

        this.type = type;
        this.at = Instant.now();
        this.requestId = requestId;

        // Echo req (null-safe: if req is null, leave fields null)
        if (req != null) {
            this.q = req.getQ();
            this.includeDeleted = req.getIncludeDeleted();
            this.page = req.getPage();
            this.size = req.getSize();
            this.sortBy = req.getSortBy();
            this.sortDir = req.getSortDir();
        }

        // Results
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.content = content;
        this.returnedCount = (content == null ? 0 : content.size());

        // Error
        this.error = error;
    }

    // --- Factories ---

    public static UserServiceSearchEvent success(String requestId, UserSearchRequest req,
                                                 long totalElements, int totalPages,
                                                 List<UserEntity> content) {
        return new UserServiceSearchEvent(
            "SEARCH_SUCCESS", requestId, req, totalElements, totalPages, content, null
        );
    }

    public static UserServiceSearchEvent error(String requestId, UserSearchRequest req, String message) {
        return new UserServiceSearchEvent(
            "SEARCH_ERROR", requestId, req, null, null, null, message
        );
    }

    // --- Getters/Setters (needed by Jackson) ---

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getAt() { return at; }
    public void setAt(Instant at) { this.at = at; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public Boolean getIncludeDeleted() { return includeDeleted; }
    public void setIncludeDeleted(Boolean includeDeleted) { this.includeDeleted = includeDeleted; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }

    public Long getTotalElements() { return totalElements; }
    public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public Integer getReturnedCount() { return returnedCount; }
    public void setReturnedCount(Integer returnedCount) { this.returnedCount = returnedCount; }

    public List<UserEntity> getContent() { return content; }
    public void setContent(List<UserEntity> content) { this.content = content; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}