-- V1: Création de la table utilisateur (Base)
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    type ENUM('admin', 'enseignant', 'parent', 'enfant') NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    birth_date DATE,
    telephone VARCHAR(20),
    adresse TEXT,
    specialite VARCHAR(100),
    niveau VARCHAR(50),
    profile_picture VARCHAR(255),
    parent_id INT,
    last_login_ip VARCHAR(45),
    last_login_country VARCHAR(100),
    last_login_city VARCHAR(100),
    last_login_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES user(id) ON DELETE SET NULL
);

-- V1.1: Création de la table événement (Exemple)
CREATE TABLE IF NOT EXISTS school_event (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    location VARCHAR(255),
    max_capacity INT,
    image_path VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
