/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cập nhật metadata sổ tay (PATCH §6). Trường null = không đổi. */
public record DeckUpdateRequest(
        @Size(max = 100) String name,
        @Size(max = 500) String description,
        @Pattern(regexp = "N5|N4|N3|N2|N1") String jlptLevel,
        @Size(max = 100) String topic,
        @Size(max = 20) String color) {}
