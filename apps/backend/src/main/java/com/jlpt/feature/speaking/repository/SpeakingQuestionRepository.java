/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.repository;

import com.jlpt.feature.speaking.entity.SpeakingQuestion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpeakingQuestionRepository extends JpaRepository<SpeakingQuestion, Long> {

    List<SpeakingQuestion> findByLesson_IdOrderByDisplayOrderAsc(Long lessonId);

    void deleteByLesson_Id(Long lessonId);
}
