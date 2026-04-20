-- Exam Platform MySQL initialization script (server deployment)
-- Usage:
--   mysql -uroot -p < create_database.sql
-- Or with custom app user:
--   mysql -uroot -p -e "SOURCE /path/to/create_database.sql"

CREATE DATABASE IF NOT EXISTS exam_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'exam_user'@'%' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON exam_platform.* TO 'exam_user'@'%';
FLUSH PRIVILEGES;
