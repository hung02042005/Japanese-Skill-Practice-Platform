/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Data;

/**
 * Dữ liệu onboarding học viên. Hiện chỉ lưu mục tiêu JLPT ({@code jlptGoal} →
 * target_jlpt_level); {@code dailyMinutes}/{@code focusSkills} nhận nhưng chưa lưu
 * (chưa có cột — xem plan, tránh migration).
 */
@Data
public class OnboardingRequest {

    @NotBlank(message = "Mục tiêu JLPT không được để trống")
    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT không hợp lệ")
    private String jlptGoal;

    private Integer dailyMinutes;

    private List<String> focusSkills;
}
