# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY social-service/build.gradle social-service/settings.gradle ./social-service/
COPY social-service/gradle ./social-service/gradle
COPY social-service/gradlew ./social-service/

# Copy source and proto
COPY social-service/src ./social-service/src
COPY proto ./proto

# Build
RUN cd social-service && gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built jar and proto (if needed for some runtimes, but usually Spring Boot bundles protos)
# However, keeping proto structure just in case of dynamic loading
COPY --from=build /app/social-service/build/libs/*.jar app.jar
COPY --from=build /app/proto ./proto

EXPOSE 8083
EXPOSE 50054

ENTRYPOINT ["java", "-jar", "app.jar"]
