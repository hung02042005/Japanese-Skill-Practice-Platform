/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import com.jlpt.feature.student.StudentUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabularyTopicRepository extends JpaRepository<VocabularyTopic, Long> {

    // Chủ đề đã publish theo cấp độ, sắp xếp theo thứ tự hiển thị (lesson-path Vocab Home).
    @Query(
            """
            SELECT t FROM VocabularyTopic t
            WHERE t.jlptLevel = :jlptLevel
              AND t.status = :status
            ORDER BY t.displayOrder ASC, t.id ASC
            """)
    List<VocabularyTopic> findPublishedByLevel(
            @Param("jlptLevel") StudentUser.JlptLevel jlptLevel, @Param("status") Kanji.ContentStatus status);

    // Toàn bộ chủ đề của một cấp độ (mọi trạng thái) — màn quản lý topic của Staff.
    List<VocabularyTopic> findByJlptLevelOrderByDisplayOrderAscIdAsc(StudentUser.JlptLevel jlptLevel);

    boolean existsByJlptLevelAndSlug(StudentUser.JlptLevel jlptLevel, String slug);

    boolean existsByJlptLevelAndTitleVi(StudentUser.JlptLevel jlptLevel, String titleVi);

    // Thứ tự hiển thị kế tiếp khi staff tạo chủ đề mới (append cuối danh sách của cấp độ).
    @Query("SELECT COALESCE(MAX(t.displayOrder), 0) FROM VocabularyTopic t WHERE t.jlptLevel = :jlptLevel")
    int findMaxDisplayOrder(@Param("jlptLevel") StudentUser.JlptLevel jlptLevel);
}
