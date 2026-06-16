/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kana_characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanaCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kana_id")
    private Integer id;

    @Column(name = "character_value", nullable = false, length = 5)
    private String characterValue;

    @Column(nullable = false, length = 10)
    private String romaji;

    @Enumerated(EnumType.STRING)
    @Column(name = "kana_type", nullable = false, length = 15)
    private KanaType kanaType;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "stroke_order_url", length = 500)
    private String strokeOrderUrl;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    public enum KanaType {
        HIRAGANA("hiragana"),
        KATAKANA("katakana");
        private final String v;

        KanaType(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }
    }
}
