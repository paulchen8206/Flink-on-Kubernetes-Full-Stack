-- Unified DDL for Postgres initialization

-- Users and databases
CREATE USER root WITH PASSWORD 'admin1';
CREATE DATABASE sales_report;
GRANT ALL PRIVILEGES ON DATABASE sales_report TO root;

-- Purchases table
CREATE TABLE IF NOT EXISTS purchases (
    id SERIAL PRIMARY KEY,
    event_data TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Purchase inventory merged table
CREATE TABLE IF NOT EXISTS purchase_inventory_merged (
    id SERIAL PRIMARY KEY,
    transaction_time TIMESTAMP,
    transaction_id VARCHAR(64),
    product_id VARCHAR(32),
    price DOUBLE PRECISION,
    quantity INT,
    state VARCHAR(16),
    is_member BOOLEAN,
    member_discount DOUBLE PRECISION,
    add_supplements BOOLEAN,
    supplement_price DOUBLE PRECISION,
    total_purchase DOUBLE PRECISION,
    event_time TIMESTAMP,
    existing_level INT,
    stock_quantity INT,
    new_level INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Purchase report table (in sales_report DB)
-- Note: The following commands must be run after switching to sales_report
-- \c sales_report;
CREATE TABLE purchase_report (
    dim_item VARCHAR(255),
    dim_category VARCHAR(255),
    dim_state VARCHAR(255),
    purchase_time TIMESTAMP,
    fact_count_transactions FLOAT,
    fact_sum_quantity FLOAT,
    fact_sum_price FLOAT,
    fact_sum_member_discount FLOAT,
    fact_sum_supplement_price FLOAT,
    fact_sum_total_purchase FLOAT,
    fact_avg_total_purchase FLOAT,
    PRIMARY KEY(dim_item, dim_category, dim_state, purchase_time)
);
CREATE INDEX idx_timestamp ON purchase_report (dim_item, dim_category, dim_state, purchase_time);
ALTER TABLE purchase_report OWNER TO root;
