/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.exception;

import com.jlpt.shared.common.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * UC-34 — Advice riêng cho feature, trả về body 409 kèm {@code data.errorCode}
 * (và {@code data.references} với RESOURCE_IN_USE) đúng đặc tả API mục 6.3/6.4.
 *
 * <p>Đặt {@link Order} cao hơn {@code GlobalExceptionHandler} để các exception riêng của UC-34
 * được xử lý tại đây thay vì bị handler tổng quát bắt. Không đặt trong {@code shared/} để giữ
 * nguyên tắc shared không phụ thuộc feature (refactor-layer-to-feature §Tiêu chí Shared vs Feature).
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class PublishedContentExceptionHandler {

    @ExceptionHandler(ResourceInUseException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleResourceInUse(
            ResourceInUseException resourceInUseError) {
        log.warn(
                "RESOURCE_IN_USE: {} blocking reference(s)",
                resourceInUseError.getReferences() == null
                        ? 0
                        : resourceInUseError.getReferences().size());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("errorCode", "RESOURCE_IN_USE");
        data.put("references", resourceInUseError.getReferences());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, resourceInUseError.getMessage(), data));
    }

    @ExceptionHandler(RestoreNotAllowedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleRestoreNotAllowed(
            RestoreNotAllowedException restoreNotAllowedError) {
        log.warn("RESTORE_NOT_ALLOWED: {}", restoreNotAllowedError.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, restoreNotAllowedError.getMessage(), errorData("RESTORE_NOT_ALLOWED")));
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInvalidTransition(
            InvalidStateTransitionException invalidStateTransitionError) {
        log.warn("INVALID_STATE_TRANSITION: {}", invalidStateTransitionError.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        409, invalidStateTransitionError.getMessage(), errorData("INVALID_STATE_TRANSITION")));
    }

    private Map<String, Object> errorData(String errorCode) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("errorCode", errorCode);
        return data;
    }
}
