package com.task.smartgrocerybe.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private boolean success;
    private String message;
    private T data;
    private String path;
    private String errorCode;

    // Success responses
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Success", data);
    }

    // Error responses - Single parameter (uses default BAD_REQUEST)
    public static <T> ApiResponse<T> error(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    // Error responses with HttpStatus
    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .success(false)
                .message(message)
                .build();
    }

    // Error responses with custom status code
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .status(statusCode)
                .success(false)
                .message(message)
                .build();
    }

    // Error responses with data
    public static <T> ApiResponse<T> error(String message, HttpStatus status, T data) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    // Error responses with error code
    public static <T> ApiResponse<T> error(String message, HttpStatus status, String errorCode) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    // Convenience error methods
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(message, HttpStatus.UNAUTHORIZED);
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return error(message, HttpStatus.FORBIDDEN);
    }

    public static <T> ApiResponse<T> internalServerError(String message) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Helper method to set path
    public ApiResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }

    // Convert to ResponseEntity
    public org.springframework.http.ResponseEntity<ApiResponse<T>> toResponseEntity() {
        return org.springframework.http.ResponseEntity.status(this.status).body(this);
    }
}