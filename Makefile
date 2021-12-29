.PHONY: build-word-count # Builds Docker image
build-word-count:
	 ./gradlew :word-count:jibDockerBuild -Djib.to.image="smuehr/word-count:latest"

.PHONY: build-favourite-colour # Builds favourite-colour Docker image
build-build-favourite-colour:
	 ./gradlew :favourite-colour:jibDockerBuild -Djib.to.image="smuehr/favourite-colour:latest"
