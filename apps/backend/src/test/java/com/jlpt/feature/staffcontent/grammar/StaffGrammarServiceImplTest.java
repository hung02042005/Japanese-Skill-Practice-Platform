/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.grammar.dto.*;
import com.jlpt.feature.staffcontent.grammar.repository.StaffGrammarRepository;
import com.jlpt.feature.staffcontent.grammar.service.StaffGrammarServiceImpl;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StaffGrammarServiceImplTest {

    @Mock
    private StaffGrammarRepository grammarRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private StaffGrammarServiceImpl service;

    private StaffUser staff;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder()
                .id(100L)
                .email("staff@example.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
    }

    @Test
    void createGrammar_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(grammarRepository.save(any())).thenAnswer(i -> {
            GrammarPoint g = i.getArgument(0);
            g.setId(1L);
            return g;
        });

        CreateGrammarRequest req = new CreateGrammarRequest();
        req.setStructure("~てはいけない");
        req.setMeaning("Must not do");
        req.setUsageExplanation("Prohibition");
        req.setJlptLevel("N5");
        req.setExampleSentenceJp("食べてはいけない");

        GrammarDetailResponse res = service.createGrammar(req, "staff@example.com");
        assertEquals(1L, res.getGrammarId());
        assertEquals("~てはいけない", res.getStructure());
    }

    @Test
    void submitForReview_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        GrammarPoint grammar = GrammarPoint.builder()
                .id(1L)
                .structure("Structure")
                .meaning("Meaning")
                .usageExplanation("Usage")
                .exampleSentenceJp("Example")
                .jlptLevel(JlptLevel.N5)
                .status(ContentStatus.DRAFT)
                .createdBy(staff)
                .build();
        when(grammarRepository.findByIdAndStatusNot(1L, ContentStatus.DELETED)).thenReturn(Optional.of(grammar));

        GrammarSubmitReviewResponse res = service.submitForReview(1L, "staff@example.com");
        assertEquals("pending_review", res.getStatus());
    }
}
