# Use a lightweight official Java runtime
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy maven wrapper and pom file first to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Give execution permission to the Maven wrapper script
RUN chmod +x mvnw

# Download dependencies (this caches them for faster builds)
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Find the built jar and rename it to app.jar
RUN mv target/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]