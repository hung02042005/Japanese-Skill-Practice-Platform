/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.grammar.dto.CreateGrammarRequest;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarDetailResponse;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarSubmitReviewResponse;
import com.jlpt.feature.staffcontent.grammar.exception.GrammarBusinessException;
import com.jlpt.feature.staffcontent.grammar.repository.StaffGrammarRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests cho StaffGrammarServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class StaffGrammarServiceImplTest {

    @Mock
    private StaffGrammarRepository grammarRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private StaffGrammarServiceImpl staffGrammarService;

    private StaffUser staff;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder()
                .id(1L)
                .email("staff@test.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
    }

    @Test
    void createGrammar_invalidJlptLevel_throwsException() {
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));

        CreateGrammarRequest req = new CreateGrammarRequest();
        req.setJlptLevel("INVALID");

        assertThrows(GrammarBusinessException.class, () -> staffGrammarService.createGrammar(req, "staff@test.com"));
    }

    @Test
    void createGrammar_success_savesAsDraft() {
        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(grammarRepository.save(any())).thenAnswer(i -> {
            GrammarPoint g = i.getArgument(0);
            g.setId(10L);
            return g;
        });

        CreateGrammarRequest req = new CreateGrammarRequest();
        req.setTitle("V-てから");
        req.setStructure("V-てから");
        req.setMeaning("After doing V");
        req.setUsageExplanation("Indicates sequence of actions");
        req.setJlptLevel("N5");
        req.setExampleSentenceJp("ご飯を食べてから、歯を磨きます。");

        GrammarDetailResponse res = staffGrammarService.createGrammar(req, "staff@test.com");

        assertNotNull(res);
        assertEquals(10L, res.getGrammarId());
        assertEquals("draft", res.getStatus());
        verify(grammarRepository).save(any(GrammarPoint.class));
    }

    @Test
    void submitForReview_incompleteFields_throwsGrammarBusinessException() {
        GrammarPoint grammar = GrammarPoint.builder()
                .id(10L)
                .createdBy(staff)
                .status(ContentStatus.DRAFT)
                .structure("") // Blank structure
                .build();

        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(grammarRepository.findByIdAndStatusNot(10L, ContentStatus.DELETED)).thenReturn(Optional.of(grammar));

        assertThrows(GrammarBusinessException.class, () -> staffGrammarService.submitForReview(10L, "staff@test.com"));
    }

    @Test
    void submitForReview_success_updatesStatusToPendingReview() {
        GrammarPoint grammar = GrammarPoint.builder()
                .id(10L)
                .createdBy(staff)
                .status(ContentStatus.DRAFT)
                .structure("V-てから")
                .meaning("After doing V")
                .usageExplanation("Usage")
                .exampleSentenceJp("JP sentence")
                .jlptLevel(JlptLevel.N5)
                .build();

        when(staffUserRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(grammarRepository.findByIdAndStatusNot(10L, ContentStatus.DELETED)).thenReturn(Optional.of(grammar));

        GrammarSubmitReviewResponse res = staffGrammarService.submitForReview(10L, "staff@test.com");

        assertNotNull(res);
        assertEquals("pending_review", res.getStatus());
        assertEquals(ContentStatus.PENDING_REVIEW, grammar.getStatus());
        verify(grammarRepository).save(grammar);
    }
}
