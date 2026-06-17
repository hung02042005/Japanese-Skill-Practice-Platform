/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.repository;

import com.jlpt.feature.assessment.QuestionAssignment;
import com.jlpt.feature.assessment.QuestionAssignment.ParentType;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** UC-34 — Kiểm tra câu hỏi có đang nằm trong đề thi {@code published} hay không (FR-34-14). */
@Repository
public interface QuestionReferenceRepository extends JpaRepository<QuestionAssignment, Long> {

    /**
     * FR-34-14 — Câu hỏi được gán (parent_type='assessment') vào đề thi {@code published}.
     * Nếu trả về ≥1 dòng ⇒ chặn unpublish/archive/delete câu hỏi với 409 RESOURCE_IN_USE.
     *
     * <p>Dùng EXISTS subquery thay vì cross join vì {@code QuestionAssignment.parentId} là Long
     * thuần (không phải @ManyToOne Assessment), tránh tích Descartes n_assignments × n_assessments.
     */
    @Query("SELECT new com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse('assessment', a.id, a.title) "
            + "FROM com.jlpt.feature.assessment.Assessment a "
            + "WHERE a.status = :published "
            + "AND EXISTS (SELECT 1 FROM QuestionAssignment qa "
            + "  WHERE qa.parentType = :parentType AND qa.question.id = :questionId AND qa.parentId = a.id)")
    List<ReferenceItemResponse> findPublishedAssessmentsByQuestion(
            @Param("questionId") Long questionId,
            @Param("parentType") ParentType parentType,
            @Param("published") ContentStatus published);
}
