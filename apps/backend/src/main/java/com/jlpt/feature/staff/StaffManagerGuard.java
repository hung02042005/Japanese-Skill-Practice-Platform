/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staff;

import com.jlpt.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Chốt chặn quyền Staff Manager ở Service Layer.
 *
 * <p>JWT chỉ cấp authority {@code ROLE_STAFF} cho mọi nhân viên, không phân biệt
 * staff thường vs {@code staff_manager}. Các hành động mang tính quản lý (suspend học viên,
 * broadcast thông báo, duyệt nội dung) vì thế phải kiểm tra {@code staffRole} tại Service —
 * không thể dựa vào việc FE ẩn nút (xem anti-pattern "Authorization by UI hide" trong CLAUDE.md).
 */
@Component
@RequiredArgsConstructor
public class StaffManagerGuard {

    private final StaffUserRepository staffUserRepository;

    /** Trả về StaffUser nếu là staff_manager đang active; ngược lại ném 403 với message tuỳ ngữ cảnh. */
    public StaffUser requireManager(String email, String forbiddenMessage) {
        StaffUser staff =
                staffUserRepository.findByEmail(email).orElseThrow(() -> new ForbiddenException(forbiddenMessage));
        if (staff.getStaffRole() != StaffUser.StaffRole.STAFF_MANAGER
                || staff.getStatus() != StaffUser.StaffStatus.ACTIVE) {
            throw new ForbiddenException(forbiddenMessage);
        }
        return staff;
    }
}
