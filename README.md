# CommonTools Server

一个功能完善的Java后端服务，提供用户认证、文件备份和密码管理器功能。

## 功能特性

### 1. 用户认证系统
- ✅ 用户注册
- ✅ 用户登录（JWT Token认证）
- ✅ 密码加密存储（BCrypt）
- ✅ 用户信息管理

### 2. 文件备份功能
- ✅ 多格式文件上传支持
  - 图片：jpg, jpeg, png, gif
  - 文档：pdf, doc, docx, xls, xlsx, ppt, pptx, txt
  - 压缩文件：zip, rar, 7z
- ✅ 文件分类管理
- ✅ 文件下载
- ✅ 文件删除
- ✅ 文件信息查询

### 3. 密码管理器
- ✅ 密码加密存储（AES加密）
- ✅ 密码条目CRUD操作
- ✅ 密码分类管理
- ✅ 主密码保护机制

### 4. 反馈问题功能
- ✅ 用户反馈提交
- ✅ 自动发送邮件通知管理员
- ✅ 用户反馈确认邮件
- ✅ 反馈状态管理（待处理、处理中、已解决、已关闭）
- ✅ 管理员回复功能

### 5. 使用统计功能
- ✅ 自动记录API调用统计到数据库
- ✅ 数据库表设计完善，支持SQL自定义统计查询
- ✅ 记录模块、操作、用户、日期等维度信息

### 6. 其他功能
- ✅ 全局异常处理
- ✅ API文档（Swagger）
- ✅ CORS跨域支持
- ✅ 数据验证
- ✅ 用户权限管理

## 技术栈

- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0+
- **安全**: Spring Security + JWT
- **ORM**: Spring Data JPA
- **文档**: SpringDoc OpenAPI (Swagger)
- **邮件**: Spring Mail
- **AOP**: Spring AOP（用于统计记录）
- **构建工具**: Maven
- **Java版本**: 17

## 项目结构

```
src/
├── main/
│   ├── java/com/commontools/server/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # REST控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── exception/      # 异常处理
│   │   ├── model/          # 实体模型
│   │   ├── repository/     # 数据访问层
│   │   ├── security/       # 安全配置
│   │   ├── service/        # 业务逻辑层
│   │   └── util/           # 工具类
│   └── resources/
│       └── application.yml # 配置文件
└── pom.xml
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd CommonToolsServer
```

2. **编译项目**
```bash
mvn clean install
```

3. **运行项目**
```bash
mvn spring-boot:run
```

4. **访问应用**
- API地址: http://localhost:8080/api
- Swagger文档: http://localhost:8080/api/swagger-ui.html
- H2控制台: http://localhost:8080/api/h2-console

## API文档

### 认证接口

#### 1. 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "nickname": "测试用户"
}
```

#### 2. 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}

响应:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "testuser",
  "email": "test@example.com"
}
```

### 文件备份接口

#### 1. 上传文件
```
POST /api/files/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [文件]
description: "文件描述" (可选)
category: "image" (可选，支持: image, document, archive, other)
```

#### 2. 获取文件列表
```
GET /api/files?category=image (可选)
Authorization: Bearer {token}
```

#### 3. 下载文件
```
GET /api/files/{fileId}/download
Authorization: Bearer {token}
```

#### 4. 删除文件
```
DELETE /api/files/{fileId}
Authorization: Bearer {token}
```

### 密码管理器接口

#### 1. 创建密码条目
```
POST /api/passwords
Authorization: Bearer {token}
X-Master-Password: {主密码}
Content-Type: application/json

{
  "title": "GitHub",
  "website": "https://github.com",
  "username": "user@example.com",
  "password": "securePassword123",
  "notes": "备注信息",
  "category": "工作"
}
```

#### 2. 获取密码列表
```
GET /api/passwords?category=工作 (可选)
Authorization: Bearer {token}
X-Master-Password: {主密码}
```

#### 3. 获取单个密码条目
```
GET /api/passwords/{entryId}
Authorization: Bearer {token}
X-Master-Password: {主密码}
```

#### 4. 更新密码条目
```
PUT /api/passwords/{entryId}
Authorization: Bearer {token}
X-Master-Password: {主密码}
Content-Type: application/json

{
  "title": "GitHub",
  "username": "newuser@example.com",
  "password": "newPassword123",
  ...
}
```

#### 5. 删除密码条目
```
DELETE /api/passwords/{entryId}
Authorization: Bearer {token}
```

### 用户信息接口

#### 获取当前用户信息
```
GET /api/users/me
Authorization: Bearer {token}
```

### 反馈问题接口

#### 1. 提交反馈
```
POST /api/feedback
Authorization: Bearer {token}
Content-Type: application/json

{
  "subject": "发现了一个bug",
  "content": "详细描述问题...",
  "category": "bug"  // bug, feature, suggestion, other
}

响应: 反馈创建成功，会自动发送邮件给管理员和用户
```

#### 2. 获取我的反馈列表
```
GET /api/feedback/my
Authorization: Bearer {token}
```

