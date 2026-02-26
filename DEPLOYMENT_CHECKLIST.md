# ✅ 线上部署完整检查清单

## 📦 代码和配置检查

### ✓ 已完成的优化（无需修改）
- [x] AsyncConfig.java - 异步线程池 ✅
- [x] StatisticsAspect.java - 异步统计 ✅
- [x] RedisConfig.java - Redis连接优化 ✅
- [x] application.yml - 开发环境优化 ✅
- [x] application-prod.yml - **生产环境完整配置** ✅（刚更新）
- [x] pom.xml - 依赖优化 ✅

### ⚠️ 需要您检查的地方

#### 1️⃣ **application-prod.yml 中的默认值**
```yaml
# 检查这些地方是否需要修改（如已在线上配置了Redis）

redis:
  host: ${REDIS_HOST:127.0.0.1}  # ← 改为环境变量/实际值
  port: ${REDIS_PORT:6379}        # ← 改为环境变量/实际值
  password: ${REDIS_PASSWORD:}    # ← 改为环境变量/实际值

datasource:
  url: ${DB_URL:...}              # ← 改为环境变量/实际值
  username: ${DB_USERNAME:...}    # ← 改为环境变量/实际值
  password: ${DB_PASSWORD:...}    # ← 改为环境变量/实际值
```

**推荐方案：使用环境变量（已配置✅）**
- 优点：不需要修改代码，只需设置系统环境变量
- 缺点：需要手动配置

**快速方案：直接修改application-prod.yml（不推荐）**
- 优点：快速
- 缺点：每次部署都要改，容易出错，特别是密码泄露风险

#### 2️⃣ **检查是否有其他硬编码的配置**
```bash
# 在项目中搜索是否有硬编码的IP/端口
grep -r "localhost:6379" src/
grep -r "127.0.0.1" src/
grep -r "hardcoded" src/

# 如果有，需要修改为环境变量或配置文件
```

#### 3️⃣ **邮件配置（如需要）**
```yaml
mail:
  from: ${MAIL_FROM:noreply@example.com}
  admin: ${MAIL_ADMIN:admin@example.com}
```
检查是否需要邮件功能，如需要请配置环境变量。

#### 4️⃣ **文件存储路径**
```yaml
file:
  storage:
    path: ${FILE_STORAGE_PATH:/var/data/commontools/uploads}
```
需要在线上服务器创建该目录，并赋予应用用户写入权限。

---

## 🚀 部署流程检查

### 第1阶段：本地准备

- [ ] 代码已从git拉取最新版本
- [ ] 运行 `git log -1` 确认最新commit包含所有优化
- [ ] 本地编译成功：`mvn clean package -DskipTests`
- [ ] 目标JAR生成：`target/commontools-server-1.0.0.jar`
- [ ] JAR大小合理（应在80-120MB之间）

```bash
# 验证JAR包
ls -lh target/commontools-server-1.0.0.jar
file target/commontools-server-1.0.0.jar  # 应显示 JAR/Zip 文件
unzip -t target/commontools-server-1.0.0.jar | tail -5  # 验证JAR完整性
```

### 第2阶段：环境配置

#### Redis 环境检查
- [ ] Redis服务器已启动
- [ ] Redis可以从线上服务器访问（网络连通性）
- [ ] Redis授权凭证（密码）已准备好
- [ ] Redis内存和存储空间充足

```bash
# 线上服务器检查Redis连接（替换IP和端口）
redis-cli -h 192.168.1.100 -p 6379 -a password ping
# 预期返回: PONG
```

#### 数据库 环境检查
- [ ] MySQL服务器已启动并可访问
- [ ] 数据库 `commontools` 已存在
- [ ] 数据库用户已创建并有权限
- [ ] 数据库root密码已变更（安全性）

```bash
# 从线上服务器检查数据库连接
mysql -h db.example.com -u commontools -p -e "SELECT 1"
# 预期返回: 1
```

#### 系统环境检查
- [ ] Java已安装（JDK 17+）
- [ ] Java version -java -version` 验证
- [ ] ulimit 设置正确：`ulimit -n` 应≥10000
- [ ] 磁盘空间充足：至少5GB空闲
- [ ] 内存充足：至少2GB可用内存

```bash
# 检查Java版本
java -version  # 应显示 openjdk version "17.0.x"

# 检查ulimit
ulimit -n      # 应≥10000，否则执行：ulimit -n 10000

# 检查磁盘
df -h          # 寻找至少5GB空闲空间

