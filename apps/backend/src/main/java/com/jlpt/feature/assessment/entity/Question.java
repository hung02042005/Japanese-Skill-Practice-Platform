/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.entity;

import com.jlpt.feature.staff.entity.StaffUser;
import com.jlpt.feature.student.entity.StudentUser;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "question_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false, length = 5)
    private StudentUser.JlptLevel jlptLevel;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "option_a", columnDefinition = "NVARCHAR(MAX)")
    private String optionA;

    @Column(name = "option_b", columnDefinition = "NVARCHAR(MAX)")
    private String optionB;

    @Column(name = "option_c", columnDefinition = "NVARCHAR(MAX)")
    private String optionC;

    @Column(name = "option_d", columnDefinition = "NVARCHAR(MAX)")
    private String optionD;

    @Column(name = "correct_option", columnDefinition = "CHAR(1)")
    private String correctOption;

    @Column(name = "correct_answer_text", columnDefinition = "NVARCHAR(MAX)")
    private String correctAnswerText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private StaffUser createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContentStatus status = ContentStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private StaffUser approvedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

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

    public enum QuestionType {
        MULTIPLE_CHOICE("multiple_choice"),
        FILL_BLANK("fill_blank"),
        TRUE_FALSE("true_false");
        private final String v;

        QuestionType(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum Skill {
        VOCABULARY("vocabulary"),
        GRAMMAR("grammar"),
        KANJI("kanji"),
        READING("reading"),
        LISTENING("listening"),
        MIXED("mixed");
        private final String v;

        Skill(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }

    public enum ContentStatus {
        DRAFT("draft"),
        PENDING_REVIEW("pending_review"),
        REJECTED("rejected"),
        PUBLISHED("published"),
        ARCHIVED("archived"),
        DELETED("deleted");
        private final String v;

        ContentStatus(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
