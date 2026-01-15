# 数据库设计文档

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
| username | VARCHAR(50) | 用户名 | 非空 |
| password | VARCHAR(255) | 密码（BCrypt加密） | 非空 |
| email | VARCHAR(255) | 邮箱 | 非空 |
| phone | VARCHAR(20) | 手机号 | 可空 |
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

### 2. backup_files（文件备份表）

存储用户上传的文件信息。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 文件ID | 主键，自增 |
| user_id | VARCHAR(36) | 用户ID（UUID） | 非空 |
| original_file_name | VARCHAR(255) | 原始文件名 | 非空 |
| stored_file_name | VARCHAR(255) | 存储文件名 | 非空 |
| file_path | VARCHAR(500) | 文件存储路径 | 非空 |
| file_size | BIGINT | 文件大小（字节） | 非空 |
| file_type | VARCHAR(50) | 文件MIME类型 | 非空 |
| file_extension | VARCHAR(20) | 文件扩展名 | 可空 |
| description | VARCHAR(500) | 文件描述 | 可空 |
| category | VARCHAR(50) | 文件分类：image, document, archive等 | 可空 |
| created_at | DATETIME | 创建时间 | 非空 |
| updated_at | DATETIME | 更新时间 | 非空 |

**索引：**
- PRIMARY KEY (id)

### 3. password_entries（密码条目表）

存储用户密码管理器中的密码条目（加密存储）。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 密码条目ID | 主键，自增 |
| user_id | VARCHAR(36) | 用户ID（UUID） | 非空 |
| title | VARCHAR(255) | 标题 | 非空 |
| website | VARCHAR(500) | 网站地址 | 可空 |
| encrypted_username | TEXT | 加密后的用户名 | 非空 |
| encrypted_password | TEXT | 加密后的密码 | 非空 |
| encrypted_notes | TEXT | 加密后的备注 | 可空 |
| category | VARCHAR(50) | 分类：工作、个人、金融等 | 可空 |
| created_at | DATETIME | 创建时间 | 非空 |
| updated_at | DATETIME | 更新时间 | 非空 |

**索引：**
- PRIMARY KEY (id)

### 4. feedbacks（反馈表）

存储用户提交的反馈信息。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 反馈ID | 主键，自增 |
| user_id | VARCHAR(36) | 用户ID（UUID） | 非空 |
| subject | VARCHAR(200) | 反馈主题 | 非空 |
| content | TEXT | 反馈内容 | 非空 |
| category | VARCHAR(50) | 反馈分类：bug, feature, suggestion, other | 可空 |
| status | VARCHAR(20) | 处理状态：PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决，CLOSED-已关闭 | 非空，默认PENDING |
| admin_reply | VARCHAR(500) | 管理员回复 | 可空 |
| email_sent | TINYINT(1) | 是否已发送邮件：1-已发送，0-未发送 | 非空，默认0 |
| created_at | DATETIME | 创建时间 | 非空 |
| updated_at | DATETIME | 更新时间 | 非空 |

**状态枚举：**
- PENDING: 待处理
- PROCESSING: 处理中
- RESOLVED: 已解决
- CLOSED: 已关闭

**索引：**
- PRIMARY KEY (id)

### 5. usage_statistics（使用统计表）

存储用户API调用统计信息（按日期时间聚合）。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 统计ID | 主键，自增 |
| user_id | VARCHAR(36) | 用户ID（UUID，不引用users表） | 非空 |
| module | VARCHAR(50) | 模块名称 | 非空 |
| action | VARCHAR(50) | 操作名称 | 非空 |
| endpoint | VARCHAR(200) | API端点路径 | 非空 |
| ip_address | VARCHAR(50) | IP地址 | 可空 |
| user_agent | VARCHAR(500) | 用户代理（User-Agent） | 可空 |
| device_type | VARCHAR(20) | 设备类型：PC, MOBILE, TABLET, UNKNOWN | 可空 |
| browser | VARCHAR(50) | 浏览器：Chrome, Firefox, Safari等 | 可空 |
| os | VARCHAR(50) | 操作系统：Windows, macOS, Linux, iOS, Android等 | 可空 |
| stat_date | DATETIME(6) | 统计日期时间（年月日时分秒） | 非空 |
| count | INT | 访问次数 | 非空，默认1 |
| created_at | DATETIME(6) | 创建时间 | 非空 |

**模块名称示例：**
- watermark: 水印模块
- conversion: 格式转换模块
- signature: 签名模块
- compression: 压缩模块
- heic-to-jpg: HEIC转JPG模块
- password-manager: 密码管理器模块
- pages: 页面模块

**操作名称示例：**
- upload: 上传
- download: 下载
- preview: 预览
- view: 查看
- create: 创建
- update: 更新
- delete: 删除
- list: 列表查询

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY (user_id, module, action, stat_date, endpoint, ip_address) - 防止重复统计

