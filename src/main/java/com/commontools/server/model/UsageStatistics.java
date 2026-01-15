package com.commontools.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "usage_statistics", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module", "action", "stat_date", "endpoint", "ip_address"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UsageStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String userId; // 用户UUID

    @Column(nullable = false, length = 50)
    private String module; // 模块名称：files, passwords, feedback, users等

    @Column(nullable = false, length = 50)
    private String action; // 操作：upload, download, create, update, delete, list, view等

    @Column(nullable = false, length = 200)
    private String endpoint; // API端点路径

    @Column(length = 50)
    private String ipAddress; // IP地址

    @Column(length = 500)
    private String userAgent; // 用户代理（User-Agent）

    @Column(length = 20)
    private String deviceType; // 设备类型：PC, MOBILE, TABLET, UNKNOWN

    @Column(length = 50)
    private String browser; // 浏览器：Chrome, Firefox, Safari等

    @Column(length = 50)
    private String os; // 操作系统：Windows, macOS, Linux, iOS, Android等

    @Column(nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime statDate; // 统计日期时间（年月日时分秒）

    @Column(nullable = false)
    private Integer count = 1; // 访问次数

    @CreatedDate
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    // 用于统计的辅助字段
    @Transient
    private Long totalCount; // 总访问次数（用于聚合查询）
}
