/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.learning.dto.VocabTopicResponse;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.learning.dto.CreateVocabTopicRequest;
import com.jlpt.feature.staffcontent.learning.exception.LearningContentException;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.DuplicateResourceException;
import com.jlpt.shared.exception.ForbiddenException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * FR-redo-topic — phần tạo & tra cứu catalog chủ đề (delete/restore đã có
 * {@link VocabularyTopicServiceDeleteRestoreTest}): Staff tạo được và chủ đề publish ngay, slug tự
 * sinh không dấu, chặn trùng slug/tiêu đề trong cùng cấp độ.
 */
@ExtendWith(MockitoExtension.class)
class VocabularyTopicServiceCreateTest {

    private static final String STAFF_EMAIL = "staff@sakuji.com";

    @Mock
    private VocabularyTopicRepository topicRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @InjectMocks
    private VocabularyTopicService service;

    private StaffUser staff;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder()
                .id(1L)
                .email(STAFF_EMAIL)
                .staffRole(StaffUser.StaffRole.STAFF)
                .status(StaffUser.StaffStatus.ACTIVE)
                .build();
    }

    private CreateVocabTopicRequest createRequest() {
        CreateVocabTopicRequest req = new CreateVocabTopicRequest();
        req.setJlptLevel("N5");
        req.setTitleVi("  Gia đình  ");
        req.setTitleJa("  家族  ");
        return req;
    }

    private void stubNoDuplicates() {
        when(topicRepository.existsByJlptLevelAndSlug(eq(JlptLevel.N5), any())).thenReturn(false);
        when(topicRepository.existsByJlptLevelAndTitleVi(JlptLevel.N5, "Gia đình"))
                .thenReturn(false);
    }

    private static JlptLevel eq(JlptLevel value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }

    // ── listByLevel ──────────────────────────────────────────────────────────

    @Test
    void listByLevel_returnsNonDeletedTopicsOfThatLevel() {
        VocabularyTopic topic = VocabularyTopic.builder()
                .id(5L)
                .jlptLevel(JlptLevel.N5)
                .slug("gia-dinh")
                .titleJa("家族")
                .titleVi("Gia đình")
                .displayOrder(1)
                .status(Kanji.ContentStatus.PUBLISHED)
                .build();
        when(topicRepository.findByJlptLevelAndStatusNotOrderByDisplayOrderAscIdAsc(
                        JlptLevel.N5, Kanji.ContentStatus.DELETED))
                .thenReturn(List.of(topic));

        List<VocabTopicResponse> result = service.listByLevel("N5");

        assertEquals(1, result.size());
    }

    @Test
    void listByLevel_invalidLevel_throwsInvalidJlptLevel() {
        assertThrows(LearningContentException.class, () -> service.listByLevel("N9"));
    }

    @Test
    void listByLevel_nullLevel_throwsInvalidJlptLevel() {
        assertThrows(LearningContentException.class, () -> service.listByLevel(null));
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_generatesSlugFromTitleTrimsAndPublishesImmediately() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        stubNoDuplicates();
        when(topicRepository.findMaxDisplayOrder(JlptLevel.N5)).thenReturn(3);
        when(topicRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.create(createRequest(), STAFF_EMAIL);

        ArgumentCaptor<VocabularyTopic> captor = ArgumentCaptor.forClass(VocabularyTopic.class);
        verify(topicRepository).save(captor.capture());
        VocabularyTopic saved = captor.getValue();
        assertEquals("gia-dinh", saved.getSlug());
        assertEquals("Gia đình", saved.getTitleVi());
        assertEquals("家族", saved.getTitleJa());
        assertEquals(4, saved.getDisplayOrder());
        assertEquals(Kanji.ContentStatus.PUBLISHED, saved.getStatus());
        assertSame(staff, saved.getCreatedBy());
    }

    @Test
    void create_explicitSlug_isTrimmedAndLowercased() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        stubNoDuplicates();
        when(topicRepository.findMaxDisplayOrder(JlptLevel.N5)).thenReturn(0);
        when(topicRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateVocabTopicRequest req = createRequest();
        req.setSlug("  MY-SLUG  ");

        service.create(req, STAFF_EMAIL);

        ArgumentCaptor<VocabularyTopic> captor = ArgumentCaptor.forClass(VocabularyTopic.class);
        verify(topicRepository).save(captor.capture());
        assertEquals("my-slug", captor.getValue().getSlug());
        assertEquals(1, captor.getValue().getDisplayOrder());
    }

    @Test
    void create_blankSlug_fallsBackToSlugifiedTitle() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        stubNoDuplicates();
        when(topicRepository.findMaxDisplayOrder(JlptLevel.N5)).thenReturn(0);
        when(topicRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateVocabTopicRequest req = createRequest();
        req.setSlug("   ");

        service.create(req, STAFF_EMAIL);

        ArgumentCaptor<VocabularyTopic> captor = ArgumentCaptor.forClass(VocabularyTopic.class);
        verify(topicRepository).save(captor.capture());
        assertEquals("gia-dinh", captor.getValue().getSlug());
    }

    @Test
    void create_duplicateSlugAtSameLevel_throwsDuplicate() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(topicRepository.existsByJlptLevelAndSlug(JlptLevel.N5, "gia-dinh")).thenReturn(true);
        CreateVocabTopicRequest req = createRequest();

        assertThrows(DuplicateResourceException.class, () -> service.create(req, STAFF_EMAIL));
        verify(topicRepository, never()).save(any());
    }

    @Test
    void create_duplicateTitleAtSameLevel_throwsDuplicate() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(topicRepository.existsByJlptLevelAndSlug(JlptLevel.N5, "gia-dinh")).thenReturn(false);
        when(topicRepository.existsByJlptLevelAndTitleVi(JlptLevel.N5, "Gia đình"))
                .thenReturn(true);
        CreateVocabTopicRequest req = createRequest();

        assertThrows(DuplicateResourceException.class, () -> service.create(req, STAFF_EMAIL));
        verify(topicRepository, never()).save(any());
    }

    @Test
    void create_unknownStaffAccount_throwsForbidden() {
        when(staffUserRepository.findByEmail("ghost@sakuji.com")).thenReturn(Optional.empty());
        CreateVocabTopicRequest req = createRequest();

        assertThrows(ForbiddenException.class, () -> service.create(req, "ghost@sakuji.com"));
    }

    @Test
    void create_invalidLevel_throwsInvalidJlptLevel() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        CreateVocabTopicRequest req = createRequest();
        req.setJlptLevel("N9");

        assertThrows(LearningContentException.class, () -> service.create(req, STAFF_EMAIL));
    }
}
