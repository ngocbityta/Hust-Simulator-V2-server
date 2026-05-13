# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY messaging-service/build.gradle messaging-service/settings.gradle ./messaging-service/
COPY messaging-service/gradle ./messaging-service/gradle
COPY messaging-service/gradlew ./messaging-service/

# Copy source
COPY messaging-service/src ./messaging-service/src

# Build
RUN cd messaging-service && gradle bootJar --no-daemon

# Extract stage
FROM eclipse-temurin:17-jre-alpine AS extract
WORKDIR /app
COPY --from=build /app/messaging-service/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --destination extracted

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Use a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=extract /app/extracted ./

EXPOSE 8082
EXPOSE 9092

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
