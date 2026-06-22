/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.exception;

import com.jlpt.shared.common.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * UC-06 — Advice riêng cho feature learning, trả {@code data.errorCode} (LEVEL_MISMATCH,
 * CONTENT_NOT_FOUND, VALIDATION_FAILED, PROGRESS_REGRESSION) đúng đặc tả UC-06 §3/§5.
 *
 * <p>Order cao hơn {@code GlobalExceptionHandler} để exception riêng của feature này không bị
 * handler tổng quát bắt mất errorCode (theo mẫu {@code PublishedContentExceptionHandler}).
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class LearningExceptionHandler {

    @ExceptionHandler(LearningException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleLearningException(LearningException ex) {
        log.warn("{}: {}", ex.getErrorCode(), ex.getMessage());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("errorCode", ex.getErrorCode());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(ex.getStatus(), ex.getMessage(), data));
    }
}
