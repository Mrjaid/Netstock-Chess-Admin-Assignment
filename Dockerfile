# Multi-stage build: compile with Maven, run on lightweight JRE
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
# copy only what we need for dependency resolution first to leverage caching
COPY pom.xml .
COPY src ./src

# Build the application using the 'production' profile so Vaadin frontend goals run and
# 'flow-build-info.json' is generated (avoids dev-mode startup failure in the container).
RUN mvn -B -DskipTests -Pproduction package

# Runtime image (use Java 17 runtime to match project java.version)
FROM eclipse-temurin:17-jre
WORKDIR /app
# Install curl so healthcheck using curl works
RUN apt-get update && apt-get install -y curl ca-certificates && rm -rf /var/lib/apt/lists/*
# Copy built jar from build stage (artifact name from target)
COPY --from=build /app/target/chess_admin-0.0.1-SNAPSHOT.jar ./app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Ensure the H2 database folder exists when running in container if mounted
RUN mkdir -p /app/data/h2

ENTRYPOINT ["java","-jar","/app/app.jar"]
