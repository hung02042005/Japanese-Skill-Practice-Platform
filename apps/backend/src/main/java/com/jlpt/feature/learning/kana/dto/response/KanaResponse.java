/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kana.dto.response;

import lombok.Builder;
import lombok.Data;

/** UC-08 — Item DTO for GET /api/kana. Không expose Entity (ADR-005). */
@Data
@Builder
public class KanaResponse {

    private Integer kanaId;
    /** Ký tự Kana hiển thị (あ, ア, ...). Field name "character" khớp với frontend. */
    private String character;
    /** Romaji (a, i, u, ...). */
    private String romaji;
    /** Nhóm hàng trong bảng chữ (1=あ行, 2=か行, ...). Dùng để nhóm ký tự trên UI. */
    private String rowGroup;
    private String audioUrl;
    /** Ảnh tĩnh thứ tự nét (BR-08-03). Field name "strokeGifUrl" khớp với frontend. */
    private String strokeGifUrl;
    private String kanaType;
    private Boolean isCompleted;
}
