package com.yewai.classBookingSystem.common.dto;

import com.yewai.classBookingSystem.common.enums.ResponseStatus;

import java.util.Map;

public record ApiResponse<T>(
        ResponseStatus status,
        T data,
        String error,
        String message,
        Map<String, String> details
) {

    public ApiResponse(T data) {
        this(ResponseStatus.SUCCESS, data, null, null, null);
    }

    public ApiResponse(T data, String message) {
        this(ResponseStatus.SUCCESS, data, null, message, null);
    }

    public ApiResponse(String message) {
        this(ResponseStatus.SUCCESS, null, null, message, null);
    }

    public ApiResponse(String error, String message) {
        this(ResponseStatus.ERROR, null, error, message, null);
    }

    public ApiResponse(String error, String message, Map<String, String> details) {
        this(ResponseStatus.ERROR, null, error, message, details);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseStatus.SUCCESS, data, null, null, null);
    }
}

