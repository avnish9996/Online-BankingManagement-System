-- Create database
CREATE DATABASE IF NOT EXISTS bankdb;
USE bankdb;

-- Users table (customers + admins)
CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  phone VARCHAR(20),
  role ENUM('CUSTOMER','ADMIN') DEFAULT 'CUSTOMER',
  password_hash VARCHAR(256) NOT NULL,
  salt VARCHAR(64) NOT NULL,
  status ENUM('PENDING','ACTIVE','FROZEN') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
  acc_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  acc_type VARCHAR(30) DEFAULT 'SAVINGS',
  balance DECIMAL(15,2) DEFAULT 0.00,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
  tx_id INT AUTO_INCREMENT PRIMARY KEY,
  acc_id INT NOT NULL,
  tx_type ENUM('DEPOSIT','WITHDRAW','TRANSFER') NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  tx_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  description VARCHAR(255),
  target_acc_id INT,
  FOREIGN KEY (acc_id) REFERENCES accounts(acc_id) ON DELETE CASCADE
);

-- Insert a sample admin (password: admin123) - replace salt/hash after generating or use the Java app to create admin.
-- For convenience, you can insert an admin manually after creating a hash with the Java program or temporarily set all registration-created users to ADMIN and log in.
