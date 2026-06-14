#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 /path/to/exam_platform_YYYYMMDD_HHMMSS.sql.gz" >&2
  exit 1
fi

BACKUP_FILE="$1"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-exam_platform}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"

if [[ ! -f "$BACKUP_FILE" ]]; then
  echo "Backup file not found: $BACKUP_FILE" >&2
  exit 1
fi

echo "Restoring $BACKUP_FILE into $DB_NAME on $DB_HOST:$DB_PORT"
read -r -p "This will overwrite data in ${DB_NAME}. Type RESTORE to continue: " confirm
if [[ "$confirm" != "RESTORE" ]]; then
  echo "Canceled."
  exit 1
fi

if [[ "$BACKUP_FILE" == *.gz ]]; then
  gzip -dc "$BACKUP_FILE" | MYSQL_PWD="$DB_PASSWORD" mysql \
    --host="$DB_HOST" \
    --port="$DB_PORT" \
    --user="$DB_USERNAME" \
    --default-character-set=utf8mb4 \
    "$DB_NAME"
else
  MYSQL_PWD="$DB_PASSWORD" mysql \
    --host="$DB_HOST" \
    --port="$DB_PORT" \
    --user="$DB_USERNAME" \
    --default-character-set=utf8mb4 \
    "$DB_NAME" < "$BACKUP_FILE"
fi

echo "Restore complete."
