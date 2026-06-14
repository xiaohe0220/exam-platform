# 人工智能通识教育考试平台（MVP）

单体应用：Spring Boot 3.2 + Vue 3 + Element Plus + MySQL（或本地 H2 演示）。

## 功能概要

- **教师/管理员**：题库（单选/多选/判断/填空/简答）、题库 Excel 批量导入、手动组卷、按题型随机组卷、发布考试、答卷列表、题目级正确率分析、成绩/答卷导出、主观题批阅。
- **学生**：查看可参加考试、进入全屏答题页、自动保存答案、切屏上报、全屏退出/复制粘贴/右键等安全事件审计、达上限自动交卷、交卷后查看分数、错题本、留言反馈。
- **管理员**：用户导入导出、账号启停/角色/密码/邮箱/手机号维护、班级与课程管理、真实运行监控、审计日志导出、平台数据分析、核心数据 Excel 备份导出。

## 快速启动（推荐：H2 内存库，无需 Docker）

1. 安装 **JDK 17**、**Maven**、**Node.js/npm**。
2. 后端：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

3. 前端：

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173` 。

### 演示账号（由 `DataInitializer` 写入）

> 仅 `local` / `test` profile 默认写入演示数据。生产环境默认关闭演示账号与样例考试。

| 账号      | 密码       | 角色   |
|-----------|------------|--------|
| teacher   | teacher123 | 教师   |
| student   | student123 | 学生（班级：计算机2101） |
| admin     | admin123   | 教务处 |

教师已预置一场已发布的「人工智能通识 - 样例期末考试」，学生登录后即可在列表中进入。

## 使用 MySQL + Docker（与生产接近）

1. 在项目根目录执行：

```bash
docker compose up -d
```

2. **去掉** `local` profile，使用默认 `application.yml`（端口 `3307` 与 `backend` 中配置一致）。
3. `mvn spring-boot:run`（无 `-Dspring-boot.run.profiles=local`）。

## 服务器部署：数据库初始化（MySQL）

1. 在目标服务器安装 MySQL 8.x，并执行脚本创建库和应用账号：

```bash
cd backend/scripts/mysql
mysql -uroot -p < create_database.sql
```

2. 启动后端前设置环境变量（示例）：

```bash
export SPRING_PROFILES_ACTIVE=mysql
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=exam_platform
export DB_USERNAME=exam_user
export DB_PASSWORD=change_me
export APP_JWT_SECRET='请替换为至少32字节的生产密钥'
export APP_SEED_DEMO_DATA_ENABLED=false
export APP_PUBLIC_REGISTRATION_ENABLED=true
export APP_DEMO_PASSWORD_RESET_ENABLED=false
# 空库首次启动时可临时设置，创建首个管理员后请移除
export APP_BOOTSTRAP_ADMIN_USERNAME=admin
export APP_BOOTSTRAP_ADMIN_PASSWORD='replace_with_strong_admin_password'
# 可选：接入 OpenAI 兼容大模型接口，未配置 API Key 时会使用离线规则回复
export APP_AGENT_ENABLED=true
export APP_AGENT_API_KEY='your_llm_api_key'
export APP_AGENT_BASE_URL='https://api.openai.com/v1/chat/completions'
export APP_AGENT_MODEL='gpt-4o-mini'
# 可选：邮件/短信通知。邮件需配置 spring.mail.*；短信使用 Webhook。
export SPRING_MAIL_HOST=smtp.example.com
export SPRING_MAIL_PORT=587
export SPRING_MAIL_USERNAME=notice@example.com
export SPRING_MAIL_PASSWORD='mail_password'
export APP_NOTIFY_SMS_WEBHOOK_URL='https://sms-gateway.example.com/send'
# 默认收紧 CORS；推荐在 app.cors.allowed-origins 中配置前端域名
export APP_CORS_ALLOW_ALL_ORIGINS=false
```

3. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

> 说明：当前 `mysql` profile 使用 `ddl-auto=update`，首次部署会自动创建/补齐表结构。生产稳定后建议改为 `validate` 并引入迁移工具（如 Flyway）管理版本。

## 公网访问部署说明

- 后端默认监听 `0.0.0.0:8080`，确保服务器安全组/防火墙已放行 `8080`（或你反代后的端口 `80/443`）。
- 前端建议使用 `npm run build` 后由 Nginx 托管静态文件，再反向代理 `/api` 到后端 `8080`。
- 如果你要让任意网站都能直接调用后端 API，可设置 `APP_CORS_ALLOW_ALL_ORIGINS=true`。
- 更推荐做法是仅配置你的前端域名到 `app.cors.allowed-origins`，安全性更高。

### 上线安全清单

- 必须设置 `APP_JWT_SECRET`，且长度不少于 32 字节；非 `local/test` 环境使用默认密钥会拒绝启动。
- 生产默认关闭演示数据和无验证码公开重置密码：`APP_SEED_DEMO_DATA_ENABLED=false`、`APP_DEMO_PASSWORD_RESET_ENABLED=false`。
- 自主注册默认开启：`APP_PUBLIC_REGISTRATION_ENABLED=true`。注册账号必须使用本人学号/工号，后端会按学号/工号唯一约束确保一人一账号；如需关闭自主注册，可设置为 `false`。
- 空库首次上线请临时设置 `APP_BOOTSTRAP_ADMIN_USERNAME` 和 `APP_BOOTSTRAP_ADMIN_PASSWORD` 创建首个管理员，登录后立即修改密码并移除这两个变量。
- 学生/教师忘记密码请由管理员在用户管理中重置；已登录用户可在个人资料页修改自己的密码。
- 后端会按考试开始时间、考试时长和考试结束时间做服务端超时校验，前端倒计时只作为交互提示。
- 生产建议不要使用 `local` profile 对外服务；MySQL 稳定后将 `ddl-auto=update` 改为 `validate` 并接入 Flyway/Liquibase。
- 只开放 HTTPS 入口，Nginx 反代 `/api` 到后端内网端口，并将 CORS 限定为前端域名。

### 服务器配置大模型助手

平台右下角「智能助手」默认可用离线规则回复。要接入真实大模型，请在后端服务器的 systemd 环境变量中配置 OpenAI 兼容的 Chat Completions 接口：

```bash
export APP_AGENT_ENABLED=true
export APP_AGENT_API_KEY='your_llm_api_key'
export APP_AGENT_BASE_URL='https://api.openai.com/v1/chat/completions'
export APP_AGENT_MODEL='gpt-4o-mini'
```

如果使用私有网关、国产模型或自建兼容服务，只要接口兼容 `/v1/chat/completions` 请求/响应结构，把 `APP_AGENT_BASE_URL` 和 `APP_AGENT_MODEL` 换成对应值即可。例如：

```bash
export APP_AGENT_BASE_URL='https://your-gateway.example.com/v1/chat/completions'
export APP_AGENT_MODEL='your-model-name'
```

修改服务器环境变量后重启后端：

```bash
sudo systemctl daemon-reload
sudo systemctl restart exam-platform
sudo journalctl -u exam-platform -f
```

登录系统后打开右下角「智能助手」，抽屉标题会显示「已接入 <model>」；若未配置 `APP_AGENT_API_KEY`，会显示「离线规则」。

### 通知、监控与备份

- 站内通知默认可用；在「平台设置」开启邮件后，需要配置 `SPRING_MAIL_HOST`、`SPRING_MAIL_PORT`、`SPRING_MAIL_USERNAME`、`SPRING_MAIL_PASSWORD`。
- 在「平台设置」开启短信后，需要设置 `APP_NOTIFY_SMS_WEBHOOK_URL`，系统会向该地址 POST `{ phone, text }`。
- 管理端「监控」页会实时探测 API、数据库、Redis（若启用）、JVM 内存、CPU、在线考试会话与切屏预警。
- 管理端「数据」页可导出核心数据备份工作簿；服务器定时备份建议使用：

```bash
BACKUP_DIR=/opt/exam-platform/backups \
DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=exam_platform \
DB_USERNAME=exam_user DB_PASSWORD='change_me' \
bash scripts/backup-mysql.sh
```

恢复前请先确认目标库：

```bash
DB_NAME=exam_platform DB_USERNAME=exam_user DB_PASSWORD='change_me' \
bash scripts/restore-mysql.sh /opt/exam-platform/backups/exam_platform_YYYYMMDD_HHMMSS.sql.gz
```

## 一键部署脚本（Linux 服务器）

项目已提供脚本：`scripts/deploy-server.sh`，会自动完成：

- 前端构建并发布到 `/var/www/exam-platform`
- 执行数据库初始化脚本
- 生成并启动后端 `systemd` 服务
- 生成并启用 Nginx 站点配置

执行前请先设置关键变量（示例）：

```bash
export PROJECT_DIR=/opt/exam-platform
export DOMAIN=your.domain.or.ip
export MYSQL_ROOT_PASSWORD='your_mysql_root_password'
export DB_PASSWORD='change_me'
export APP_JWT_SECRET='replace_with_secure_secret_min_32_bytes'
export APP_CORS_ALLOW_ALL_ORIGINS=false
export APP_SEED_DEMO_DATA_ENABLED=false
export APP_PUBLIC_REGISTRATION_ENABLED=true
export APP_DEMO_PASSWORD_RESET_ENABLED=false
export APP_BOOTSTRAP_ADMIN_USERNAME=admin
export APP_BOOTSTRAP_ADMIN_PASSWORD='replace_with_strong_admin_password'
export APP_AGENT_ENABLED=true
export APP_AGENT_API_KEY='your_llm_api_key'
export APP_AGENT_BASE_URL='https://api.openai.com/v1/chat/completions'
export APP_AGENT_MODEL='gpt-4o-mini'
export APP_NOTIFY_SMS_WEBHOOK_URL=''
```

执行部署：

```bash
cd /opt/exam-platform
sudo bash scripts/deploy-server.sh
```

可选模板文件：

- `scripts/templates/exam-platform.service.template`
- `scripts/templates/nginx.exam-platform.conf.template`

### Redis（交卷分布式幂等，可选）

1. `docker compose up -d` 启动 MySQL 与 Redis。
2. 后端增加 **`docker` profile**（已提供 `application-docker.yml`）：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

默认配置中 `app.redis.enabled` 为 `false`，无需 Redis 即可运行；启用 `docker` profile 后会连接 `localhost:6380` 的 Redis 并启用交卷 SETNX 幂等。

## 主观题批阅（HTTP 示例）

教师登录拿到 `Bearer` 后：

```http
POST /api/teacher/exams/{examId}/attempts/{attemptId}/grade
Content-Type: application/json
Authorization: Bearer <token>

{"questionId":5,"score":18,"comment":"论述较完整"}
```

## 目录结构

- `backend/`：Spring Boot 工程
- `frontend/`：Vite + Vue3 + Element Plus
- `docker-compose.yml`：MySQL 8 + Redis（Redis 可为后续缓存/会话扩展预留）

## 说明

当前版本已覆盖考试发布、作答、阅卷、分析、监控、审计、备份与通知配置。视频监考/人脸核验属于外部硬件与服务集成项，当前已保留考试安全事件审计链路，接入摄像头或人脸服务时建议继续复用该审计模型。
