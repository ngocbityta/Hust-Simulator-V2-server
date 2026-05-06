# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app

COPY messaging-service/build.gradle messaging-service/settings.gradle ./messaging-service/
COPY messaging-service/gradle ./messaging-service/gradle
COPY messaging-service/gradlew ./messaging-service/

COPY messaging-service/src ./messaging-service/src

RUN cd messaging-service && gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/messaging-service/build/libs/*.jar app.jar

EXPOSE 8082
EXPOSE 9092

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
