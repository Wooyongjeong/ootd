package com.github.wooyong.ootd.common;

/**
 * Common API response envelope.
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    /**
     * Build a success response with data.
     */
    public static <T> ApiResponse<T> success(ResponseCode responseCode, T data) {
        return new ApiResponse<>(true, responseCode.code(), responseCode.defaultMessage(), data);
    }

    /**
     * Build a success response without data.
     */
    public static ApiResponse<Void> success(ResponseCode responseCode) {
        return new ApiResponse<>(true, responseCode.code(), responseCode.defaultMessage(), null);
    }

    /**
     * Build an error response with optional detail data.
     */
    public static <T> ApiResponse<T> error(ResponseCode responseCode, String message, T data) {
        return new ApiResponse<>(false, responseCode.code(), message, data);
    }

    /**
     * Build an error response without detail data.
     */
    public static ApiResponse<Void> error(ResponseCode responseCode, String message) {
        return new ApiResponse<>(false, responseCode.code(), message, null);
    }
}

