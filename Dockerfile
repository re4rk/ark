# Build stage
FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /app

# Copy gradle wrapper and config files first for better caching
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradlew ./
RUN chmod +x gradlew

# Copy all build.gradle.kts files for dependency resolution
COPY core/core-api/build.gradle.kts ./core/core-api/
COPY core/core-enum/build.gradle.kts ./core/core-enum/
COPY clients/client-example/build.gradle.kts ./clients/client-example/
COPY clients/client-s3/build.gradle.kts ./clients/client-s3/
COPY storage/db-core/build.gradle.kts ./storage/db-core/
COPY support/logging/build.gradle.kts ./support/logging/
COPY support/monitoring/build.gradle.kts ./support/monitoring/
COPY tests/api-docs/build.gradle.kts ./tests/api-docs/

# Download dependencies (this layer will be cached if build files don't change)
RUN ./gradlew :core:core-api:dependencies --no-daemon || true

# Copy source code
COPY . .

# Build application
RUN ./gradlew :core:core-api:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build --chown=spring:spring /app/core/core-api/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

