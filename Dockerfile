# Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY build.gradle .
COPY gradle gradle
COPY src src
COPY settings.gradle .
RUN ./gradlew clean bootJar --no-daemon

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar demo.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","demo.jar"]
