-- EcoDeli Database Initialization Script
-- This script runs when the MariaDB container starts for the first time

-- Use the database
USE EcoDeli;

-- Set timezone
SET time_zone = '+00:00';

-- Configure MariaDB settings for better performance
SET GLOBAL innodb_buffer_pool_size = 268435456; -- 256MB
SET GLOBAL query_cache_size = 33554432; -- 32MB
SET GLOBAL query_cache_type = 1;

FLUSH PRIVILEGES;