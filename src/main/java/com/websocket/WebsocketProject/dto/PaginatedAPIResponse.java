package com.websocket.WebsocketProject.dto;

import java.util.List;

public class PaginatedAPIResponse<T> {

    private List<T> data;

    private Integer page;

    private Integer pageSize;

    private Integer totalPages;

    private Long totalElements;

    private Boolean last;

    private String message;

    public PaginatedAPIResponse() {
    }

    public PaginatedAPIResponse(
            List<T> data,
            Integer page,
            Integer pageSize,
            Integer totalPages,
            Long totalElements,
            Boolean last,
            String message) {

        this.data = data;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.last = last;
        this.message = message;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}