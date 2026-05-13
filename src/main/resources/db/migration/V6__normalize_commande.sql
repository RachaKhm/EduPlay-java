-- Flyway migration: normalize commande schema
-- Uses a stored procedure to safely add columns if they don't exist (MySQL 8.0/8.4 compatibility)

DROP PROCEDURE IF EXISTS AddColumnIfNotNull;

DELIMITER //

CREATE PROCEDURE AddColumnIfNotNull()
BEGIN
    -- Add total_amount
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'commande' 
        AND COLUMN_NAME = 'total_amount'
    ) THEN
        ALTER TABLE commande ADD COLUMN total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00;
    END IF;

    -- Add user_id
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'commande' 
        AND COLUMN_NAME = 'user_id'
    ) THEN
        ALTER TABLE commande ADD COLUMN user_id INT NULL;
    END IF;

    -- Add is_paid
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'commande' 
        AND COLUMN_NAME = 'is_paid'
    ) THEN
        ALTER TABLE commande ADD COLUMN is_paid TINYINT(1) NOT NULL DEFAULT 0;
    END IF;

    -- Add created_at
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'commande' 
        AND COLUMN_NAME = 'created_at'
    ) THEN
        ALTER TABLE commande ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END //

DELIMITER ;

CALL AddColumnIfNotNull();
DROP PROCEDURE AddColumnIfNotNull;
