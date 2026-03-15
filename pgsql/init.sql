-- Unified DDL for Postgres initialization

-- Users and databases
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'root') THEN
        CREATE ROLE root LOGIN PASSWORD 'admin1';
    END IF;
END
$$;

SELECT 'CREATE DATABASE sales_report'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sales_report')\gexec

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

-- Purchase report table mirror in purchase_db (used by unified monitoring view)
CREATE TABLE IF NOT EXISTS purchase_report (
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
CREATE INDEX IF NOT EXISTS idx_timestamp_purchase_db ON purchase_report (dim_item, dim_category, dim_state, purchase_time);

-- One-stop view for quick pipeline monitoring in purchase_db
CREATE OR REPLACE VIEW pipeline_monitoring_view AS
SELECT
    'purchases'::text AS source_table,
    created_at AS event_time,
    id::text AS record_key,
    NULL::double precision AS metric_value,
    event_data AS details
FROM purchases
UNION ALL
SELECT
    'purchase_inventory_merged'::text AS source_table,
    COALESCE(transaction_time, created_at) AS event_time,
    (transaction_id || ':' || product_id) AS record_key,
    total_purchase AS metric_value,
    ('state=' || state || ', qty=' || quantity::text) AS details
FROM purchase_inventory_merged
UNION ALL
SELECT
    'purchase_report'::text AS source_table,
    purchase_time AS event_time,
    (dim_item || ':' || dim_state) AS record_key,
    fact_sum_total_purchase::double precision AS metric_value,
    ('category=' || dim_category || ', tx=' || fact_count_transactions::text) AS details
FROM purchase_report;

-- Purchase report table (in sales_report DB)
\connect sales_report

CREATE TABLE IF NOT EXISTS purchase_report (
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
CREATE INDEX IF NOT EXISTS idx_timestamp ON purchase_report (dim_item, dim_category, dim_state, purchase_time);
ALTER TABLE purchase_report OWNER TO root;
