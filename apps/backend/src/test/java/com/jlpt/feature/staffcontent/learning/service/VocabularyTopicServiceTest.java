/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit test cho hàm slugify (bỏ dấu tiếng Việt) — thuần logic, không cần Spring context. */
class VocabularyTopicServiceTest {

    @Test
    void slugify_removesVietnameseDiacriticsAndSpaces() {
        assertEquals("gia-dinh", VocabularyTopicService.slugify("Gia đình"));
        assertEquals("am-thuc", VocabularyTopicService.slugify("Ẩm thực"));
        assertEquals("giao-thong", VocabularyTopicService.slugify("Giao thông"));
    }

    @Test
    void slugify_trimsAndCollapsesSeparators() {
        assertEquals("cong-nghe", VocabularyTopicService.slugify("  Công   nghệ  "));
        assertEquals("a-b", VocabularyTopicService.slugify("a / b"));
    }

    @Test
    void slugify_fallsBackWhenEmpty() {
        assertEquals("topic", VocabularyTopicService.slugify("！！！"));
    }
}
