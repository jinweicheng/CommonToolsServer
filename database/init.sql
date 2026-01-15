-- ============================================
-- CommonTools Server 数据库初始化脚本
-- 包含示例数据和常用查询
-- ============================================

USE `commontools`;

-- ============================================
-- 插入示例数据（可选）
-- ============================================

-- 插入管理员用户（密码：admin123，已BCrypt加密）
-- INSERT INTO users (username, password, email, nickname, role, enabled) 
-- VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJZ2C', 'admin@commontools.com', '管理员', 'ADMIN', 1);

-- ============================================
-- 常用查询视图（可选）
-- ============================================

-- 创建模块使用统计视图
CREATE OR REPLACE VIEW v_module_statistics AS
SELECT 
    module,
    SUM(count) as total_count,
    COUNT(DISTINCT user_id) as user_count,
    COUNT(DISTINCT action) as action_count
FROM usage_statistics
GROUP BY module;

-- 注意：由于usage_statistics表的user_id是VARCHAR(36)存储UUID，不能直接JOIN users表
-- 如果需要用户活跃度统计，可以使用以下视图（基于user_id字符串匹配）
-- CREATE OR REPLACE VIEW v_user_activity AS
-- SELECT 
--     user_id,
--     COUNT(DISTINCT DATE(stat_date)) as active_days,
--     SUM(count) as total_requests
-- FROM usage_statistics
-- GROUP BY user_id;

-- ============================================
-- 存储过程示例（可选）
-- ============================================

-- 获取模块统计的存储过程（更新为支持DATETIME类型）
DELIMITER //
CREATE PROCEDURE sp_get_module_statistics(
    IN start_datetime DATETIME,
    IN end_datetime DATETIME
)
BEGIN
    SELECT 
        module,
        SUM(count) as total_count,
        ROUND(SUM(count) * 100.0 / (
            SELECT SUM(count) 
            FROM usage_statistics 
            WHERE stat_date BETWEEN start_datetime AND end_datetime
        ), 2) as percentage
    FROM usage_statistics 
    WHERE stat_date BETWEEN start_datetime AND end_datetime 
    GROUP BY module 
    ORDER BY total_count DESC;
END //
DELIMITER ;

-- 使用示例：
-- CALL sp_get_module_statistics('2024-01-01 00:00:00', '2024-01-31 23:59:59');
