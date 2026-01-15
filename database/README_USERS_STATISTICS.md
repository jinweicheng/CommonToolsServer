# 用户表和统计表设计文档

## 数据库信息

- **数据库类型**: MySQL 8.0+
- **数据库名称**: `commontools`
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci

## 表结构说明

### 1. users（用户表）

存储用户基本信息。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 用户ID | 主键，自增 |
| username | VARCHAR(50) | 用户名 | 唯一，非空 |
| password | VARCHAR(255) | 密码（BCrypt加密） | 非空 |
| email | VARCHAR(255) | 邮箱 | 唯一，非空 |
| phone | VARCHAR(20) | 手机号 | 唯一，可空 |
| nickname | VARCHAR(50) | 昵称 | 可空 |
| avatar | VARCHAR(500) | 头像URL | 可空 |
| gender | TINYINT(1) | 性别：0-未知，1-男，2-女 | 可空 |
| birthday | DATE | 生日 | 可空 |
| role | VARCHAR(20) | 角色：USER, ADMIN | 非空，默认USER |
| status | VARCHAR(20) | 账户状态：ACTIVE-正常，LOCKED-锁定，DISABLED-禁用 | 非空，默认ACTIVE |
| enabled | TINYINT(1) | 是否启用：1-启用，0-禁用 | 非空，默认1 |
| email_verified | TINYINT(1) | 邮箱是否验证：1-已验证，0-未验证 | 非空，默认0 |
| phone_verified | TINYINT(1) | 手机是否验证：1-已验证，0-未验证 | 非空，默认0 |
| last_login_time | DATETIME | 最后登录时间 | 可空 |
| last_login_ip | VARCHAR(50) | 最后登录IP地址 | 可空 |
| login_count | INT | 登录次数 | 非空，默认0 |
| register_source | VARCHAR(50) | 注册来源：WEB, APP, API等 | 可空 |
| remark | VARCHAR(500) | 备注 | 可空 |
| created_at | DATETIME | 创建时间 | 非空 |
| updated_at | DATETIME | 更新时间 | 非空 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY (username)
- UNIQUE KEY (email)
- UNIQUE KEY (phone)
- KEY (role)
- KEY (status)
- KEY (enabled)
- KEY (email_verified)
- KEY (created_at)
- KEY (last_login_time)

### 2. usage_statistics（使用统计表）

存储用户API调用统计信息（按日期和小时聚合）。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 统计ID | 主键，自增 |
| user_id | BIGINT | 用户ID | 外键，非空 |
| module | VARCHAR(50) | 模块名称 | 非空 |
| action | VARCHAR(50) | 操作名称 | 非空 |
| endpoint | VARCHAR(200) | API端点路径 | 非空 |
| method | VARCHAR(10) | HTTP方法 | 非空 |
| ip_address | VARCHAR(50) | IP地址 | 可空 |
| user_agent | VARCHAR(500) | 用户代理（User-Agent） | 可空 |
| device_type | VARCHAR(20) | 设备类型：PC, MOBILE, TABLET, UNKNOWN | 可空 |
| browser | VARCHAR(50) | 浏览器 | 可空 |
| os | VARCHAR(50) | 操作系统 | 可空 |
| response_time | INT | 响应时间（毫秒） | 可空 |
| status_code | INT | HTTP状态码 | 可空 |
| is_success | TINYINT(1) | 是否成功：1-成功，0-失败 | 默认1 |
| error_message | VARCHAR(500) | 错误信息 | 可空 |
| session_id | VARCHAR(100) | 会话ID | 可空 |
| stat_date | DATE | 统计日期 | 非空 |
| stat_hour | TINYINT | 统计小时（0-23） | 可空 |
| count | INT | 访问次数 | 非空，默认1 |
| created_at | DATETIME | 创建时间 | 非空 |

**模块名称示例：**
- files: 文件模块
- passwords: 密码管理器模块
- feedback: 反馈模块
- users: 用户模块

