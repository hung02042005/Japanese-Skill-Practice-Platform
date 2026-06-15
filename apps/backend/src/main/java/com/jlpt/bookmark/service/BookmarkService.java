/* (c) JLPT E-Learning Platform */
package com.jlpt.bookmark.service;

import com.jlpt.bookmark.dto.BookmarkRequest;
import com.jlpt.bookmark.dto.BookmarkResponse;
import com.jlpt.entity.GrammarPoint;
import com.jlpt.entity.Kanji;
import com.jlpt.entity.StudentContentProgress;
import com.jlpt.entity.StudentUser;
import com.jlpt.entity.Vocabulary;
import com.jlpt.exception.BadRequestException;
import com.jlpt.exception.ResourceNotFoundException;
import com.jlpt.repository.GrammarPointRepository;
import com.jlpt.repository.KanjiRepository;
import com.jlpt.repository.StudentContentProgressRepository;
import com.jlpt.repository.StudentUserRepository;
import com.jlpt.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final StudentContentProgressRepository progressRepository;
    private final VocabularyRepository vocabularyRepository;
    private final KanjiRepository kanjiRepository;
    private final GrammarPointRepository grammarPointRepository;
    private final StudentUserRepository studentUserRepository;

    public BookmarkResponse addBookmark(Long studentId, BookmarkRequest request) {
        StudentContentProgress.ContentType contentType = parseContentType(request.contentType());

        StudentContentProgress progress = progressRepository
                .findByStudentAndContent(studentId, contentType, request.contentId())
                .orElseGet(() -> {
                    StudentUser student = studentUserRepository.getReferenceById(studentId);
                    return StudentContentProgress.builder()
                            .student(student)
                            .contentType(contentType)
                            .contentId(request.contentId())
                            .build();
                });

        progress.setIsBookmarked(true);
        progress.setBookmarkNote(request.note());
        progress.setBookmarkedAt(LocalDateTime.now());

        StudentContentProgress saved = progressRepository.save(progress);
        return toResponse(saved, loadContentMaps(List.of(saved)));
    }

    public void removeBookmark(Long studentId, String contentTypeStr, Long contentId) {
        StudentContentProgress.ContentType contentType = parseContentType(contentTypeStr);
        StudentContentProgress progress = progressRepository
                .findByStudentAndContent(studentId, contentType, contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark không tồn tại"));

        progress.setIsBookmarked(false);
        progress.setBookmarkNote(null);
        progress.setBookmarkedAt(null);
        progressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public Page<BookmarkResponse> listBookmarks(Long studentId, String type, Pageable pageable) {
        Page<StudentContentProgress> page = (type != null && !type.isBlank())
                ? progressRepository.findBookmarksByType(studentId, Boolean.TRUE, parseContentType(type), pageable)
                : progressRepository.findBookmarks(studentId, Boolean.TRUE, pageable);
        // Batch-fetch nội dung theo từng loại để tránh N+1 (mỗi bookmark trước đây
        // gọi 2 query riêng để lấy displayText + jlptLevel).
        BookmarkMaps maps = loadContentMaps(page.getContent());
        return page.map(p -> toResponse(p, maps));
    }

    private StudentContentProgress.ContentType parseContentType(String type) {
        try {
            return StudentContentProgress.ContentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Loại nội dung không hợp lệ: " + type);
        }
    }

    private BookmarkResponse toResponse(StudentContentProgress p, BookmarkMaps maps) {
        String displayText = "Unknown";
        String jlptLevel = null;
        Long id = p.getContentId();
        switch (p.getContentType()) {
            case VOCABULARY -> {
                Vocabulary v = maps.vocab().get(id);
                if (v != null) {
                    displayText = v.getWord();
                    jlptLevel = v.getJlptLevel() != null ? v.getJlptLevel().name() : null;
                }
            }
            case KANJI -> {
                Kanji k = maps.kanji().get(id);
                if (k != null) {
                    displayText = k.getCharacterValue();
                    jlptLevel = k.getJlptLevel() != null ? k.getJlptLevel().name() : null;
                }
            }
            case GRAMMAR -> {
                GrammarPoint g = maps.grammar().get(id);
                if (g != null) {
                    displayText = g.getStructure();
                    jlptLevel = g.getJlptLevel() != null ? g.getJlptLevel().name() : null;
                }
            }
            default -> {}
        }
        return new BookmarkResponse(
                p.getId(),
                p.getContentType().name(),
                p.getContentId(),
                displayText,
                p.getBookmarkNote(),
                p.getBookmarkedAt(),
                jlptLevel);
    }

    /** Gom contentId theo loại rồi batch-fetch một lần mỗi loại (tránh N+1). */
    private BookmarkMaps loadContentMaps(List<StudentContentProgress> items) {
        List<Vocabulary> vocab =
                vocabularyRepository.findAllById(idsOfType(items, StudentContentProgress.ContentType.VOCABULARY));
        List<Kanji> kanji = kanjiRepository.findAllById(idsOfType(items, StudentContentProgress.ContentType.KANJI));
        List<GrammarPoint> grammar =
                grammarPointRepository.findAllById(idsOfType(items, StudentContentProgress.ContentType.GRAMMAR));
        return new BookmarkMaps(
                toMap(vocab, Vocabulary::getId), toMap(kanji, Kanji::getId), toMap(grammar, GrammarPoint::getId));
    }

    private static Set<Long> idsOfType(List<StudentContentProgress> items, StudentContentProgress.ContentType type) {
        return items.stream()
                .filter(p -> p.getContentType() == type)
                .map(StudentContentProgress::getContentId)
                .collect(Collectors.toSet());
    }

    private static <T> Map<Long, T> toMap(List<T> entities, Function<T, Long> idFn) {
        return entities.stream().collect(Collectors.toMap(idFn, Function.identity()));
    }

    private record BookmarkMaps(Map<Long, Vocabulary> vocab, Map<Long, Kanji> kanji, Map<Long, GrammarPoint> grammar) {}
}
