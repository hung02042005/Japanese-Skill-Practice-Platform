/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.shared.common.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * ADR-008: mọi exception phải ra khỏi API dưới dạng {@code {status, message, data}} với đúng HTTP
 * status — test từng handler của @RestControllerAdvice.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void resourceNotFound_maps404() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleNotFound(new ResourceNotFoundException("Không tìm thấy người dùng"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Không tìm thấy người dùng", response.getBody().getMessage());
    }

    @Test
    void noResourceFound_maps404WithGenericMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleNoResourceFound(
                new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/khong-ton-tai"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Không tìm thấy tài nguyên yêu cầu.", response.getBody().getMessage());
    }

    @Test
    void businessException_keepsItsOwnStatusAndErrorCode() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBusinessException(new BusinessException(429, "TOO_MANY_REQUESTS", "Quá nhiều yêu cầu"));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(429, response.getBody().getStatus());
        assertEquals("TOO_MANY_REQUESTS", response.getBody().getCode());
        assertEquals("Quá nhiều yêu cầu", response.getBody().getMessage());
    }

    @Test
    void badRequest_maps400() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBadRequest(new BadRequestException("Loại người dùng không hợp lệ"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void forbidden_maps403WithOriginalMessage() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleForbidden(new ForbiddenException("Không thể tự sửa tài khoản của mình"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Không thể tự sửa tài khoản của mình", response.getBody().getMessage());
    }

    @Test
    void duplicateResource_maps409() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleConflict(new DuplicateResourceException("Email đã được sử dụng"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void businessRule_maps422() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBusinessRule(new BusinessRuleException("Không thể xóa Admin"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(422, response.getBody().getStatus());
    }

    @Test
    void accessDenied_maps403WithGenericMessageNotLeakingDetails() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(
                new org.springframework.security.access.AccessDeniedException("ROLE_ADMIN required on /internal"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(
                "Bạn không có quyền thực hiện thao tác này.", response.getBody().getMessage());
    }

    @Test
    void constraintViolation_joinsFieldMessagesAndKeepsOnlyLeafFieldName() {
        ConstraintViolation<?> nested = violation("createStaff.request.email", "không đúng định dạng");
        ConstraintViolation<?> flat = violation("fullName", "không được để trống");

        ResponseEntity<ApiResponse<String>> response =
                handler.handleConstraintViolation(new ConstraintViolationException(Set.of(nested, flat)));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Dữ liệu không hợp lệ.", response.getBody().getMessage());
        String detail = response.getBody().getData();
        assertTrue(detail.contains("email: không đúng định dạng"));
        assertTrue(detail.contains("fullName: không được để trống"));
        assertFalse(detail.contains("createStaff.request"));
    }

    private ConstraintViolation<?> violation(String propertyPath, String message) {
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(propertyPath);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    @Test
    void illegalArgument_maps400WithPrefixedMessage() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("No enum constant N9"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Dữ liệu không hợp lệ: No enum constant N9", response.getBody().getMessage());
    }

    @Test
    void methodArgumentNotValid_joinsAllFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(
                        new FieldError("request", "email", "không được để trống"),
                        new FieldError("request", "password", "tối thiểu 8 ký tự")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiResponse<String>> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "email: không được để trống, password: tối thiểu 8 ký tự",
                response.getBody().getData());
    }

    @Test
    void optimisticLocking_maps409() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleOptimisticLocking(new ObjectOptimisticLockingFailureException(Object.class, 1L));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("thao tác đồng thời"));
    }

    @Test
    void unexpectedException_maps500WithoutLeakingStackTrace() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleGeneral(new NullPointerException("user.getEmail() is null at line 42"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(
                "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau.",
                response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("NullPointerException"));
    }
}
