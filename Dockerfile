# Multi-stage build for a Spring Boot (Maven) application
# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only what is needed for build cache efficiency
COPY pom.xml ./
COPY src ./src

# Package the application (skip tests for faster builds; remove -DskipTests to run tests)
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar (wildcard handles versioned jar names)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
