/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import com.jlpt.dto.response.DashboardResponse;
import com.jlpt.entity.StudentUser;
import com.jlpt.exception.ResourceNotFoundException;
import com.jlpt.repository.StudentContentProgressRepository;
import com.jlpt.repository.StudentUserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final StudentUserRepository studentUserRepository;
    private final StudentContentProgressRepository progressRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long studentId) {
        StudentUser student = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // Streak
        int streak = student.getCurrentStreak() != null ? student.getCurrentStreak() : 0;

        // weekDays — 7 ngày gần nhất (hôm nay ở index 6), true nếu student học ngày đó
        List<Boolean> weekDays = buildWeekDays(student.getLastActivityDate(), streak);

        // wordCount — số vocab đã hoàn thành
        long wordCount = progressRepository.countCompletedVocab(studentId);

        // daysThisMonth — số ngày học trong tháng hiện tại
        LocalDate now = LocalDate.now();
        long daysThisMonth =
                progressRepository.countDistinctStudyDaysInMonth(studentId, now.getYear(), now.getMonthValue());

        log.debug(
                "[DASHBOARD] studentId={} streak={} wordCount={} daysThisMonth={}",
                studentId,
                streak,
                wordCount,
                daysThisMonth);

        return new DashboardResponse(streak, weekDays, wordCount, daysThisMonth);
    }

    /**
     * Tái tạo 7 ngày gần nhất dựa trên streak hiện tại.
     * Nếu streak >= N ngày liên tiếp → mark N ngày cuối là true.
     */
    private List<Boolean> buildWeekDays(LocalDate lastActive, int streak) {
        LocalDate today = LocalDate.now();
        List<Boolean> days = new ArrayList<>(7);

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            if (lastActive != null && streak > 0) {
                // Ngày thuộc streak hiện tại: [lastActive - streak + 1 .. lastActive]
                LocalDate streakStart = lastActive.minusDays(streak - 1);
                days.add(!day.isBefore(streakStart) && !day.isAfter(lastActive));
            } else {
                days.add(false);
            }
        }
        return days;
    }
}
