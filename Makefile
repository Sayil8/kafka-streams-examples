.PHONY: build-word-count # Builds Docker image
build-word-count:
	 ./gradlew :word-count:jibDockerBuild -Djib.to.image="smuehr/word-count:latest"

.PHONY: build-favourite-colour # Builds favourite-colour Docker image
build-build-favourite-colour:
	 ./gradlew :favourite-colour:jibDockerBuild -Djib.to.image="smuehr/favourite-colour:latest"

.PHONY: build-bank-balance-producer # Builds bank balance producer Docker image
build-bank-balance-producer:
	 ./gradlew :bank-balance-producer:jibDockerBuild -Djib.to.image="smuehr/bank-balance-producer:latest"

.PHONY: build-bank-balance # Builds bank balance Docker image
build-bank-balance:
	 ./gradlew :bank-balance:jibDockerBuild -Djib.to.image="smuehr/bank-balance:latest"