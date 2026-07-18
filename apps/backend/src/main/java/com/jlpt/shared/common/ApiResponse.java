/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private String message;
    private String code;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("Thao tác thành công.")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().status(200).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message("Tạo mới thành công.")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder().status(201).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder().status(status).message(message).build();
    }

    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Lỗi kèm mã máy-đọc (errorCode) để client phân biệt loại lỗi mà không phải
     * dò chuỗi message tiếng Việt. Tách tên riêng (không overload `error`) để
     * tránh đụng chữ ký `error(int, String, T data)` khi T = String.
     */
    public static <T> ApiResponse<T> errorWithCode(int status, String message, String code) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .code(code)
                .build();
    }
}
