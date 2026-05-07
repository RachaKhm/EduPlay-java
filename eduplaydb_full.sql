-- Full eduplaydb schema inferred from project code
-- Import in phpMyAdmin or via CLI: mysql -u root -p < eduplaydb_full.sql

CREATE DATABASE IF NOT EXISTS `eduplaydb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `eduplaydb`;

-- user, school_event, product, commande, event_registration already in eduplaydb.sql
-- Additional tables inferred from project services

-- -----------------------------------------------------------------
-- table: book_request
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `book_request`;
CREATE TABLE `book_request` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  book_title VARCHAR(255) NOT NULL,
  enfant_id INT NOT NULL,
  requested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_available TINYINT(1) DEFAULT 0,
  is_notified TINYINT(1) DEFAULT 0,
  resource_id INT DEFAULT NULL,
  INDEX idx_enfant (enfant_id),
  INDEX idx_resource (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: event_resource
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `event_resource`;
CREATE TABLE `event_resource` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(100),
  title VARCHAR(255),
  context TEXT,
  file_path VARCHAR(1024),
  url VARCHAR(1024),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  event_id INT,
  INDEX idx_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: subscription
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `subscription`;
CREATE TABLE `subscription` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  parent_id INT NOT NULL,
  kid_id INT NOT NULL,
  course_id INT NOT NULL,
  subscribed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  active TINYINT(1) DEFAULT 1,
  INDEX idx_parent (parent_id),
  INDEX idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: seance
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `seance`;
CREATE TABLE `seance` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  start_time DATETIME,
  end_time DATETIME,
  course_id INT,
  title VARCHAR(255),
  date DATE,
  location VARCHAR(255),
  status VARCHAR(50),
  description TEXT,
  INDEX idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: resource
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  library_id INT,
  title VARCHAR(255),
  author VARCHAR(255),
  summary TEXT,
  cover_image VARCHAR(1024),
  pdf_file VARCHAR(1024),
  type VARCHAR(50),
  min_age INT,
  max_age INT,
  language VARCHAR(50),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_library (library_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: library
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `library`;
CREATE TABLE `library` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  cover_image VARCHAR(1024),
  min_age INT,
  max_age INT,
  level VARCHAR(100),
  theme VARCHAR(100),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: course
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  duration_training VARCHAR(100),
  description TEXT,
  level VARCHAR(100),
  pdf_file VARCHAR(1024),
  status VARCHAR(50) DEFAULT 'draft',
  teacher_id INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: course_reviews
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `course_reviews`;
CREATE TABLE `course_reviews` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  user_id INT NOT NULL,
  rating INT,
  comment TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_course (course_id),
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Optional: other auxiliary tables can be added similarly

-- End of full inferred schema

