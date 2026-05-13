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

# Extract stage
FROM eclipse-temurin:17-jre-alpine AS extract
WORKDIR /app
COPY --from=build /app/auth-service/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --destination extracted

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Use a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=extract /app/extracted ./

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
