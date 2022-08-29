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
RUN apt-get install -y tini s3fs

ENV CLOUD_PLATFORM=AWS

COPY --from=builder /app/build/libs/capi-launchpad-*.jar /capi-launchpad.jar

COPY start.sh ./start.sh
RUN chmod +x ./start.sh

ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["/start.sh"]
