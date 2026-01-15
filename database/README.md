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
| username | VARCHAR(50) | 用户名 | 唯一，非空 |
| password | VARCHAR(255) | 密码（BCrypt加密） | 非空 |
| email | VARCHAR(255) | 邮箱 | 唯一，非空 |
| nickname | VARCHAR(50) | 昵称 | 可空 |
| role | VARCHAR(20) | 角色 | 非空，默认USER |
| enabled | TINYINT(1) | 是否启用 | 非空，默认1 |
| created_at | DATETIME | 创建时间 | 非空 |
| updated_at | DATETIME | 更新时间 | 非空 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY (username)
- UNIQUE KEY (email)
- KEY (role)
- KEY (enabled)
- KEY (created_at)

### 2. backup_files（文件备份表）

存储用户上传的文件信息。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 文件ID | 主键，自增 |
| user_id | BIGINT | 用户ID | 外键，非空 |
| original_file_name | VARCHAR(255) | 原始文件名 | 非空 |
| stored_file_name | VARCHAR(255) | 存储文件名 | 非空 |
| file_path | VARCHAR(500) | 文件存储路径 | 非空 |
| file_size | BIGINT | 文件大小（字节） | 非空 |
| file_type | VARCHAR(50) | 文件MIME类型 | 非空 |
| file_extension | VARCHAR(20) | 文件扩展名 | 可空 |
| description | VARCHAR(500) | 文件描述 | 可空 |
| category | VARCHAR(50) | 文件分类 | 可空 |
| created_at | DATETIME | 创建时间 | 非空 |
| updated_at | DATETIME | 更新时间 | 非空 |

**索引：**
- PRIMARY KEY (id)
- KEY (user_id) - 外键索引
- KEY (category)
- KEY (created_at)
- KEY (file_type)

**外键：**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

### 3. password_entries（密码条目表）

存储用户密码管理器中的密码条目（加密存储）。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 密码条目ID | 主键，自增 |
| user_id | BIGINT | 用户ID | 外键，非空 |
| title | VARCHAR(255) | 标题 | 非空 |
| website | VARCHAR(500) | 网站地址 | 可空 |
| encrypted_username | TEXT | 加密后的用户名 | 非空 |
| encrypted_password | TEXT | 加密后的密码 | 非空 |
| encrypted_notes | TEXT | 加密后的备注 | 可空 |
| category | VARCHAR(50) | 分类 | 可空 |
| created_at | DATETIME | 创建时间 | 非空 |
| updated_at | DATETIME | 更新时间 | 非空 |

**索引：**
- PRIMARY KEY (id)
- KEY (user_id) - 外键索引
- KEY (category)
- KEY (created_at)
- KEY (title)

**外键：**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

### 4. feedbacks（反馈表）

存储用户提交的反馈信息。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 反馈ID | 主键，自增 |
| user_id | BIGINT | 用户ID | 外键，非空 |
| subject | VARCHAR(200) | 反馈主题 | 非空 |
| content | TEXT | 反馈内容 | 非空 |
| category | VARCHAR(50) | 反馈分类 | 可空 |
| status | VARCHAR(20) | 处理状态 | 非空，默认PENDING |
| admin_reply | VARCHAR(500) | 管理员回复 | 可空 |
| email_sent | TINYINT(1) | 是否已发送邮件 | 非空，默认0 |
| created_at | DATETIME | 创建时间 | 非空 |
| updated_at | DATETIME | 更新时间 | 非空 |

**状态枚举：**
- PENDING: 待处理
- PROCESSING: 处理中
- RESOLVED: 已解决
- CLOSED: 已关闭

**索引：**
- PRIMARY KEY (id)
- KEY (user_id) - 外键索引
- KEY (status)
- KEY (category)
- KEY (created_at)

**外键：**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

### 5. usage_statistics（使用统计表）

存储用户API调用统计信息（按日期聚合）。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 统计ID | 主键，自增 |
| user_id | BIGINT | 用户ID | 外键，非空 |
| module | VARCHAR(50) | 模块名称 | 非空 |
| action | VARCHAR(50) | 操作名称 | 非空 |
| endpoint | VARCHAR(200) | API端点路径 | 非空 |
| stat_date | DATE | 统计日期 | 非空 |
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
- UNIQUE KEY (user_id, module, action, stat_date) - 防止重复统计
- KEY (user_id) - 外键索引
- KEY (module)
- KEY (action)
- KEY (stat_date)
- KEY (module, action) - 组合索引用于统计查询

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

### 3. 统计某个用户的使用情况

```sql
SELECT 
    module, 
    action, 
    SUM(count) as total_count 
FROM usage_statistics 
WHERE user_id = 1 
    AND stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY module, action 
ORDER BY total_count DESC;
```

### 4. 统计每日访问量趋势

```sql
SELECT 
    stat_date, 
    SUM(count) as daily_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY stat_date 
ORDER BY stat_date;
```

### 5. 统计最活跃的用户（Top 10）

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

### 6. 统计各模块的点击率（百分比）

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

### 7. 统计功能点击率较低的操作（使用次数少于10次）

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

### 8. 统计各模块的点击率排名（包含占比）

```sql
SELECT 
    module,
    SUM(count) as total_count,
    ROUND(SUM(count) * 100.0 / (
        SELECT SUM(count) 
        FROM usage_statistics 
        WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31'
    ), 2) as percentage,
    RANK() OVER (ORDER BY SUM(count) DESC) as rank
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
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
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY action 
ORDER BY total_count DESC;
```

### 10. 统计最近7天的模块使用趋势

```sql
SELECT 
    stat_date,
    module,
    SUM(count) as daily_count
FROM usage_statistics 
WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY stat_date, module
ORDER BY stat_date DESC, daily_count DESC;
```

## 数据库初始化

1. 执行 `schema.sql` 创建数据库和表结构
2. 修改 `application.yml` 中的数据库连接信息
3. 启动应用，JPA会自动创建表结构（如果使用ddl-auto=update）

## 注意事项

1. **外键约束**: 所有表都设置了外键约束，删除用户时会级联删除相关数据
2. **索引优化**: 已为常用查询字段创建索引，提升查询性能
3. **字符集**: 使用utf8mb4支持emoji和特殊字符
4. **统计表**: usage_statistics表使用唯一索引防止重复统计，每天同一用户同一模块同一操作只记录一条记录，count字段累加
5. **生产环境**: 建议将ddl-auto设置为validate或none，使用schema.sql手动创建表结构
