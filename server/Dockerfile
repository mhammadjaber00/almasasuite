# Use the official Eclipse Temurin JDK 21 image as the base image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Add a non-root user to run the application
RUN addgroup -S almasa && adduser -S almasa -G almasa

# Set the user to run the application
USER almasa

# Copy the JAR file from the build context to the container
COPY --chown=almasa:almasa server/build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Set environment variables with default values
ENV DB_URL=jdbc:postgresql://postgres:5432/almasa \
    DB_USER=postgres \
    DB_PASSWORD=postgres \
    DB_DRIVER=org.postgresql.Driver \
    JWT_SECRET=change-me-in-production \
    STRIPE_WEBHOOK_SECRET="" \
    STRIPE_API_KEY="" \
    MINIO_ENDPOINT=http://minio:9000 \
    MINIO_ACCESS_KEY=minioadmin \
    MINIO_SECRET_KEY=minioadmin \
    MINIO_BUCKET=almasa

# Set JVM options
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Set the entry point to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Health check to verify the application is running
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/health || exit 1