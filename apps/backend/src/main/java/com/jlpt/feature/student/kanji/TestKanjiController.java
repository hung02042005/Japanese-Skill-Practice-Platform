package com.jlpt.feature.student.kanji;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import org.springframework.data.domain.PageRequest;

@RestController
@RequiredArgsConstructor
public class TestKanjiController {
    private final StudentKanjiRepository repo;

    @GetMapping("/api/test-kanji")
    public Object testKanji() {
        try {
            return repo.findByLevelAndStatus(JlptLevel.N5, ContentStatus.PUBLISHED, PageRequest.of(0, 20));
        } catch (Exception e) {
            return e.toString() + " | " + e.getMessage();
        }
    }
}
