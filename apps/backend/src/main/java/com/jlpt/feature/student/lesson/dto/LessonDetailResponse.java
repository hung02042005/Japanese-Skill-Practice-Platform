/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.lesson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Chi tiết một bài học cho học viên (UC chi tiết bài học). vocabulary/grammarPoints hiện trả
 * mảng rỗng vì model Lesson chưa liên kết trực tiếp tới Vocabulary/Grammar (xem plan).
 */
@Data
@Builder
public class LessonDetailResponse {

    private Long id;
    private String title;
    private String jlptLevel;
    private String lessonType;
    private int estimatedMinutes;

    @JsonProperty("isLocked")
    private boolean locked;

    private String progressStatus;
    private int progressPercent;
    private String contentHtml;
    private String audioUrl;
    private String imageUrl;
    private Long prevLessonId;
    private Long nextLessonId;
    private List<Object> vocabulary;
    private List<Object> grammarPoints;
}
