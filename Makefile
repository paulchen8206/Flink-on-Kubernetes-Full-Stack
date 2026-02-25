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
