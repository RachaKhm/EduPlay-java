-- V9: Ensure `commande` table exists with canonical columns
-- Idempotent: creates the table only if missing and ensures recommended columns exist
CREATE TABLE IF NOT EXISTS `commande` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `product_id` INT NOT NULL,
  `parent_id` INT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `total_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `user_id` INT NOT NULL DEFAULT 0,
  `is_paid` TINYINT(1) NOT NULL DEFAULT 0,
  `status` VARCHAR(50) NOT NULL DEFAULT 'pending',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX idx_commande_product (`product_id`),
  INDEX idx_commande_parent (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Ensure parent_id column exists (safe no-op if already present)
ALTER TABLE `commande` ADD COLUMN IF NOT EXISTS `parent_id` INT;

