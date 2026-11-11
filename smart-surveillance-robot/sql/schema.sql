-- MySQL 8 schema for the Smart Public Surveillance Robot backend.
-- The application also creates/updates these tables automatically (ddl-auto=update);
-- this file is provided for manual setup and for your project documentation.

CREATE DATABASE IF NOT EXISTS surveillance_robot
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE surveillance_robot;

CREATE TABLE IF NOT EXISTS app_users (
  id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username       VARCHAR(50)  NOT NULL UNIQUE,
  password_hash  VARCHAR(100) NOT NULL,
  role           VARCHAR(20)  NOT NULL,          -- ADMIN | OPERATOR
  enabled        BIT          NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS robots (
  id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  robot_code    VARCHAR(40)  NOT NULL UNIQUE,
  name          VARCHAR(100) NOT NULL,
  status        VARCHAR(20)  NOT NULL,           -- ONLINE | OFFLINE | ALERT
  latitude      DOUBLE NULL,
  longitude     DOUBLE NULL,
  last_seen_at  DATETIME(6)  NULL,
  created_at    DATETIME(6)  NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sensor_readings (
  id               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  robot_id         BIGINT NOT NULL,
  ultrasonic_cm    DOUBLE NULL,                  -- NULL = out of range
  ir_triggered     BIT    NOT NULL,
  sound_triggered  BIT    NOT NULL,
  recorded_at      DATETIME(6) NOT NULL,
  CONSTRAINT fk_sensor_robot FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE,
  INDEX idx_sensor_robot_time (robot_id, recorded_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detection_events (
  id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  robot_id      BIGINT       NOT NULL,
  object_class  VARCHAR(100) NOT NULL,
  confidence    DOUBLE       NOT NULL,
  latitude      DOUBLE NULL,
  longitude     DOUBLE NULL,
  image_url     VARCHAR(500) NULL,
  alert_sent    BIT          NOT NULL,
  detected_at   DATETIME(6)  NOT NULL,
  CONSTRAINT fk_detection_robot FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE,
  INDEX idx_detection_robot_time (robot_id, detected_at),
  INDEX idx_detection_time (detected_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movement_commands (
  id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  robot_id      BIGINT      NOT NULL,
  command       VARCHAR(20) NOT NULL,            -- FORWARD | REVERSE | LEFT | RIGHT | STOP
  status        VARCHAR(20) NOT NULL,            -- PENDING | DELIVERED
  issued_by     VARCHAR(50) NOT NULL,
  created_at    DATETIME(6) NOT NULL,
  delivered_at  DATETIME(6) NULL,
  CONSTRAINT fk_command_robot FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE,
  INDEX idx_command_robot_status (robot_id, status, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alerts (
  id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  robot_id         BIGINT       NOT NULL,
  type             VARCHAR(30)  NOT NULL,        -- DETECTION | LOW_DISTANCE | ROBOT_OFFLINE
  message          VARCHAR(500) NOT NULL,
  delivered        BIT          NOT NULL,        -- sent to Telegram
  acknowledged     BIT          NOT NULL,
  created_at       DATETIME(6)  NOT NULL,
  acknowledged_at  DATETIME(6)  NULL,
  acknowledged_by  VARCHAR(50)  NULL,
  CONSTRAINT fk_alert_robot FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE,
  INDEX idx_alert_robot_type_time (robot_id, type, created_at),
  INDEX idx_alert_ack (acknowledged, created_at)
) ENGINE=InnoDB;
