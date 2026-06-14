#!/usr/bin/env bash
set -euo pipefail

# One-click deployment for Linux server (Ubuntu/CentOS compatible).
# What it does:
# 1) Build frontend and publish static files to /var/www/exam-platform
# 2) Initialize MySQL database/user by backend script
# 3) Install/refresh systemd service for backend
# 4) Install/refresh nginx config and reload nginx

PROJECT_DIR="${PROJECT_DIR:-/opt/exam-platform}"
DOMAIN="${DOMAIN:-_}"
BACKEND_PORT="${BACKEND_PORT:-8080}"
WEB_ROOT="${WEB_ROOT:-/var/www/exam-platform}"
SERVICE_NAME="${SERVICE_NAME:-exam-platform}"
NGINX_SITE_NAME="${NGINX_SITE_NAME:-exam-platform}"

# Database config (must match backend env)
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-exam_platform}"
DB_USERNAME="${DB_USERNAME:-exam_user}"
DB_PASSWORD="${DB_PASSWORD:-change_me}"

# App config
APP_JWT_SECRET="${APP_JWT_SECRET:-}"
APP_CORS_ALLOW_ALL_ORIGINS="${APP_CORS_ALLOW_ALL_ORIGINS:-false}"
APP_SEED_DEMO_DATA_ENABLED="${APP_SEED_DEMO_DATA_ENABLED:-false}"
APP_PUBLIC_REGISTRATION_ENABLED="${APP_PUBLIC_REGISTRATION_ENABLED:-true}"
APP_DEMO_PASSWORD_RESET_ENABLED="${APP_DEMO_PASSWORD_RESET_ENABLED:-false}"
APP_BOOTSTRAP_ADMIN_USERNAME="${APP_BOOTSTRAP_ADMIN_USERNAME:-}"
APP_BOOTSTRAP_ADMIN_PASSWORD="${APP_BOOTSTRAP_ADMIN_PASSWORD:-}"
APP_AGENT_ENABLED="${APP_AGENT_ENABLED:-true}"
APP_AGENT_API_KEY="${APP_AGENT_API_KEY:-}"
APP_AGENT_BASE_URL="${APP_AGENT_BASE_URL:-https://api.openai.com/v1/chat/completions}"
APP_AGENT_MODEL="${APP_AGENT_MODEL:-gpt-4o-mini}"
APP_NOTIFY_SMS_WEBHOOK_URL="${APP_NOTIFY_SMS_WEBHOOK_URL:-}"
SPRING_MAIL_HOST="${SPRING_MAIL_HOST:-}"
SPRING_MAIL_PORT="${SPRING_MAIL_PORT:-}"
SPRING_MAIL_USERNAME="${SPRING_MAIL_USERNAME:-}"
SPRING_MAIL_PASSWORD="${SPRING_MAIL_PASSWORD:-}"

MYSQL_ROOT_USER="${MYSQL_ROOT_USER:-root}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-}"

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Please run as root: sudo bash scripts/deploy-server.sh"
  exit 1
fi

if [[ ! -d "$PROJECT_DIR/backend" || ! -d "$PROJECT_DIR/frontend" ]]; then
  echo "PROJECT_DIR is invalid: $PROJECT_DIR"
  exit 1
fi

if [[ -z "$MYSQL_ROOT_PASSWORD" ]]; then
  echo "MYSQL_ROOT_PASSWORD is empty. Set it before running."
  echo "Example:"
  echo "  export MYSQL_ROOT_PASSWORD='your_mysql_root_password'"
  exit 1
fi

