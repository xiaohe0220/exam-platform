# 人工智能通识教育考试平台（MVP）

单体应用：Spring Boot 3.2 + Vue 3 + Element Plus + MySQL（或本地 H2 演示）。

## 功能概要

- **教师/管理员**：题库（单选/多选/判断/填空/简答）、手动组卷、按题型随机智能组卷、发布考试、查看答卷列表、主观题批阅 API。
- **学生**：查看可参加考试、进入全屏答题页、自动保存答案、`visibility` 切屏上报与达上限自动交卷、交卷后查看分数。
- **管理员**：用户列表、审计日志（示例数据）。

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
# 如需允许任意来源跨域访问（公网开放）
export APP_CORS_ALLOW_ALL_ORIGINS=true
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
export APP_CORS_ALLOW_ALL_ORIGINS=true
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

当前版本为可运行 **MVP**：侧重教学流程与 UI 规范落地，未实现视频监考、Redis 分布式会话等；生产环境请替换 JWT 密钥、改用 HTTPS、加固异常信息、配置真实备份与等保策略。
