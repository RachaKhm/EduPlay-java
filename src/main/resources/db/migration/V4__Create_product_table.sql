-- V4: Create product table for marketplace
CREATE TABLE IF NOT EXISTS product (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  description TEXT,
  availability TINYINT(1) NOT NULL DEFAULT 1,
  picture VARCHAR(512),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

