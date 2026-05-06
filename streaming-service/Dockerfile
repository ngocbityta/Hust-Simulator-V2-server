# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY streaming-service/build.gradle streaming-service/settings.gradle* ./streaming-service/
COPY streaming-service/gradle ./streaming-service/gradle/
COPY streaming-service/gradlew ./streaming-service/

# Copy source
COPY streaming-service/src ./streaming-service/src

# Build
RUN cd streaming-service && gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/streaming-service/build/libs/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
