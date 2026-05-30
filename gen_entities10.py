import os
DIR = "apps/backend/src/main/java/com/jlpt/entity"
FILES = {}

FILES["SystemSetting.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Integer id;

    @Column(name = "setting_group", nullable = false, length = 50)
    private String settingGroup;

    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "NVARCHAR(MAX)")
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    @Builder.Default
    private ValueType valueType = ValueType.STRING;

    @Column(name = "is_editable", nullable = false)
    @Builder.Default
    private Boolean isEditable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private AdminUser updatedBy;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum ValueType { STRING("string"), INTEGER("integer"), BOOLEAN("boolean"), TIME("time");
        private final String v; ValueType(String v) { this.v = v; } public String getValue() { return v; } }
}
"""

FILES["AdminAuditLog.java"] = """package com.jlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_actor_id")
    private AdminUser adminActor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_actor_id")
    private StaffUser staffActor;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "target_table", length = 100)
    private String targetTable;

    @Column(name = "target_id")
    private Long targetId;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
"""

for fname, content in FILES.items():
    with open(os.path.join(DIR, fname), "w", encoding="utf-8") as f:
        f.write(content.lstrip())
    print(f"OK {fname}")
