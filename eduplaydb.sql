-- eduplaydb SQL dump generated from project migrations
-- Import this file into phpMyAdmin (or mysql CLI) to create the schema used by the application.
-- Usage (phpMyAdmin): choose Import -> upload this file -> Go
-- Usage (CLI): mysql -u root -p < eduplaydb.sql

CREATE DATABASE IF NOT EXISTS `eduplaydb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `eduplaydb`;

-- -----------------------------------------------------------------
-- table: user
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `first_name` VARCHAR(50) NOT NULL,
  `last_name` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) UNIQUE NOT NULL,
  `username` VARCHAR(50) UNIQUE,
  `password` VARCHAR(255),
  `type` ENUM('admin','enseignant','parent','enfant') NOT NULL,
  `active` BOOLEAN DEFAULT TRUE,
  `birth_date` DATE,
  `telephone` VARCHAR(20),
  `adresse` TEXT,
  `specialite` VARCHAR(100),
  `niveau` VARCHAR(50),
  `profile_picture` VARCHAR(255),
  `parent_id` INT DEFAULT NULL,
  `last_login_ip` VARCHAR(45),
  `last_login_country` VARCHAR(100),
  `last_login_city` VARCHAR(100),
  `last_login_at` DATETIME,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  -- additional columns used by the application
  `reset_token` VARCHAR(255),
  `reset_token_expiry` DATETIME,
  `session_token` VARCHAR(255),
  `session_expiry` DATETIME,
  `login_attempts` INT DEFAULT 0,
  `locked_until` DATETIME,
  `otp_code` VARCHAR(20),
  `otp_expiry` DATETIME,
  `facial_embedding` TEXT,
  `profile_picture_url` VARCHAR(512),
  CONSTRAINT `fk_parent` FOREIGN KEY (`parent_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: school_event
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `school_event`;
CREATE TABLE `school_event` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  start_date DATETIME NOT NULL,
  end_date DATETIME NOT NULL,
  location VARCHAR(255),
  image_path VARCHAR(255),
  latitude VARCHAR(100),
  longitude VARCHAR(100),
  max_capacity INT DEFAULT 0,
  current_registrations INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: product
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  description TEXT,
  availability TINYINT(1) NOT NULL DEFAULT 1,
  picture VARCHAR(512),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------
-- table: commande (orders)
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `commande`;
CREATE TABLE `commande` (
  id INT NOT NULL AUTO_INCREMENT,
  product_id INT NOT NULL,
  parent_id INT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  total_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  user_id INT NOT NULL DEFAULT 0,
  is_paid TINYINT(1) NOT NULL DEFAULT 0,
  status VARCHAR(50) NOT NULL DEFAULT 'pending',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_product (product_id),
  INDEX idx_parent (parent_id),
  CONSTRAINT fk_commande_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
  CONSTRAINT fk_commande_parent FOREIGN KEY (parent_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Optional: ensure engines (safe no-op if already InnoDB)
ALTER TABLE `user` ENGINE=InnoDB;
ALTER TABLE `product` ENGINE=InnoDB;
ALTER TABLE `commande` ENGINE=InnoDB;
ALTER TABLE `school_event` ENGINE=InnoDB;

-- End of dump

