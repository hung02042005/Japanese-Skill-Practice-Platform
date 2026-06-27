/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff;

import com.jlpt.feature.staff.dto.response.StaffMemberResponse;
import com.jlpt.shared.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Staff Manager — danh sach nhan vien ho tro de chon nguoi giao ticket (assignee picker, UC-29).
 * Phan quyen Manager duoc kiem tra o service (staffRole == STAFF_MANAGER).
 */
@RestController
@RequestMapping("/api/staff/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffMemberController {

    private final StaffMemberService staffMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffMemberResponse>>> listAssignable(
            Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.success(staffMemberService.listAssignableStaff(authentication.getName())));
    }
}
