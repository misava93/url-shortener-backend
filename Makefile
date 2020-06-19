# constants
APP = skillshare-url-shortener
DOCKER_TARGET = url-shortener-service

.PHONY: build-app
build-app:
	@echo "Building application"
	./gradlew build

.PHONY: docker-image-build
docker-image-build: Dockerfile build-app
	@echo "Building Docker image with containerized URL Shortener Service"
	docker build --target ${DOCKER_TARGET} -t ${APP}/${DOCKER_TARGET} .

.PHONY: docker-image-run
docker-image-run: docker-image-build
	@echo "Creating network to allow communication between client and server containers"
	-docker network create skillshare-net
	@echo "Cleaning containers"
	docker container prune -f
	@echo "Running Docker container with containerized URL Shortener Service"
	docker run --network skillshare-net -it --publish 8080:8080 --rm --name ${DOCKER_TARGET} ${APP}/${DOCKER_TARGET}

