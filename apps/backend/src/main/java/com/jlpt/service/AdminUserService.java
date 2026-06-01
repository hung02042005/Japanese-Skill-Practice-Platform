/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import com.jlpt.dto.request.UpdateUserRoleRequest;
import com.jlpt.dto.request.UpdateUserStatusRequest;
import com.jlpt.dto.response.AdminUserResponse;
import com.jlpt.entity.AdminUser;
import com.jlpt.entity.StaffUser;
import com.jlpt.entity.StudentUser;
import com.jlpt.exception.BadRequestException;
import com.jlpt.exception.DuplicateResourceException;
import com.jlpt.exception.ForbiddenException;
import com.jlpt.exception.ResourceNotFoundException;
import com.jlpt.repository.AdminUserRepository;
import com.jlpt.repository.StaffUserRepository;
import com.jlpt.repository.StudentUserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final StudentUserRepository studentUserRepository;
    private final StaffUserRepository staffUserRepository;
    private final AdminUserRepository adminUserRepository;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listAllUsers() {
        List<AdminUserResponse> result = new ArrayList<>();
        studentUserRepository.findAll().stream().map(this::toResponse).forEach(result::add);
        staffUserRepository.findAll().stream().map(this::toResponse).forEach(result::add);
        adminUserRepository.findAll().stream().map(this::toResponse).forEach(result::add);
        result.sort(Comparator.comparing(AdminUserResponse::getCreatedAt).reversed());
        return result;
    }

    @Transactional
    public AdminUserResponse updateUserStatus(String userType, Long id, UpdateUserStatusRequest request) {
        boolean isBan = "BAN".equals(request.getAction());
        return switch (userType.toUpperCase()) {
            case "STUDENT" -> {
                StudentUser student = studentUserRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên id=" + id));
                if (isBan) {
                    student.setStatus(StudentUser.StudentStatus.SUSPENDED);
                    student.setSuspendReason(request.getReason());
                    student.setCurrentStreak(0);
                } else {
                    student.setStatus(StudentUser.StudentStatus.ACTIVE);
                    student.setSuspendReason(null);
                }
                yield toResponse(studentUserRepository.save(student));
            }
            case "STAFF" -> {
                StaffUser staff = staffUserRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + id));
                if (isBan) {
                    staff.setStatus(StaffUser.StaffStatus.SUSPENDED);
                    staff.setSuspendReason(request.getReason());
                } else {
                    staff.setStatus(StaffUser.StaffStatus.ACTIVE);
                    staff.setSuspendReason(null);
                }
                yield toResponse(staffUserRepository.save(staff));
            }
            case "ADMIN" -> {
                AdminUser admin = adminUserRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy admin id=" + id));
                if (isBan) {
                    admin.setStatus(AdminUser.AdminStatus.SUSPENDED);
                    admin.setSuspendReason(request.getReason());
                } else {
                    admin.setStatus(AdminUser.AdminStatus.ACTIVE);
                    admin.setSuspendReason(null);
                }
                yield toResponse(adminUserRepository.save(admin));
            }
            default -> throw new BadRequestException("userType không hợp lệ: " + userType);
        };
    }

    @Transactional
    public AdminUserResponse updateUserRole(String userType, Long id, UpdateUserRoleRequest request) {
        String currentType = userType.toUpperCase();
        String newRole = request.getNewRole().toUpperCase();

        if ("ADMIN".equals(currentType)) {
            throw new ForbiddenException("Không thể thay đổi vai trò của Admin");
        }
        if (currentType.equals(newRole)) {
            return getUser(currentType, id);
        }
        if ("STUDENT".equals(currentType) && "STAFF".equals(newRole)) {
            return promoteStudentToStaff(id);
        }
        if ("STAFF".equals(currentType) && "STUDENT".equals(newRole)) {
            return demoteStaffToStudent(id);
        }
        throw new BadRequestException("Thay đổi vai trò không hợp lệ");
    }

    private AdminUserResponse promoteStudentToStaff(Long studentId) {
        StudentUser student = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên id=" + studentId));

        if (staffUserRepository.existsByEmail(student.getEmail())) {
            throw new DuplicateResourceException("Email đã tồn tại trong bảng nhân viên");
        }

        StaffUser newStaff = StaffUser.builder()
                .email(student.getEmail())
                .fullName(student.getFullName())
                .passwordHash(student.getPasswordHash())
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
        StaffUser saved = staffUserRepository.save(newStaff);

        student.setStatus(StudentUser.StudentStatus.DELETED);
        studentUserRepository.save(student);

        log.info("[AdminUserService] Promoted studentId={} -> STAFF staffId={}", studentId, saved.getId());
        return toResponse(saved);
    }

    private AdminUserResponse demoteStaffToStudent(Long staffId) {
        StaffUser staff = staffUserRepository
                .findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + staffId));

        if (studentUserRepository.existsByEmail(staff.getEmail())) {
            throw new DuplicateResourceException("Email đã tồn tại trong bảng học viên");
        }

        StudentUser newStudent = StudentUser.builder()
                .email(staff.getEmail())
                .fullName(staff.getFullName())
                .passwordHash(staff.getPasswordHash())
                .status(StudentUser.StudentStatus.ACTIVE)
                .build();
        StudentUser saved = studentUserRepository.save(newStudent);

        staff.setStatus(StaffUser.StaffStatus.DELETED);
        staffUserRepository.save(staff);

        log.info("[AdminUserService] Demoted staffId={} -> STUDENT studentId={}", staffId, saved.getId());
        return toResponse(saved);
    }

    private AdminUserResponse getUser(String userType, Long id) {
        return switch (userType) {
            case "STUDENT" -> studentUserRepository
                    .findById(id)
                    .map(this::toResponse)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
            case "STAFF" -> staffUserRepository
                    .findById(id)
                    .map(this::toResponse)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
            case "ADMIN" -> adminUserRepository
                    .findById(id)
                    .map(this::toResponse)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
            default -> throw new BadRequestException("userType không hợp lệ");
        };
    }

    private AdminUserResponse toResponse(StudentUser s) {
        String status = s.getStatus() == StudentUser.StudentStatus.SUSPENDED
                ? "BANNED"
                : s.getStatus().name();
        return AdminUserResponse.builder()
                .id(s.getId())
                .userType("STUDENT")
                .role("STUDENT")
                .fullName(s.getFullName())
                .email(s.getEmail())
                .jlptLevel(
                        s.getCurrentJlptLevel() != null
                                ? s.getCurrentJlptLevel().name()
                                : null)
                .status(status)
                .streak(s.getCurrentStreak())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private AdminUserResponse toResponse(StaffUser s) {
        String status = s.getStatus() == StaffUser.StaffStatus.SUSPENDED
                ? "BANNED"
                : s.getStatus().name();
        return AdminUserResponse.builder()
                .id(s.getId())
                .userType("STAFF")
                .role("STAFF")
                .fullName(s.getFullName())
                .email(s.getEmail())
                .status(status)
                .createdAt(s.getCreatedAt())
                .build();
    }

    private AdminUserResponse toResponse(AdminUser a) {
        String status = a.getStatus() == AdminUser.AdminStatus.SUSPENDED
                ? "BANNED"
                : a.getStatus().name();
        return AdminUserResponse.builder()
                .id(a.getId())
                .userType("ADMIN")
                .role("ADMIN")
                .fullName(a.getFullName())
                .email(a.getEmail())
                .status(status)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
