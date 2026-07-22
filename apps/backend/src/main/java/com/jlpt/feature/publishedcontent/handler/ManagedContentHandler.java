/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.handler;

import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.publishedcontent.model.ManagedContentSnapshot;
import com.jlpt.feature.publishedcontent.model.TargetStatus;
import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UC-34 — Chiến lược (Strategy) quản lý vòng đời sau xuất bản cho từng loại nội dung.
 *
 * <p>Mỗi bảng học liệu có một implementation; {@code ManagedContentResolver} ánh xạ
 * {@link ContentType} → handler để tránh if/else rải rác (tránh God Class).
 */
public interface ManagedContentHandler {

    /** Loại nội dung handler phụ trách. */
    ContentType type();

    /** Tên bảng DB (ghi {@code admin_audit_logs.target_table}). */
    String tableName();

    /** Item đang {@code published}, lọc theo level (null = mọi level), sort {@code published_at} DESC (FR-34-03/04). */
    List<ManagedContentSnapshot> findPublished(JlptLevel level);

    /** Snapshot một item theo id bất kể trạng thái; rỗng nếu không tồn tại (FR-34-08). */
    Optional<ManagedContentSnapshot> findById(Long contentId);

    /** Danh sách tài nguyên active đang tham chiếu item (rỗng nếu không bị chặn) (FR-34-14/15/16). */
    List<ReferenceItemResponse> findBlockingReferences(Long contentId);

    /**
     * Guarded soft-delete: {@code UPDATE ... SET status=<target> WHERE id=:id AND status='published'}.
     * Trả số dòng ảnh hưởng (0 = item không còn {@code published} — FR-34-10/22).
     */
    int changeStatus(Long contentId, TargetStatus target, LocalDateTime now);

    /**
     * Guarded restore: {@code UPDATE ... SET status='published' WHERE id=:id AND status='archived'}.
     * Trả số dòng ảnh hưởng (0 = item không còn {@code archived} — FR-34-18).
     */
    int restore(Long contentId, LocalDateTime now);
}
