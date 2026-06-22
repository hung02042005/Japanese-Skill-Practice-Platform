package com.jlpt.feature.student.kanji;

import com.jlpt.feature.student.StudentUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "kanji_writing_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiWritingAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentUser student;

    @Column(name = "kanji_id", nullable = false)
    private Long kanjiId;

    @Column(name = "character_value", nullable = false, length = 5)
    private String characterValue;

    @Column(name = "total_strokes", nullable = false)
    private int totalStrokes;

    @Column(name = "avg_dtw_score")
    private Double avgDtwScore;

    /** "perfect" | "good" | "ok" | "bad" */
    @Column(name = "final_quality", length = 20)
    private String finalQuality;

    /** JSON array của kết quả từng nét [{idx, dtw, quality, dir}, ...] */
    @Column(name = "stroke_details", columnDefinition = "NVARCHAR(MAX)")
    private String strokeDetails;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Long createdBy;
}
