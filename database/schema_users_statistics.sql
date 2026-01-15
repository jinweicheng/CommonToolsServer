-- ============================================
-- CommonTools Server 核心表结构设计
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `commontools` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `commontools`;

-- ============================================
-- 1. 用户表 (users)
-- ============================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `email` VARCHAR(255) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT(1) DEFAULT NULL COMMENT '性别：0-未知，1-男，2-女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER, ADMIN',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '账户状态：ACTIVE-正常，LOCKED-锁定，DISABLED-禁用',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    `email_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邮箱是否验证：1-已验证，0-未验证',
    `phone_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '手机是否验证：1-已验证，0-未验证',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP地址',
    `login_count` INT NOT NULL DEFAULT 0 COMMENT '登录次数',
    `register_source` VARCHAR(50) DEFAULT NULL COMMENT '注册来源：WEB, APP, API等',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_email_verified` (`email_verified`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_last_login_time` (`last_login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 使用统计表 (usage_statistics)
-- ============================================
CREATE TABLE IF NOT EXISTS `usage_statistics` (
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
    KEY `idx_created_at` (`created_at`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用统计表';
