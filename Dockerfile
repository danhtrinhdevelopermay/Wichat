# Multi-stage build for Kotlin Ktor application
FROM gradle:8.5-jdk17 AS build

# Set working directory
WORKDIR /home/gradle/src

# Copy Gradle files first for caching
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts gradle.properties ./
COPY --chown=gradle:gradle gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || return 0

# Copy source code
COPY --chown=gradle:gradle src ./src

# Build fat JAR
RUN gradle jar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install curl for healthcheck
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /home/gradle/src/build/libs/*.jar /app/application.jar

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port (Render uses PORT env variable)
EXPOSE 5000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-5000}/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
