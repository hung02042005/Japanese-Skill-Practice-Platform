/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kana;

import com.jlpt.feature.learning.KanaCharacter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** UC-08 — Read access to kana_characters. No status filter: all kana are always public (BR-08-07). */
@Repository
public interface KanaRepository extends JpaRepository<KanaCharacter, Integer> {

    List<KanaCharacter> findByKanaTypeOrderByDisplayOrder(KanaCharacter.KanaType kanaType);
}
