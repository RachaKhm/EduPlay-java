-- V3: Ajout robuste des colonnes manquantes
-- On utilise une procédure pour vérifier l'existence avant d'ajouter

DELIMITER //

CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(64),
    IN columnName VARCHAR(64),
    IN columnDef TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = tableName 
        AND COLUMN_NAME = columnName
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', columnName, ' ', columnDef);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

-- Application pour school_event
CALL AddColumnIfNotExists('school_event', 'latitude', 'VARCHAR(100) AFTER image_path');
CALL AddColumnIfNotExists('school_event', 'longitude', 'VARCHAR(100) AFTER latitude');
CALL AddColumnIfNotExists('school_event', 'max_capacity', 'INT DEFAULT 0 AFTER longitude');
CALL AddColumnIfNotExists('school_event', 'current_registrations', 'INT DEFAULT 0 AFTER max_capacity');

-- Application pour user
CALL AddColumnIfNotExists('user', 'profile_picture', 'VARCHAR(255) AFTER parent_id');

DROP PROCEDURE AddColumnIfNotExists;
