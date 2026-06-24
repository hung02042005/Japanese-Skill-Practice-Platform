/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.repository;

import com.jlpt.feature.admin.entity.SystemSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Integer> {

    Optional<SystemSetting> findBySettingGroupAndSettingKey(String settingGroup, String settingKey);

    List<SystemSetting> findAllBySettingGroup(String settingGroup);

    boolean existsBySettingGroupAndSettingKey(String settingGroup, String settingKey);

    @Query("SELECT DISTINCT s.settingGroup FROM SystemSetting s ORDER BY s.settingGroup ASC")
    List<String> findAllDistinctGroups();
}
