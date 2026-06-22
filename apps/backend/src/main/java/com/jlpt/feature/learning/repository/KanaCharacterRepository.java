package com.jlpt.feature.learning.repository;

import com.jlpt.feature.learning.KanaCharacter;
import com.jlpt.feature.learning.KanaCharacter.KanaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanaCharacterRepository extends JpaRepository<KanaCharacter, Integer> {
    List<KanaCharacter> findByKanaTypeOrderByDisplayOrderAsc(KanaType kanaType);
}
