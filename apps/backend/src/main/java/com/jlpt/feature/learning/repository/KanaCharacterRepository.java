/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.repository;

import com.jlpt.feature.learning.KanaCharacter;
import com.jlpt.feature.learning.KanaCharacter.KanaType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KanaCharacterRepository extends JpaRepository<KanaCharacter, Integer> {
    List<KanaCharacter> findByKanaTypeOrderByDisplayOrderAsc(KanaType kanaType);
}