# 检查内存
free -h        # 寻找至少2GB可用内存
```

### 第3阶段：部署前准备

- [ ] 已备份现有应用（如有）
  ```bash
  cp /opt/commontools/commontools-server-1.0.0.jar /opt/commontools/commontools-server-1.0.0.jar.backup.$(date +%Y%m%d_%H%M%S)
  ```

- [ ] 已创建必要的目录
  ```bash
  mkdir -p /var/log/commontools
  mkdir -p /var/data/commontools/uploads
  ```

- [ ] 已配置启动脚本
  ```bash
  # 复制并修改start-prod.sh中的环境变量
  vi /opt/commontools/start-prod.sh
  ```

- [ ] 已赋予执行权限
  ```bash
  chmod +x /opt/commontools/start-prod.sh
  chmod +x /opt/commontools/commontools-server-1.0.0.jar
  ```

### 第4阶段：部署执行

- [ ] 停止旧应用（如有）
  ```bash
  pkill -f "commontools-server" || true
  sleep 2  # 等待进程关闭
  ```

- [ ] 上传新JAR包
  ```bash
  scp target/commontools-server-1.0.0.jar user@server:/opt/commontools/
  ```

- [ ] 通过启动脚本启动新应用
  ```bash
  cd /opt/commontools
  ./start-prod.sh
  ```

- [ ] 等待应用启动（30-60秒）
  ```bash
  sleep 30 && tail -50 /var/log/commontools/commontools.log
  ```

### 第5阶段：部署验证

#### 日志检查
- [ ] 应用成功启动（查看启动日志）
  ```bash
  grep -i "started\|application" /var/log/commontools/commontools.log | head -10
  # 预期看到: "Application started successfully"
  ```

- [ ] 无FATAL/ERROR级别错误
  ```bash
  grep -i "FATAL\|ERROR" /var/log/commontools/commontools.log
  # 应该无结果或只有正常的外部错误
  ```

- [ ] Redis连接成功
  ```bash
  grep -i "redis\|connection pool" /var/log/commontools/commontools.log
  # 预期看到 redis连接正常信息
  ```

- [ ] 数据库连接成功
  ```bash
  grep -i "datasource\|initialized\|pool" /var/log/commontools/commontools.log
  # 预期看到 HikariCP连接池已初始化
  ```

#### 性能检查
- [ ] CPU占用从97%降到30-50%
  ```bash
  top -p $(pgrep -f commontools-server)
  # CPU占用应该在30-50%之间
  ```

- [ ] 内存使用稳定
  ```bash
  jstat -gc $(pgrep -f commontools-server) 5000
  # 观察GC情况，应无持续增长
  ```

- [ ] 响应正常
  ```bash
  curl -s http://localhost:8080/api/health | jq .
  # 预期返回: {"status":"UP"}
  ```

#### 功能检查
- [ ] 健康检查端点响应
  ```bash
  curl http://localhost:8080/api/health
  ```

- [ ] API端点响应（如登录）
  ```bash
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test"}'
  ```

- [ ] 数据库查询正常（如用户查询）
  ```bash
  curl http://localhost:8080/api/users/me \
    -H "Authorization: Bearer <token>"
  ```

---

## 🔍 常见问题排查

### ❌ 应用启动失败

**检查项：**
```bash
# 1. 查看详细错误
tail -200 /var/log/commontools/commontools.log | grep -A 5 "ERROR"

# 2. 检查Redis连接
redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} -a ${REDIS_PASSWORD} ping

# 3. 检查数据库连接
mysql -h ${DB_HOST} -u ${DB_USERNAME} -p${DB_PASSWORD} -e "SELECT 1"

# 4. 检查Java进程是否还在
ps aux | grep commontools
```

### ❌ CPU仍然很高

**检查项：**
- [ ] 是否使用了新的JAR包（确认JAR生成时间）
- [ ] 是否配置了统计功能（应禁用）
  ```yaml
  statistics:
    enabled: false
  ```
- [ ] 日志级别是否降低到WARN
  ```yaml
  logging:
    level:
      root: WARN
  ```
- [ ] 是否启用了DEBUG日志
  ```bash
  grep -i "level.*DEBUG\|level.*TRACE" start-prod.sh
  ```

### ❌ Redis连接失败

**检查项：**
- [ ] Redis主机地址正确
- [ ] Redis端口正确
- [ ] Redis密码正确（如有）
- [ ] 防火墙开放了Redis端口
- [ ] Redis服务器确实在运行

```bash
# 测试连接
telnet ${REDIS_HOST} ${REDIS_PORT}
# 或
nc -zv ${REDIS_HOST} ${REDIS_PORT}
```

### ❌ 数据库连接失败

**检查项：**
- [ ] 数据库主机地址正确
- [ ] 数据库端口正确
- [ ] 数据库用户名正确
- [ ] 数据库密码正确
- [ ] 数据库已创建
- [ ] 防火墙开放了数据库端口

```bash
# 测试连接
telnet ${DB_HOST} 3306
# 或
mysql -h ${DB_HOST} -u ${DB_USERNAME} -p
```

---

## 📊 性能基准（部署后应观察到）

| 指标 | 预期值 | 验证命令 |
|-----|--------|--------|
| CPU占用 | 30-50% | `top` |
| 响应时间 | 50-100ms | 应用日志 |
| TPS | 1000+ | 压力测试 |
| 内存占用 | 稳定 | `jstat -gc` |
| Redis连接 | <50ms | Redis监控 |

---

## ✅ 最终确认清单

部署完成后，请逐一确认：

- [ ] ✅ 应用启动成功，无ERROR日志
- [ ] ✅ CPU占用从97%降低到30-50%
- [ ] ✅ Redis连接正常
- [ ] ✅ 数据库连接正常
- [ ] ✅ 健康检查端点响应正常
- [ ] ✅ 业务API能正常调用
- [ ] ✅ 日志文件正常生成

---

## 🎯 部署总结

```
部署时间线：
├─ T-1小时：最后一次代码检查和备份
├─ T-0：停止旧应用
├─ T+1分钟：启动新应用
├─ T+2分钟：等待应用初始化
├─ T+3分钟：第一次验证（日志检查）
├─ T+5分钟：第二次验证（CPU检查）
├─ T+10到60分钟：持续监控，确保无异常
└─ 完成：部署成功，继续监测24小时
```

**预期结果：CPU从97%降到30-50%**

如需回滚，执行：
```bash
pkill -f commontools-server
cp /opt/commontools/commontools-server-1.0.0.jar.backup /opt/commontools/commontools-server-1.0.0.jar
./start-prod.sh
```
