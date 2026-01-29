-- ============================================
-- 删除并重新创建所有表
-- 特别注意：
-- 1. 所有表的user_id都是VARCHAR(36)用于存储UUID，没有外键约束
-- 2. usage_statistics表的stat_date是DATETIME(6)类型，包含年月日时分秒，移除了stat_hour字段
-- 3. 所有表只保留PRIMARY KEY，移除了其他所有索引和约束
-- ============================================

USE `commontools`;

-- ============================================
-- 第一步：删除所有表（按依赖顺序，先删除有外键的表）
-- ============================================

-- 禁用外键检查（临时）
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有表
DROP TABLE IF EXISTS `contacts`;
DROP TABLE IF EXISTS `usage_statistics`;
DROP TABLE IF EXISTS `backup_files`;
DROP TABLE IF EXISTS `password_entries`;
DROP TABLE IF EXISTS `feedbacks`;
DROP TABLE IF EXISTS `users`;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二步：重新创建所有表
-- ============================================

-- ============================================
-- 1. 用户表 (users)
-- ============================================
CREATE TABLE `users` (
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
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 使用统计表 (usage_statistics)
-- 注意：user_id是VARCHAR(36)用于存储UUID，没有外键约束
-- stat_date是DATETIME类型，包含年月日时分秒
-- ============================================
CREATE TABLE `usage_statistics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID（UUID，不引用users表）',
    `module` VARCHAR(50) NOT NULL COMMENT '模块名称：watermark, conversion, signature, compression, heic-to-jpg, password-manager, pages等',
    `action` VARCHAR(50) NOT NULL COMMENT '操作名称：upload, download, preview, view, create, update, delete等',
    `endpoint` VARCHAR(200) NOT NULL COMMENT 'API端点路径',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理（User-Agent）',
    `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型：PC, MOBILE, TABLET, UNKNOWN',
    `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器：Chrome, Firefox, Safari等',
    `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统：Windows, macOS, Linux, iOS, Android等',
    `stat_date` DATETIME(6) NOT NULL COMMENT '统计日期时间（年月日时分秒）',
    `count` INT NOT NULL DEFAULT 1 COMMENT '访问次数',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_module_action_date_endpoint_ip` (`user_id`, `module`, `action`, `stat_date`, `endpoint`, `ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用统计表';

-- ============================================
-- 3. 文件备份表 (backup_files)
-- user_id是VARCHAR(36)用于存储UUID，没有外键约束
-- ============================================
CREATE TABLE `backup_files` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `original_file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_file_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `file_type` VARCHAR(50) NOT NULL COMMENT '文件MIME类型',
    `file_extension` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '文件描述',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '文件分类：image, document, archive等',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件备份表';

-- ============================================
-- 4. 密码条目表 (password_entries)
-- user_id是VARCHAR(36)用于存储UUID，没有外键约束
-- ============================================
CREATE TABLE `password_entries` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '密码条目ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `title` VARCHAR(255) NOT NULL COMMENT '标题',
    `website` VARCHAR(500) DEFAULT NULL COMMENT '网站地址',
    `encrypted_username` TEXT NOT NULL COMMENT '加密后的用户名',
    `encrypted_password` TEXT NOT NULL COMMENT '加密后的密码',
    `encrypted_notes` TEXT DEFAULT NULL COMMENT '加密后的备注',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类：工作、个人、金融等',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密码条目表';

-- ============================================
-- 5. 反馈表 (feedbacks)
-- user_id是VARCHAR(36)用于存储UUID，没有外键约束
-- ============================================
CREATE TABLE `feedbacks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `subject` VARCHAR(200) NOT NULL COMMENT '反馈主题',
    `content` TEXT NOT NULL COMMENT '反馈内容',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '反馈分类：bug, feature, suggestion, other',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决，CLOSED-已关闭',
    `admin_reply` VARCHAR(500) DEFAULT NULL COMMENT '管理员回复',
    `email_sent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发送邮件：1-已发送，0-未发送',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈表';

-- ============================================
-- 6. 联系表单表 (contacts)
-- 公开访问的联系表单，不需要关联用户表
-- ============================================
CREATE TABLE `contacts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '联系ID',
    `name` VARCHAR(100) NOT NULL COMMENT '姓名',
    `email` VARCHAR(255) NOT NULL COMMENT '邮箱',
    `subject` VARCHAR(200) NOT NULL COMMENT '主题',
    `message` TEXT NOT NULL COMMENT '消息内容',
    `email_sent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发送邮件：1-已发送，0-未发送',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '发送失败时的错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='联系表单表';


CREATE TABLE `front_errors` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                `user_id` VARCHAR(100) NOT NULL COMMENT '用户id',
                                `module` VARCHAR(50) NOT NULL COMMENT '模块名称：watermark, conversion, signature, compression, heic-to-jpg, password-manager, pages等',
                                `action` VARCHAR(50) NOT NULL COMMENT '操作名称：upload, download, preview, view, create, update, delete等',
                                `endpoint` VARCHAR(200) NOT NULL COMMENT 'API端点路径',
                                `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
                                `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型：PC, MOBILE, TABLET, UNKNOWN',
                                `error_message` VARCHAR(500) DEFAULT NULL COMMENT '发送失败时的错误信息',
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='前端运行异常表';



-- ============================================
-- 验证表结构
-- ============================================
SHOW TABLES;

-- 查看各表的user_id字段类型
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'commontools' 
    AND COLUMN_NAME = 'user_id'
ORDER BY TABLE_NAME;

-- 查看所有索引（应该只有PRIMARY KEY）
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'commontools'
ORDER BY TABLE_NAME, INDEX_NAME;
