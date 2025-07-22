# Dockerfile pour Railway - Backend EcoDeli
# Multi-stage build optimisé pour Spring Boot + PostgreSQL

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy backend files
COPY ecodeli-backend/pom.xml ./
COPY ecodeli-backend/mvnw ./
COPY ecodeli-backend/mvnw.cmd ./
COPY ecodeli-backend/.mvn ./.mvn

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY ecodeli-backend/src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks (Railway compatibility)
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1001 -S ecodeli && \
    adduser -S ecodeli -u 1001 -G ecodeli

# Set working directory
WORKDIR /app

# Create uploads directory with proper permissions
RUN mkdir -p /app/uploads && \
    chown -R ecodeli:ecodeli /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown ecodeli:ecodeli app.jar

# Switch to non-root user
USER ecodeli

# Expose port (Railway will map this automatically)
EXPOSE 8080

# Health check for Railway
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
