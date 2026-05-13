# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY social-service/build.gradle social-service/settings.gradle ./social-service/
COPY social-service/gradle ./social-service/gradle
COPY social-service/gradlew ./social-service/

# Copy source
COPY social-service/src ./social-service/src

# Build
RUN cd social-service && gradle bootJar --no-daemon

# Extract stage
FROM eclipse-temurin:17-jre-alpine AS extract
WORKDIR /app
COPY --from=build /app/social-service/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --destination extracted

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Use a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=extract /app/extracted ./

EXPOSE 8083

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
