CREATE DATABASE IF NOT EXISTS calc_data;
USE calc_data;

CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'STRONG_PASSWORD';

GRANT ALL PRIVILEGES ON calc_data.* TO 'app_user'@'%';

FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS calc_results (
                                            id INT AUTO_INCREMENT PRIMARY KEY,
                                            number1 DOUBLE NOT NULL,
                                            number2 DOUBLE NOT NULL,
                                            sum_result DOUBLE NOT NULL,
                                            product_result DOUBLE NOT NULL,
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);