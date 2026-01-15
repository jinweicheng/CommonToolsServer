-- ============================================
-- 快速修复：修改 usage_statistics 表的 user_id 字段类型
-- 将 user_id 从整数类型改为 VARCHAR(36) 以支持UUID
-- ============================================

USE `commontools`;

-- 查看当前表结构（可选，用于确认）
-- DESCRIBE `usage_statistics`;
-- SHOW CREATE TABLE `usage_statistics`;

-- 如果表中有数据且需要保留，先备份（可选）
-- CREATE TABLE `usage_statistics_backup` AS SELECT * FROM `usage_statistics`;

-- 删除唯一约束（如果存在）
ALTER TABLE `usage_statistics` DROP INDEX IF EXISTS `uk_user_module_action_date`;

-- 删除相关索引
ALTER TABLE `usage_statistics` DROP INDEX IF EXISTS `idx_user_id`;

-- 修改 user_id 列类型为 VARCHAR(36)
ALTER TABLE `usage_statistics` 
MODIFY COLUMN `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID（UUID）';

-- 重新创建索引
ALTER TABLE `usage_statistics` 
ADD UNIQUE KEY `uk_user_module_action_date` (`user_id`, `module`, `action`, `stat_date`, `stat_hour`);

ALTER TABLE `usage_statistics` 
ADD KEY `idx_user_id` (`user_id`);

-- 验证修改结果
DESCRIBE `usage_statistics`;
