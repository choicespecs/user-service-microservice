/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.choicespecs.e_commerce_proj_user_service.dto;

/**
 *
 * @author christopherlee
 */
public class PageResult<T> {
    private Integer page;
    private Integer size;
    private Long total;
    private java.util.List<T> items;

    public PageResult(Integer page, Integer size, Long total, java.util.List<T> items) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.items = items;
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

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public java.util.List<T> getItems() {
        return items;
    }

    public void setItems(java.util.List<T> items) {
        this.items = items;
    }
}
