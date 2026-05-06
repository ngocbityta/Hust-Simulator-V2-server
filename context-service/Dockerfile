# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY context-service/build.gradle context-service/settings.gradle ./context-service/
COPY context-service/gradle ./context-service/gradle
COPY context-service/gradlew ./context-service/

# Copy source and proto
COPY context-service/src ./context-service/src
COPY proto ./proto

# Build
RUN cd context-service && gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built jar and proto (if needed for some runtimes, but usually Spring Boot bundles protos)
# However, keeping proto structure just in case of dynamic loading
COPY --from=build /app/context-service/build/libs/*.jar app.jar
COPY --from=build /app/proto ./proto

EXPOSE 8080
EXPOSE 50052

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
