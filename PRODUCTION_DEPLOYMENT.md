# 🚀 线上部署指南 - 生产环境配置

## 📋 部署前必读

已为生产环境 (application-prod.yml) 补充完整配置。**不能直接打包发布**，需要完成以下步骤：

---

## 🔧 第1步：编译打包

### 本地编译
```bash
cd d:\软考\CommonTools\CommonToolsServer

# 确保使用最新代码（清空旧编译）
mvn clean package -DskipTests -P prod

# 生成的JAR位置：
# target/commontools-server-1.0.0.jar
```

### 如果没有-P prod参数的配置
```bash
# 标准编译命令
mvn clean package -DskipTests
```

---

## 🌐 第2步：环境变量配置

在线上服务器部署前，**必须配置以下环境变量**：

### 📌 必需环境变量（与Redis相关）

```bash
# Redis 配置
export REDIS_HOST=你的Redis服务器IP        # 例：192.168.1.100
export REDIS_PORT=6379                     # Redis端口，默认6379
export REDIS_PASSWORD=your_redis_password  # Redis密码（如有）
export REDIS_ENABLED=true                  # 启用Redis

# 数据库配置
export DB_URL=jdbc:mysql://你的MySQL地址:3306/commontools?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
export DB_USERNAME=commontools
export DB_PASSWORD=数据库密码

# JWT配置（安全考虑，应该修改）
export JWT_SECRET=你的JWT密钥字符串      # 生成强随机密钥

# 前端地址
export FRONTEND_URL=https://你的前端地址

# 文件存储路径（生产环境）
export FILE_STORAGE_PATH=/var/data/commontools/uploads

# 邮件配置
export MAIL_FROM=noreply@yourdomain.com
export MAIL_ADMIN=admin@yourdomain.com
```

### 📌 可选环境变量

```bash
# OAuth2 配置（如需要社交登录）
export OAUTH2_ENABLED=false
export GOOGLE_CLIENT_ID=xxxxx
export GOOGLE_CLIENT_SECRET=xxxxx
# ... 其他OAuth2配置

# 统计功能（默认禁用以减少CPU）
export STATISTICS_ENABLED=false

# Logback配置
export LOG_PATH=/var/log/commontools
```

---

## 📝 第3步：创建启动脚本

### Linux/Mac 启动脚本: `start-prod.sh`

```bash
#!/bin/bash

# 设置堆内存（根据服务器配置调整）
export JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 配置环境变量
export REDIS_HOST=192.168.1.100         # 修改为实际Redis地址
export REDIS_PORT=6379
export REDIS_PASSWORD=your_password     # 如果有密码
export REDIS_ENABLED=true

export DB_URL="jdbc:mysql://db.example.com:3306/commontools?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
export DB_USERNAME=commontools
export DB_PASSWORD=db_password          # 修改为实际密码

export JWT_SECRET="your-super-secret-jwt-key-123456"
export FRONTEND_URL="https://app.example.com"
export FILE_STORAGE_PATH="/var/data/commontools/uploads"

# 启动应用
java ${JAVA_OPTS} \
  -Dspring.profiles.active=prod \
  -jar commontools-server-1.0.0.jar >> /var/log/commontools/startup.log 2>&1 &

echo "✅ 应用已启动"
```

### Windows 启动脚本: `start-prod.bat`

```batch
@echo off
REM 设置堆内存
set JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200

REM 配置环境变量
set REDIS_HOST=192.168.1.100
set REDIS_PORT=6379
set REDIS_PASSWORD=your_password
set REDIS_ENABLED=true

set DB_URL=jdbc:mysql://db.example.com:3306/commontools?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
set DB_USERNAME=commontools
set DB_PASSWORD=db_password

set JWT_SECRET=your-super-secret-jwt-key-123456
set FRONTEND_URL=https://app.example.com
set FILE_STORAGE_PATH=D:\commontools\uploads

REM 启动应用
java %JAVA_OPTS% -Dspring.profiles.active=prod -jar commontools-server-1.0.0.jar

pause
```

---

## 🚢 第4步：部署到线上

### 上传JAR包到服务器
```bash
# 从本地上传到服务器
scp target/commontools-server-1.0.0.jar user@server:/opt/commontools/

# 或使用FTP/SFTP工具上传
```

### 调整文件权限
```bash
chmod +x /opt/commontools/commontools-server-1.0.0.jar
chmod +x /opt/commontools/start-prod.sh
```

### 创建日志目录
```bash
mkdir -p /var/log/commontools
mkdir -p /var/data/commontools/uploads

# 赋予权限
chown -R user:group /var/log/commontools
chown -R user:group /var/data/commontools
chmod 755 /var/log/commontools
chmod 755 /var/data/commontools/uploads
```

