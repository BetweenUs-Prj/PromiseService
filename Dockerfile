# Multi-stage build for optimal image size
FROM gradle:8.5-jdk17 AS build

# Set working directory
WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
COPY gradlew.bat ./

# Download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew bootJar --no-daemon

# Production stage
FROM openjdk:17-jre-slim

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]