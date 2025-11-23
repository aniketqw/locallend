# =============================================================================
# LocalLend 2.0 - Multi-Stage Docker Build
# =============================================================================
# Optimized production image with design pattern refactoring
# Compatible with both AMD64 and ARM64 (Apple Silicon) platforms
# Stage 1: Build with Maven
# Stage 2: Run with minimal JRE
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build Stage
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS builder

LABEL maintainer="LocalLend Team"
LABEL description="LocalLend 2.0 Backend Builder"

WORKDIR /build

# Copy Maven configuration files first (for better layer caching)
COPY pom.xml .

# Note: Maven wrapper files (.mvn, mvnw, mvnw.cmd) are not included
# The base image already contains Maven 3.9

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# Verify the JAR was created
RUN ls -lah target/

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre AS runtime

LABEL maintainer="LocalLend Team"
LABEL description="LocalLend 2.0 - Refactored with Command, State & Event Patterns"
LABEL version="2.0.0"

# Install utilities for health checks and debugging
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security best practices
RUN groupadd -g 1001 locallend && \
    useradd -r -u 1001 -g locallend locallend

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R locallend:locallend /app

# Switch to non-root user
USER locallend

# Expose application port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables (override at runtime with -e or docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \
    SERVER_PORT=8080 \
    MONGODB_URI=mongodb://mongodb:27017/locallend \
    JWT_SECRET=change-this-secret-in-production

# Run the application with JVM optimizations
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]

# =============================================================================
# Build & Run Commands:
# =============================================================================
# Build:
#   docker build -t locallend2.0:latest -t locallend2.0:2.0.0 .
#
# Run (standalone):
#   docker run -d --name locallend2.0 \
#     -p 8080:8080 \
#     -e MONGODB_URI=mongodb://host.docker.internal:27017/locallend \
#     -e JWT_SECRET=your-secure-jwt-secret-here \
#     locallend2.0:latest
#
# Run (with docker-compose):
#   docker-compose up -d
#
# View logs:
#   docker logs -f locallend2.0
#
# Health check:
#   curl http://localhost:8080/actuator/health
# =============================================================================
