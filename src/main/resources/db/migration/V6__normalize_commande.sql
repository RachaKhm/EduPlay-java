-- Flyway migration: normalize commande schema
-- Adds canonical columns used by the application with safe defaults so INSERTS won't fail

ALTER TABLE commande
  ADD COLUMN IF NOT EXISTS total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS user_id INT NULL,
  ADD COLUMN IF NOT EXISTS is_paid TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Note: this migration simply ensures canonical columns exist with safe defaults.
-- If you want to migrate values from legacy columns (e.g. montant_total -> total_amount),
-- do that in a separate migration that can assume a specific MySQL version or run a small script.