if [[ ${#APP_JWT_SECRET} -lt 32 ]]; then
  echo "APP_JWT_SECRET must be set and at least 32 bytes before running." >&2
  echo "Example:"
  echo "  export APP_JWT_SECRET='replace_with_a_random_secret_min_32_bytes'"
  exit 1
fi

validate_mysql_identifier() {
  local name="$1"
  local value="$2"
  if [[ ! "$value" =~ ^[A-Za-z0-9_]+$ ]]; then
    echo "$name may only contain letters, numbers, and underscores: $value" >&2
    exit 1
  fi
}

mysql_quote_string() {
  local value="$1"
  value="${value//\\/\\\\}"
  value="${value//\'/\\\'}"
  printf "%s" "$value"
}

validate_mysql_identifier "DB_NAME" "$DB_NAME"
validate_mysql_identifier "DB_USERNAME" "$DB_USERNAME"

# Spring Boot 需要 JDK 17；Maven 会跟随 JAVA_HOME，避免落到系统默认 JDK 11
JAVA_HOME_RESOLVED="${JAVA_HOME:-}"
if [[ -z "$JAVA_HOME_RESOLVED" ]]; then
  for d in /usr/lib/jvm/java-17-openjdk* /usr/lib/jvm/java-17*; do
    if [[ -d "$d" && -x "$d/bin/java" ]]; then
      JAVA_HOME_RESOLVED="$d"
      break
    fi
  done
fi
if [[ -z "$JAVA_HOME_RESOLVED" || ! -x "$JAVA_HOME_RESOLVED/bin/java" ]]; then
  echo "未检测到 JDK 17。请先安装，例如：sudo yum install -y java-17-openjdk-devel" >&2
  exit 1
fi
echo "Using JAVA_HOME=$JAVA_HOME_RESOLVED"
"$JAVA_HOME_RESOLVED/bin/java" -version

echo "[1/6] Building frontend..."
cd "$PROJECT_DIR/frontend"
npm install
npm run build

echo "[2/6] Publishing frontend to $WEB_ROOT ..."
mkdir -p "$WEB_ROOT"
cp -r "$PROJECT_DIR/frontend/dist/"* "$WEB_ROOT/"

echo "[3/7] Initializing database..."
DB_PASSWORD_SQL="$(mysql_quote_string "$DB_PASSWORD")"
mysql -u"$MYSQL_ROOT_USER" -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'%' IDENTIFIED BY '${DB_PASSWORD_SQL}';
ALTER USER '${DB_USERNAME}'@'%' IDENTIFIED BY '${DB_PASSWORD_SQL}';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USERNAME}'@'%';
FLUSH PRIVILEGES;
SQL

echo "[4/7] Building backend JAR (JDK 17 + Maven)..."
export JAVA_HOME="${JAVA_HOME_RESOLVED}"
export PATH="${JAVA_HOME_RESOLVED}/bin:${PATH}"
cd "$PROJECT_DIR/backend"
mvn -q -DskipTests package
JAR_FILE="${PROJECT_DIR}/backend/target/exam-platform-1.0.0.jar"
if [[ ! -f "$JAR_FILE" ]]; then
  echo "Backend JAR not found: $JAR_FILE" >&2
  exit 1
fi

echo "[5/7] Writing systemd service..."
cat > "/etc/systemd/system/${SERVICE_NAME}.service" <<EOF
[Unit]
Description=Exam Platform Backend
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=${PROJECT_DIR}/backend
Environment=JAVA_HOME=${JAVA_HOME_RESOLVED}
Environment=PATH=${JAVA_HOME_RESOLVED}/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin
Environment=SPRING_PROFILES_ACTIVE=mysql
Environment=DB_HOST=${DB_HOST}
Environment=DB_PORT=${DB_PORT}
Environment=DB_NAME=${DB_NAME}
Environment=DB_USERNAME=${DB_USERNAME}
Environment=DB_PASSWORD=${DB_PASSWORD}
Environment=APP_JWT_SECRET=${APP_JWT_SECRET}
Environment=APP_CORS_ALLOW_ALL_ORIGINS=${APP_CORS_ALLOW_ALL_ORIGINS}
Environment=APP_SEED_DEMO_DATA_ENABLED=${APP_SEED_DEMO_DATA_ENABLED}
Environment=APP_PUBLIC_REGISTRATION_ENABLED=${APP_PUBLIC_REGISTRATION_ENABLED}
Environment=APP_DEMO_PASSWORD_RESET_ENABLED=${APP_DEMO_PASSWORD_RESET_ENABLED}
Environment=APP_BOOTSTRAP_ADMIN_USERNAME=${APP_BOOTSTRAP_ADMIN_USERNAME}
Environment=APP_BOOTSTRAP_ADMIN_PASSWORD=${APP_BOOTSTRAP_ADMIN_PASSWORD}
Environment=APP_AGENT_ENABLED=${APP_AGENT_ENABLED}
Environment=APP_AGENT_API_KEY=${APP_AGENT_API_KEY}
Environment=APP_AGENT_BASE_URL=${APP_AGENT_BASE_URL}
Environment=APP_AGENT_MODEL=${APP_AGENT_MODEL}
Environment=APP_NOTIFY_SMS_WEBHOOK_URL=${APP_NOTIFY_SMS_WEBHOOK_URL}
Environment=SPRING_MAIL_HOST=${SPRING_MAIL_HOST}
Environment=SPRING_MAIL_PORT=${SPRING_MAIL_PORT}
Environment=SPRING_MAIL_USERNAME=${SPRING_MAIL_USERNAME}
Environment=SPRING_MAIL_PASSWORD=${SPRING_MAIL_PASSWORD}
ExecStart=${JAVA_HOME_RESOLVED}/bin/java -jar ${JAR_FILE}
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

echo "[6/7] Writing nginx site config..."
mkdir -p /etc/nginx/conf.d
cat > "/etc/nginx/conf.d/${NGINX_SITE_NAME}.conf" <<EOF
server {
    listen 80;
    server_name ${DOMAIN};

    root ${WEB_ROOT};
    index index.html;

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:${BACKEND_PORT}/api/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }
}
EOF

nginx -t

echo "[7/7] Restarting services..."
systemctl daemon-reload
systemctl enable --now "${SERVICE_NAME}"
systemctl restart "${SERVICE_NAME}"
systemctl reload nginx

echo "Done."
echo "Visit: http://${DOMAIN}"
echo "Service status:"
systemctl --no-pager --full status "${SERVICE_NAME}" | sed -n '1,15p'
