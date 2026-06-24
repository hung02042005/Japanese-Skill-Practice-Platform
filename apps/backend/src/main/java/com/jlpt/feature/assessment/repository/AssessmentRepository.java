/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.repository;

import com.jlpt.feature.assessment.entity.Assessment;
import com.jlpt.feature.content.entity.Kanji;
import com.jlpt.feature.student.entity.StudentUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Read-only repository for Assessment entity — used by AnalyticsService (feat-learning-analytics).
 * Write operations belong to feat-assessment owner.
 */
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    /** Lấy danh sách các JLPT levels có assessment để build completion report. */
    @Query("SELECT DISTINCT a.jlptLevel FROM Assessment a WHERE a.jlptLevel IS NOT NULL AND a.status = com.jlpt.feature.content.entity.Kanji.ContentStatus.PUBLISHED")
    List<StudentUser.JlptLevel> findDistinctPublishedJlptLevels();
}
