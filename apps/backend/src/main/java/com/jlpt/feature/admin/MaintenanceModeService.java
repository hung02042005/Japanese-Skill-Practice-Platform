/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Đọc cờ bảo trì hệ thống (settings group=system, key=maintenance_mode). */
@Service
@RequiredArgsConstructor
public class MaintenanceModeService {

    private static final String GROUP = "system";
    private static final String KEY = "maintenance_mode";

    private final SystemSettingRepository systemSettingRepository;

    /** true nếu admin đang bật chế độ bảo trì. */
    @Transactional(readOnly = true)
    public boolean isEnabled() {
        return systemSettingRepository
                .findBySettingGroupAndSettingKey(GROUP, KEY)
                .map(s -> "true".equalsIgnoreCase(s.getSettingValue()))
                .orElse(false);
    }
}
