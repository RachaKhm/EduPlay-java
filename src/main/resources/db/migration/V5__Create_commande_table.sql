-- V5: Create commande (order) table
CREATE TABLE IF NOT EXISTS commande (
  id INT NOT NULL AUTO_INCREMENT,
  product_id INT NOT NULL,
  parent_id INT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(50) NOT NULL DEFAULT 'pending',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

