# Multi-stage build for Spring Boot backend
# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -g 1001 -S ecodeli && \
    adduser -S ecodeli -u 1001 -G ecodeli

# Set working directory
WORKDIR /app

# Create uploads directory with proper permissions
RUN mkdir -p /app/uploads && \
    chown -R ecodeli:ecodeli /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership of the JAR file
RUN chown ecodeli:ecodeli app.jar

# Switch to non-root user
USER ecodeli

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]