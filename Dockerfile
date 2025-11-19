# === BUILD stage ===
# Use an official Maven image with JDK as the base image for building
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code to the container
COPY pom.xml .
COPY src ./src

# Build the application using Maven, skipping tests for a faster image build
RUN mvn clean package -DskipTests

# === RUN stage ===
# Use a minimal OpenJDK JRE image as the base image for running the application
FROM openjdk:17-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the 'build' stage to the new 'run' stage
# Replace 'my-application.jar' with your actual JAR filename from the /target directory
COPY --from=build /app/target/my-application.jar /app/my-application.jar

# Expose the port your application listens on (e.g., 8080 for Spring Boot)
EXPOSE 8080

# Set the command to run the application
CMD ["java", "-jar", "my-application.jar"]
