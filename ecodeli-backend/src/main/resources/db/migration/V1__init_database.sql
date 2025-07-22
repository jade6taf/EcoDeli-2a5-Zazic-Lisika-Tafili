-- EcoDeli Database Initialization Script for PostgreSQL
-- This script will be executed by Hibernate on Railway

-- Configure PostgreSQL settings
SET timezone = 'UTC';

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis" CASCADE;

-- The tables will be created automatically by Hibernate based on @Entity annotations
-- This file ensures proper PostgreSQL configuration
