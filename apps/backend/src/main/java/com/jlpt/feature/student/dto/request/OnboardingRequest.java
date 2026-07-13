/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

/**
 * Dữ liệu onboarding học viên. Hiện chỉ lưu mục tiêu JLPT ({@code jlptGoal} →
 * target_jlpt_level); {@code dailyMinutes}/{@code focusSkills} nhận nhưng chưa
 * lưu
 * (chưa có cột — xem plan, tránh migration).
 */
@Data
public class OnboardingRequest {

    @NotBlank(message = "Mục tiêu JLPT không được để trống")
    private String jlptGoal;

    private Integer dailyMinutes;

    private List<String> focusSkills;
}