### 启动应用
```bash
cd /opt/commontools
./start-prod.sh

# 查看启动日志
tail -f /var/log/commontools/commontools.log
```

---

## ✅ 第5步：验证部署

### 检查应用是否启动成功
```bash
# 查看进程
ps aux | grep commontools-server

# 查看启动日志（应该无错误）
tail -100 /var/log/commontools/commontools.log

# 预期看到：
# 2026-02-26 10:30:00 INFO Application started successfully
```

### 测试应用是否正常运行
```bash
# 测试健康检查端点
curl http://localhost:8080/api/health

# 预期返回: {"status":"UP"}
```

### 监测CPU占用
```bash
# 启动应用后监测（应该从97%降到30-50%）
top -p $(pgrep -f commontools-server)

# 查看CPU趋势
# 等待1-2分钟，观察CPU是否稳定
```

### 验证Redis连接
```bash
# 查看应用日志是否有Redis连接错误
grep -i "redis\|lettuce" /var/log/commontools/commontools.log

# 如果有连接错误，检查：
# 1. REDIS_HOST 地址是否正确
# 2. REDIS_PORT 是否正确
# 3. REDIS_PASSWORD 是否正确（如有）
# 4. Redis 服务器是否可访问（检查防火墙）
```

### 验证数据库连接
```bash
# 查看应用日志是否有数据库连接错误
grep -i "datasource\|connection" /var/log/commontools/commontools.log

# 如果有连接错误，检查：
# 1. DB_URL 地址是否正确
# 2. DB_USERNAME 用户名是否正确
# 3. DB_PASSWORD 密码是否正确
# 4. 数据库服务器是否可访问
```

---

## 📊 性能基准

部署完成后应观察到的指标：

| 指标 | 部署前 | 部署后 | 验证方法 |
|-----|--------|--------|--------|
| CPU占用 | 97% | 30-50% | `top` 命令 |
| 平均响应时间 | 150-200ms | 50-100ms | 应用日志 |
| Redis连接 | ❌ 无 | ✅ 已优化 | `redis-cli INFO` |
| 数据库连接 | 不足 | ✅ 优化为20 | 查看连接数 |

---

## ⚠️ 关键检查清单

部署前必须确认：

- [ ] ✅ Redis服务器已启动并可访问
- [ ] ✅ 数据库服务器已启动并可访问  
- [ ] ✅ 所有环境变量已正确配置
- [ ] ✅ 日志目录已创建且有写入权限
- [ ] ✅ 文件存储目录已创建且有写入权限
- [ ] ✅ 防火墙已开放应用端口（8080）
- [ ] ✅ JAR包已上传到线上服务器
- [ ] ✅ 已备份原有的应用（如有）
- [ ] ✅ 已阅读本指南的所有内容

---

## 🔙 故障回滚

如发生问题，立即停止应用：

```bash
# 停止应用
pkill -f commontools-server

# 或使用
kill $(pgrep -f commontools-server)

# 恢复备份（如有）
cp commontools-server-1.0.0.jar.backup commontools-server-1.0.0.jar

# 重新启动
./start-prod.sh
```

---

## 🔍 线上诊断

如应用运行异常，使用以下命令诊断：

```bash
# 1. 查看应用状态
ps aux | grep commontools

# 2. 查看完整错误日志
tail -200 /var/log/commontools/commontools.log | grep -i ERROR

# 3. 检查网络连接
netstat -tlnp | grep 8080

# 4. 检查磁盘空间
df -h

# 5. 检查内存占用
free -h

# 6. 实时监测性能
top -u $(whoami) -p $(pgrep -f commontools)
```

---

## 🎯 总结

| 步骤 | 操作 | 状态 |
|-----|------|------|
| 1️⃣ | 重新编译打包 | `mvn clean package` |
| 2️⃣ | 配置环境变量 | 修改 start-prod.sh |
| 3️⃣ | 创建启动脚本 | 保存脚本文件 |
| 4️⃣ | 上传到线上 | `scp` 上传JAR |
| 5️⃣ | 启动应用 | `./start-prod.sh` |
| 6️⃣ | 验证部署 | 检查CPU/日志/连接 |

---

**立即行动：**
1. 修改上面的环境变量为实际值
2. 复制启动脚本到服务器
3. 执行 `./start-prod.sh` 启动
4. 监测CPU变化（应在60秒内从97%降到30-50%）

**不能直接 `java -jar` 启动，必须使用启动脚本配置环境变量！**
