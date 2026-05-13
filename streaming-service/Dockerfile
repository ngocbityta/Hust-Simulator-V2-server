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

# Extract stage
FROM eclipse-temurin:17-jre-alpine AS extract
WORKDIR /app
COPY --from=build /app/streaming-service/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --destination extracted

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Use a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=extract /app/extracted ./

EXPOSE 8084

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
