-- V2: Ensure `user` table exists with InnoDB engine and correct charset
-- This migration creates the table if it's missing, using explicit backticks and ENGINE to
-- avoid "doesn't exist in engine" issues caused by missing/incorrect engine files.
CREATE TABLE IF NOT EXISTS `user` (
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
    `parent_id` INT,
    `last_login_ip` VARCHAR(45),
    `last_login_country` VARCHAR(100),
    `last_login_city` VARCHAR(100),
    `last_login_at` DATETIME,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_parent` FOREIGN KEY (`parent_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- If the table exists but uses a different engine (e.g. MyISAM), attempt to convert it to InnoDB
-- This helps when older or corrupted table files cause the "doesn't exist in engine" error.
ALTER TABLE `user` ENGINE = InnoDB;

