-- V8: Add `picture` column to product table if missing
-- This migration is idempotent: it checks information_schema and alters only if needed.
DELIMITER //
CREATE PROCEDURE add_product_picture_if_missing()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'product'
          AND COLUMN_NAME = 'picture'
    ) THEN
        ALTER TABLE product ADD COLUMN picture VARCHAR(512);
    END IF;
END //
DELIMITER ;

CALL add_product_picture_if_missing();
DROP PROCEDURE IF EXISTS add_product_picture_if_missing;