**操作名称示例：**
- upload: 上传
- download: 下载
- create: 创建
- update: 更新
- delete: 删除
- list: 列表查询
- view: 查看详情

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY (user_id, module, action, stat_date, stat_hour) - 防止重复统计
- KEY (user_id) - 外键索引
- KEY (module)
- KEY (action)
- KEY (stat_date)
- KEY (stat_hour)
- KEY (module, action) - 组合索引用于统计查询
- KEY (ip_address)
- KEY (device_type)
- KEY (status_code)
- KEY (is_success)
- KEY (created_at)

**外键：**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

## 常用统计SQL查询

### 1. 统计各模块的使用次数（按日期范围）

```sql
SELECT 
    module, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY module 
ORDER BY total_count DESC;
```

### 2. 统计各模块各操作的详细使用情况

```sql
SELECT 
    module, 
    action, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY module, action 
ORDER BY module, total_count DESC;
```

### 3. 统计功能点击率较低的操作（使用次数少于10次）

```sql
SELECT 
    module, 
    action, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY module, action 
HAVING total_count < 10 
ORDER BY total_count ASC;
```

### 4. 统计各模块的点击率（百分比）

```sql
SELECT 
    module,
    SUM(count) as module_count,
    ROUND(SUM(count) * 100.0 / (
        SELECT SUM(count) 
        FROM usage_statistics 
        WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31'
    ), 2) as percentage
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY module 
ORDER BY module_count DESC;
```

### 5. 按设备类型统计

```sql
SELECT 
    device_type, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY device_type 
ORDER BY total_count DESC;
```

### 6. 统计错误率

```sql
SELECT 
    module,
    SUM(CASE WHEN is_success = 0 THEN count ELSE 0 END) as error_count,
    SUM(count) as total_count,
    ROUND(SUM(CASE WHEN is_success = 0 THEN count ELSE 0 END) * 100.0 / SUM(count), 2) as error_rate
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY module 
ORDER BY error_rate DESC;
```

### 7. 统计平均响应时间

```sql
SELECT 
    module,
    action,
    AVG(response_time) as avg_response_time,
    MAX(response_time) as max_response_time,
    MIN(response_time) as min_response_time
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
    AND response_time IS NOT NULL
GROUP BY module, action 
ORDER BY avg_response_time DESC;
```

### 8. 按小时统计访问量（了解用户活跃时段）

```sql
SELECT 
    stat_hour, 
    SUM(count) as hourly_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY stat_hour 
ORDER BY stat_hour;
```

### 9. 统计最活跃的用户（Top 10）

```sql
SELECT 
    u.username, 
    u.email, 
    SUM(us.count) as total_usage 
FROM usage_statistics us 
JOIN users u ON us.user_id = u.id 
WHERE us.stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY u.id, u.username, u.email 
ORDER BY total_usage DESC 
LIMIT 10;
```

### 10. 统计IP访问排行（Top 20）

```sql
SELECT 
    ip_address, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
    AND ip_address IS NOT NULL
GROUP BY ip_address 
ORDER BY total_count DESC 
LIMIT 20;
```

### 11. 统计浏览器使用情况

```sql
SELECT 
    browser, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
    AND browser IS NOT NULL
GROUP BY browser 
ORDER BY total_count DESC;
```

### 12. 统计操作系统使用情况

```sql
SELECT 
    os, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
    AND os IS NOT NULL
GROUP BY os 
ORDER BY total_count DESC;
```

## 数据库初始化

执行 `schema_users_statistics.sql` 创建数据库和表结构。

## 注意事项

1. **外键约束**: 统计表设置了外键约束，删除用户时会级联删除相关统计数据
2. **索引优化**: 已为常用查询字段创建索引，提升查询性能
3. **字符集**: 使用utf8mb4支持emoji和特殊字符
4. **统计表**: usage_statistics表使用唯一索引防止重复统计，支持按小时统计
5. **生产环境**: 建议将ddl-auto设置为validate或none，使用SQL脚本手动创建表结构
