/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.progress;

import com.jlpt.feature.student.progress.dto.LearningProgressRequest;
import com.jlpt.feature.student.progress.dto.LearningProgressResponse;

public interface StudentLearningProgressService {
    LearningProgressResponse markProgress(LearningProgressRequest request, Long studentId);

    void resetProgress(String contentType, Long studentId);
}
