FROM gradle:jdk11-focal as builder

WORKDIR /app
COPY ./settings.gradle .
COPY ./build.gradle .
COPY ./gradlew .
COPY ./gradle ./gradle
COPY ./src ./src

RUN ./gradlew :bootJar

FROM eclipse-temurin:11-jdk-focal

ARG VERSION_TAG=unknown

RUN set -e; \
    apt-get update -y && apt-get install -y \
    gnupg2 \
    tini \
    lsb-release; \
    gcsFuseRepo=gcsfuse-`lsb_release -c -s`; \
    echo "deb http://packages.cloud.google.com/apt $gcsFuseRepo main" | \
    tee /etc/apt/sources.list.d/gcsfuse.list; \
    curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | \
    apt-key add -; \
    apt-get update; \
    apt-get install -y gcsfuse && apt-get clean

ENV VERSION=$VERSION_TAG
ENV CLOUD_PLATFORM=GCP

COPY --from=builder /app/build/libs/launchpad-*.jar /launchpad.jar

COPY start.sh ./start.sh
RUN chmod +x ./start.sh

ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["/start.sh"]