#### 3. 获取单个反馈详情
```
GET /api/feedback/{feedbackId}
Authorization: Bearer {token}
```

#### 4. 管理员 - 获取所有反馈
```
GET /api/feedback/admin/all
Authorization: Bearer {token}
```

#### 5. 管理员 - 按状态查询反馈
```
GET /api/feedback/admin/status/{status}
Authorization: Bearer {token}
// status: PENDING, PROCESSING, RESOLVED, CLOSED
```

#### 6. 管理员 - 更新反馈状态
```
PUT /api/feedback/admin/{feedbackId}/status?status=RESOLVED&adminReply=问题已解决
Authorization: Bearer {token}
```

### 使用统计

统计功能会自动记录所有API调用到 `usage_statistics` 表，您可以使用SQL进行自定义统计查询。

**统计表字段：**
- `user_id`: 用户ID
- `module`: 模块名称（files, passwords, feedback等）
- `action`: 操作名称（upload, download, create等）
- `endpoint`: API端点路径
- `stat_date`: 统计日期
- `count`: 访问次数

**常用统计SQL示例：**

1. 统计各模块使用次数：
```sql
SELECT module, SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY module 
ORDER BY total_count DESC;
```

2. 统计各模块各操作详情：
```sql
SELECT module, action, SUM(count) as total_count 
FROM usage_statistics 
WHERE stat_date BETWEEN '2024-01-01' AND '2024-01-31' 
GROUP BY module, action 
ORDER BY module, total_count DESC;
```

更多统计SQL示例请参考 `database/README.md` 文件。

## 数据库设计

### 数据库表结构

项目包含以下5个核心表：

1. **users** - 用户表
2. **backup_files** - 文件备份表
3. **password_entries** - 密码条目表
4. **feedbacks** - 反馈表
5. **usage_statistics** - 使用统计表

详细的数据库设计文档和SQL脚本请查看 `database/` 目录：
- `database/recreate_all_tables.sql` - 完整的表结构创建脚本（推荐使用）
- `database/README.md` - 详细的数据库设计文档和常用SQL查询示例
- `database/init.sql` - 初始化脚本（可选，包含示例数据和视图、存储过程）

### 初始化数据库

1. 创建MySQL数据库并执行表结构脚本：
```bash
mysql -u root -p < database/recreate_all_tables.sql
```

2. 修改 `application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/commontools?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4&useUnicode=true
    username: root
    password: your_password
```

## 配置说明

### 开发环境配置 (application.yml)
- 使用MySQL数据库（默认配置）
- JWT密钥和过期时间配置
- 文件上传大小限制（100MB）
- 支持的文件扩展名列表
- 邮件服务器配置（需要配置SMTP服务器信息）
- 统计功能开关（默认开启）

### 邮件配置
在 `application.yml` 中配置邮件服务器信息：
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password  # Gmail需要使用应用专用密码
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

mail:
  from: your-email@gmail.com
  admin: admin@commontools.com  # 接收反馈的邮箱地址
```

**注意**: 
- Gmail需要开启"应用专用密码"功能
- 其他邮件服务商请参考相应的SMTP配置

### 生产环境配置

1. 使用 `application-prod.yml` 配置生产环境参数
2. 修改数据库连接信息
3. 建议将 `ddl-auto` 设置为 `validate` 或 `none`，使用 `database/schema.sql` 手动创建表结构
4. 运行：
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 安全说明

1. **密码加密**: 用户密码使用BCrypt加密存储
2. **JWT认证**: 使用JWT Token进行API认证
3. **密码管理器加密**: 密码条目使用AES加密，需要主密码才能解密
4. **文件访问控制**: 用户只能访问自己上传的文件
5. **数据隔离**: 每个用户的数据完全隔离

## 注意事项

1. **主密码**: 密码管理器的加密密钥由用户主密码派生，请妥善保管主密码
2. **文件存储**: 上传的文件存储在 `./uploads` 目录下，建议在生产环境中使用对象存储服务
3. **JWT密钥**: 生产环境请修改 `application.yml` 中的 `jwt.secret` 为更安全的密钥
4. **数据库**: 生产环境建议使用MySQL或PostgreSQL等生产级数据库
5. **邮件配置**: 需要正确配置SMTP服务器信息才能使用反馈功能
6. **统计功能**: 统计功能默认开启，可通过 `statistics.enabled=false` 关闭
7. **统计数据**: 统计数据按日期聚合，每天同一用户同一模块同一操作的访问次数会累加
8. **数据库统计**: 统计功能会自动记录到 `usage_statistics` 表，可以使用SQL进行自定义统计查询
9. **表结构**: 生产环境建议使用 `database/schema.sql` 手动创建表结构，而不是依赖JPA自动创建

## 开发建议

1. 添加单元测试和集成测试
2. 添加日志记录（Logback/Slf4j）
3. 添加缓存机制（Redis）提升性能
4. 添加文件压缩功能减少存储空间
5. 添加文件版本管理功能
6. 添加批量操作功能
7. 添加文件搜索功能

## 许可证

MIT License

