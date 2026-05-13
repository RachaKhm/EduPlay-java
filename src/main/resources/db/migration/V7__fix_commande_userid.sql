-- Flyway migration: ensure user_id has a safe default to avoid strict-mode insert failures

-- If user_id exists, set a default of 0 and make not null (safe fallback). If it doesn't exist, add it.
ALTER TABLE commande
  ADD COLUMN IF NOT EXISTS user_id INT DEFAULT 0;

-- Ensure column has a default and is NOT NULL to satisfy strict SQL modes.
ALTER TABLE commande
  MODIFY COLUMN user_id INT NOT NULL DEFAULT 0;

-- Also ensure total_amount exists with safe default (idempotent)
ALTER TABLE commande
  ADD COLUMN IF NOT EXISTS total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE commande
  MODIFY COLUMN total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00;

-- Ensure is_paid has a safe default
ALTER TABLE commande
  ADD COLUMN IF NOT EXISTS is_paid TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE commande
  MODIFY COLUMN is_paid TINYINT(1) NOT NULL DEFAULT 0;

-- created_at
ALTER TABLE commande
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE commande
  MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

