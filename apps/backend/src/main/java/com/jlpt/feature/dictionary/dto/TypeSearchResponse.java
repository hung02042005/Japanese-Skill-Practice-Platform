/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.dictionary.dto;

import java.util.List;

/**
 * Kết quả tra cứu phân trang cho MỘT loại nội dung (nút "Xem thêm" — 1B).
 * {@code items} là danh sách item record tương ứng loại ({@link SearchResponse.VocabItem}…),
 * {@code hasMore} = còn trang sau không (suy từ việc trang trả về đủ {@code size} phần tử).
 */
public record TypeSearchResponse(String type, List<Object> items, boolean hasMore) {}
