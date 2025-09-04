package com.choicespecs.e_commerce_proj_user_service.dto;
/**
 * DTO for user search request payload
 * @author christopherlee
 */
public class UserSearchRequest {

    private String q;
    private UserFilter user;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDir;
    private Boolean includeDeleted;

    public UserSearchRequest() {}

    public UserSearchRequest(String q, UserFilter user, Integer page, Integer size, String sortBy, String sortDir, Boolean includeDeleted) {
        this.q = q;
        this.user = user;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDir = sortDir;
        this.includeDeleted = includeDeleted;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public UserFilter getUser() {
        return user;
    }

    public void setUser(UserFilter user) {
        this.user = user;
    }

    public Integer getPage() {
        return page;
    }


    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public Boolean getIncludeDeleted() {
        return includeDeleted;
    }

    public void setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }

}
