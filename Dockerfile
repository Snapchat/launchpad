FROM gradle:jdk11-focal as builder

WORKDIR /app
COPY ./settings.gradle .
COPY ./build.gradle .
COPY ./gradlew .
COPY ./gradle ./gradle
COPY ./src ./src

RUN ./gradlew :bootJar

FROM eclipse-temurin:11-jdk-focal

RUN apt-get update
RUN apt-get install -y tini

ARG VERSION_TAG=unknown
ENV VERSION=$VERSION_TAG

COPY --from=builder /app/build/libs/launchpad.jar /code/launchpad.jar

WORKDIR /code

ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["java", "-jar", "launchpad.jar"]
