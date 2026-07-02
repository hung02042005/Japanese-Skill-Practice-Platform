/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Integer> {

    List<SystemSetting> findBySettingGroup(String settingGroup);

    Optional<SystemSetting> findBySettingGroupAndSettingKey(String settingGroup, String settingKey);

    boolean existsBySettingGroupAndSettingKey(String settingGroup, String settingKey);
}
