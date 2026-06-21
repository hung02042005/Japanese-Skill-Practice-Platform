/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
        name = "vocabulary_topics",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "UQ_vocab_topics_level_slug",
                    columnNames = {"jlpt_level", "slug"}),
            @UniqueConstraint(
                    name = "UQ_vocab_topics_level_title_vi",
                    columnNames = {"jlpt_level", "title_vi"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabularyTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false, length = 5)
    private StudentUser.JlptLevel jlptLevel;

    @Column(nullable = false, length = 80)
    private String slug;

    @Column(name = "title_ja", nullable = false, length = 100)
    private String titleJa;

    @Column(name = "title_vi", nullable = false, length = 100)
    private String titleVi;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Convert(converter = ContentStatusConverter.class)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Kanji.ContentStatus status = Kanji.ContentStatus.PUBLISHED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private StaffUser createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
