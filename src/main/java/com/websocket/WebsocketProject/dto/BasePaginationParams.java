package com.websocket.WebsocketProject.dto;

import com.websocket.WebsocketProject.constants.AppConstants;

public class BasePaginationParams {

    protected Integer page = AppConstants.DEFAULT_PAGE_NUMBER;
    protected Integer pageSize = AppConstants.DEFAULT_PAGE_SIZE;
    protected String sortBy = AppConstants.DEFAULT_SORT_BY;
    protected String sortDirection = AppConstants.DEFAULT_SORT_DIRECTION;

    public BasePaginationParams() {
    }

    public BasePaginationParams(Integer page, Integer pageSize, String sortBy, String sortDirection) {

        this.page = page;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}