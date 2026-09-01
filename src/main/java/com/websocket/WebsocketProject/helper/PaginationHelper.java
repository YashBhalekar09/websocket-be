package com.websocket.WebsocketProject.helper;

import com.websocket.WebsocketProject.dto.PaginatedAPIResponse;

import java.util.List;

public class PaginationHelper {

    private PaginationHelper() {
    }

    public static <T> PaginatedAPIResponse<T> createPaginationResponse(List<T> data, int page, int pageSize, int totalPages, long totalElements, boolean last, String message) {

        return new PaginatedAPIResponse<>(data, page, pageSize, totalPages, totalElements, last, message);
    }
}