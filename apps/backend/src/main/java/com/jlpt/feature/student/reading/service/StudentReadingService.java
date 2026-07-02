/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.reading.service;

import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.reading.dto.ReadingDetailResponse;
import com.jlpt.feature.student.reading.dto.ReadingLessonSummaryResponse;
import com.jlpt.feature.student.reading.dto.ReadingSubmitRequest;
import com.jlpt.feature.student.reading.dto.ReadingSubmitResponse;
import org.springframework.data.domain.Page;

public interface StudentReadingService {

    Page<ReadingLessonSummaryResponse> getLessonList(
            Lesson.LessonType type, StudentUser.JlptLevel level, Long studentId, int page, int size);

    ReadingDetailResponse getReadingDetail(Long lessonId, Long studentId);

    ReadingSubmitResponse submitReading(Long lessonId, Long studentId, ReadingSubmitRequest request);
}
