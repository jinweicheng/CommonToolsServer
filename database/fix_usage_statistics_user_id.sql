-- ============================================
-- 修复 usage_statistics 表的 user_id 字段类型
-- 将 user_id 从整数类型改为 VARCHAR(36) 以支持UUID
-- ============================================

USE `commontools`;

-- 检查当前表结构
-- SHOW CREATE TABLE `usage_statistics`;

-- 如果表存在但 user_id 是整数类型，需要修改
-- 注意：如果表中已有数据，需要先备份或清空数据

-- 方案1: 如果表中没有重要数据，直接删除表让Hibernate重新创建
-- DROP TABLE IF EXISTS `usage_statistics`;

-- 方案2: 如果表中有数据需要保留，先备份数据，然后修改表结构
-- 备份数据（如果需要）
-- CREATE TABLE `usage_statistics_backup` AS SELECT * FROM `usage_statistics`;

-- 删除旧表
DROP TABLE IF EXISTS `usage_statistics`;

-- 重新创建表（使用正确的结构）
CREATE TABLE `usage_statistics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID（UUID）',
    `module` VARCHAR(50) NOT NULL COMMENT '模块名称：watermark, conversion, signature, compression, heic-to-jpg, password-manager, pages等',
    `action` VARCHAR(50) NOT NULL COMMENT '操作名称：upload, download, preview, view, create, update, delete等',
    `endpoint` VARCHAR(200) NOT NULL COMMENT 'API端点路径',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理（User-Agent）',
    `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型：PC, MOBILE, TABLET, UNKNOWN',
    `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器：Chrome, Firefox, Safari等',
    `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统：Windows, macOS, Linux, iOS, Android等',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `stat_hour` TINYINT DEFAULT NULL COMMENT '统计小时（0-23）',
    `count` INT NOT NULL DEFAULT 1 COMMENT '访问次数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_module_action_date` (`user_id`, `module`, `action`, `stat_date`, `stat_hour`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_action` (`action`),
    KEY `idx_stat_date` (`stat_date`),
    KEY `idx_stat_hour` (`stat_hour`),
    KEY `idx_module_action` (`module`, `action`),
    KEY `idx_ip_address` (`ip_address`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用统计表';

-- 如果需要恢复备份的数据，可以使用以下SQL（需要根据实际情况调整）
-- INSERT INTO `usage_statistics` SELECT * FROM `usage_statistics_backup`;