**重要说明：**
- `user_id` 字段使用 `VARCHAR(36)` 存储UUID字符串，不设置外键约束
- `stat_date` 字段使用 `DATETIME(6)` 类型，包含年月日时分秒，支持微秒精度
- 唯一索引确保同一用户、同一模块、同一操作、同一时间、同一端点、同一IP只记录一条统计，`count` 字段累加

## 数据库初始化

### 方式一：使用SQL脚本（推荐）

执行 `recreate_all_tables.sql` 脚本创建所有表：

```bash
mysql -u root -p < database/recreate_all_tables.sql
```

或者在MySQL客户端中执行：

```sql
source database/recreate_all_tables.sql;
```

### 方式二：使用JPA自动创建

在 `application.yml` 中配置：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

**注意：** 生产环境建议使用方式一，手动执行SQL脚本创建表结构。

## 常用统计SQL查询

### 1. 统计各模块的使用次数（按日期时间范围）

```sql
SELECT 
    module, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
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
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
GROUP BY module, action 
ORDER BY module, total_count DESC;
```

### 3. 统计某个用户的使用情况

```sql
SELECT 
    module, 
    action, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE user_id = 'f7237d9d-7453-429e-853c-7b7bb99104cc' 
    AND stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
GROUP BY module, action 
ORDER BY total_count DESC;
```

### 4. 统计每日访问量趋势

```sql
SELECT 
    DATE(stat_date) as stat_day, 
    SUM(count) as daily_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
GROUP BY DATE(stat_date) 
ORDER BY stat_day;
```

### 5. 统计每小时访问量趋势

```sql
SELECT 
    DATE(stat_date) as stat_day,
    HOUR(stat_date) as stat_hour,
    SUM(count) as hourly_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
GROUP BY DATE(stat_date), HOUR(stat_date)
ORDER BY stat_day, stat_hour;
```

### 6. 统计各模块的点击率（百分比）

```sql
SELECT 
    module,
    SUM(count) as module_count,
    ROUND(SUM(count) * 100.0 / (
        SELECT SUM(count) 
        FROM usage_statistics 
        WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59'
    ), 2) as percentage
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
GROUP BY module 
ORDER BY module_count DESC;
```

### 7. 统计功能点击率较低的操作（使用次数少于10次）

```sql
SELECT 
    module, 
    action, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
GROUP BY module, action 
HAVING total_count < 10 
ORDER BY total_count ASC;
```

### 8. 统计各模块的点击率排名（包含占比）

```sql
SELECT 
    module,
    SUM(count) as total_count,
    ROUND(SUM(count) * 100.0 / (
        SELECT SUM(count) 
        FROM usage_statistics 
        WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59'
    ), 2) as percentage,
    RANK() OVER (ORDER BY SUM(count) DESC) as rank
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
GROUP BY module 
ORDER BY total_count DESC;
```

### 9. 统计各操作的使用频率（跨模块）

```sql
SELECT 
    action, 
    SUM(count) as total_count,
    COUNT(DISTINCT module) as module_count
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
GROUP BY action 
ORDER BY total_count DESC;
```

### 10. 统计最近7天的模块使用趋势

```sql
SELECT 
    DATE(stat_date) as stat_day,
    module,
    SUM(count) as daily_count
FROM usage_statistics 
WHERE stat_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(stat_date), module
ORDER BY stat_day DESC, daily_count DESC;
```

### 11. 统计设备类型分布

```sql
SELECT 
    device_type,
    COUNT(*) as record_count,
    SUM(count) as total_count
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
GROUP BY device_type
ORDER BY total_count DESC;
```

### 12. 统计浏览器分布

```sql
SELECT 
    browser,
    COUNT(*) as record_count,
    SUM(count) as total_count
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59' 
    AND browser IS NOT NULL
GROUP BY browser
ORDER BY total_count DESC;
```

## 注意事项

1. **外键约束**: 所有表都没有设置外键约束，`user_id` 字段使用 `VARCHAR(36)` 存储UUID字符串
2. **索引优化**: 所有表只保留PRIMARY KEY，移除了其他索引以简化结构
3. **字符集**: 使用utf8mb4支持emoji和特殊字符
4. **统计表**: `usage_statistics` 表使用唯一索引防止重复统计，同一用户同一模块同一操作同一时间同一端点同一IP只记录一条记录，`count` 字段累加
5. **日期时间**: `stat_date` 字段使用 `DATETIME(6)` 类型，包含年月日时分秒，支持微秒精度
6. **生产环境**: 建议使用 `recreate_all_tables.sql` 手动创建表结构，而不是依赖JPA自动创建
7. **AUTO_INCREMENT**: `id` 字段使用 `BIGINT` 类型，自增ID范围非常大（-2^63 到 2^63-1），不会有限制
