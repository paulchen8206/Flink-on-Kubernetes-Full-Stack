#!/bin/bash
# Create the joined table in Postgres for merged results
set -e

docker-compose -f docker/docker-compose.yml exec -T postgres psql -U postgres -d purchase_db -c "CREATE TABLE IF NOT EXISTS purchase_inventory_merged (id SERIAL PRIMARY KEY, transaction_time TIMESTAMP, transaction_id VARCHAR(64), product_id VARCHAR(32), price DOUBLE PRECISION, quantity INT, state VARCHAR(16), is_member BOOLEAN, member_discount DOUBLE PRECISION, add_supplements BOOLEAN, supplement_price DOUBLE PRECISION, total_purchase DOUBLE PRECISION, event_time TIMESTAMP, existing_level INT, stock_quantity INT, new_level INT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"

echo "Joined table purchase_inventory_merged ensured in Postgres."
