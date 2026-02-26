# Flink on Kubernetes Makefile

deploy:
.PHONY: kind-up kind-down prepare-dev build-jar build-docker load-image deploy helm-deploy port-forward scale upgrade savepoint

kind-up:
	./scripts/start-kind.sh

kind-down:
	./scripts/delete-kind.sh

prepare-dev:
	./scripts/prepare-dev-env.sh

build-jar:
	./scripts/build-jar.sh

build-docker:
	./scripts/build-docker.sh

load-image:
	./scripts/load-image-to-kind.sh $(IMAGE)

deploy:
	./scripts/deploy.sh

helm-deploy:
	helm upgrade --install flink ./helm/flink --wait

port-forward:
	./scripts/port-forward-ui.sh

scale:
	./scripts/scale-taskmanagers.sh $(COUNT)

upgrade:
	./scripts/upgrade-job.sh

savepoint:
	./scripts/trigger-savepoint.sh
create-joined-table:
	docker-compose -f docker/docker-compose.yml exec -T postgres psql -U postgres -d purchase_db -c "CREATE TABLE IF NOT EXISTS purchase_inventory_merged (id SERIAL PRIMARY KEY, transaction_time TIMESTAMP, transaction_id VARCHAR(64), product_id VARCHAR(32), price DOUBLE PRECISION, quantity INT, state VARCHAR(16), is_member BOOLEAN, member_discount DOUBLE PRECISION, add_supplements BOOLEAN, supplement_price DOUBLE PRECISION, total_purchase DOUBLE PRECISION, event_time TIMESTAMP, existing_level INT, stock_quantity INT, new_level INT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"
