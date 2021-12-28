.PHONY: build # Builds Docker image
build:
	 ./gradlew :word-count:jibDockerBuild -Djib.to.image="smuehr/word-count:latest"
