# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY auth-service/build.gradle auth-service/settings.gradle ./auth-service/
COPY auth-service/gradle ./auth-service/gradle
COPY auth-service/gradlew ./auth-service/

# Copy source
COPY auth-service/src ./auth-service/src

# Build
RUN cd auth-service && gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/auth-service/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
