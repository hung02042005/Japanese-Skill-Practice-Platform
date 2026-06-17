/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.contentreview.ContentSnapshot;
import com.jlpt.feature.contentreview.ContentType;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UC-33 — Chiến lược (Strategy) xử lý kiểm duyệt cho từng loại nội dung.
 *
 * <p>Mỗi bảng học liệu (lessons, grammar_points, vocabulary, kanji, questions, assessments)
 * có một implementation riêng. {@code ReviewableContentResolver} ánh xạ
 * {@link ContentType} → handler để tránh if/else rải rác (tránh God Class — PLAN §3).
 */
public interface ReviewableContentHandler {

    /** Loại nội dung mà handler phụ trách. */
    ContentType type();

    /** Tên bảng DB tương ứng (ghi vào {@code admin_audit_logs.target_table}). */
    String tableName();

    /** Danh sách item đang {@code pending_review}, lọc theo level (null = mọi level), sort updated_at ASC. */
    List<ContentSnapshot> findPending(JlptLevel level);

    /** Chi tiết một item chưa bị {@code deleted}; rỗng nếu không tồn tại/đã xóa (FR-33-07). */
    Optional<ContentSnapshot> findActiveById(Long contentId);

    /**
     * Guarded approve: {@code UPDATE ... SET status='published', approved_by=:mgr, published_at=:now
     * WHERE id=:id AND status='pending_review'}. Trả số dòng ảnh hưởng (0 = đã bị xử lý — FR-33-19).
     */
    int approve(Long contentId, StaffUser manager, LocalDateTime now);

    /**
     * Guarded transition từ {@code pending_review} sang {@code rejected}/{@code draft}.
     * Trả số dòng ảnh hưởng (0 = đã bị xử lý — FR-33-19).
     */
    int transitionFromPending(Long contentId, String targetStatus, LocalDateTime now);
}
