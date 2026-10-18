FROM eclipse-temurin:21-jre

WORKDIR /app

RUN mkdir -p /app/data && chown -R 10001:10001 /app

COPY --chown=10001:10001 build/libs/shelf-project-1.0.0.jar /app/shelf.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/shelf.jar"]
