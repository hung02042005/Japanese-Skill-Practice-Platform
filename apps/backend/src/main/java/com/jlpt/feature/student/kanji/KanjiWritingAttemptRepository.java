/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KanjiWritingAttemptRepository extends JpaRepository<KanjiWritingAttempt, Long> {}
