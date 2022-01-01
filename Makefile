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

.PHONY: build-user-data-producer # Builds user data producer Docker image
build-user-data-producer:
	 ./gradlew :user-data-producer:jibDockerBuild -Djib.to.image="smuehr/user-data-producer:latest"

.PHONY: build-user-event-enricher # Builds user event enricher Docker image
build-user-event-enricher:
	 ./gradlew :user-event-enricher:jibDockerBuild -Djib.to.image="smuehr/user-event-enricher:latest"