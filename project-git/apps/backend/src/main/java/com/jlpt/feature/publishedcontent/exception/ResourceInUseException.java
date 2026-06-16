/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.exception;

import com.jlpt.feature.publishedcontent.dto.ReferenceItemResponse;
import java.util.List;
import lombok.Getter;

/**
 * UC-34 — Nội dung đang được tham chiếu bởi tài nguyên active, không thể ẩn
 * (HTTP 409, RESOURCE_IN_USE — FR-34-14/15/16).
 *
 * <p>Mang theo danh sách {@code references} để {@link PublishedContentExceptionHandler}
 * đưa vào body response.
 */
@Getter
public class ResourceInUseException extends RuntimeException {

    private final transient List<ReferenceItemResponse> references;

    public ResourceInUseException(List<ReferenceItemResponse> references) {
        super("Nội dung đang được tham chiếu bởi tài nguyên đang hoạt động, không thể ẩn");
        this.references = references;
    }
}
