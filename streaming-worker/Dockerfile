# Build:  docker build -f streaming-worker/Dockerfile -t streaming-worker .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY streaming-common streaming-common
COPY streaming-api/pom.xml streaming-api/pom.xml
COPY streaming-worker streaming-worker
RUN mvn -q -pl streaming-worker -am package -DskipTests

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends ffmpeg && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/streaming-worker/target/streaming-worker-*.jar /app/worker.jar
ENV MEDIA_ROOT=/data/media FFMPEG_PATH=/usr/bin/ffmpeg
ENTRYPOINT ["java", "-jar", "/app/worker.jar"]
