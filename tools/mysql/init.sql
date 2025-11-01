-- Create database if not exists
CREATE DATABASE IF NOT EXISTS ark CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the ark database
USE ark;

-- Optional: Create sample table (you can remove or modify this)
CREATE TABLE IF NOT EXISTS sample_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

