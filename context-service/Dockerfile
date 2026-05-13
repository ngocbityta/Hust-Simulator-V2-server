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

# Extract stage
FROM eclipse-temurin:17-jre-alpine AS extract
WORKDIR /app
COPY --from=build /app/context-service/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --destination extracted

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Use a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=extract /app/extracted ./

EXPOSE 8080
EXPOSE 50052

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
