FROM eclipse-temurin:21-jdk AS build

WORKDIR /build
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src/main ./src/main
RUN ./gradlew --no-daemon --max-workers=2 bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN mkdir -p /app/data && chown -R 10001:10001 /app

COPY --from=build --chown=10001:10001 /build/build/libs/shelf-project-1.0.0.jar /app/shelf.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/shelf.jar"]
