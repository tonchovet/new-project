#!/usr/bin/env bash
set -euo pipefail

# ---- Configurable values -----------------------------------------------
# 1st arg  : project name (folder + description in README)
# 2nd arg  : Maven artifact id / jar name (defaults to the same as project name)
PROJECT_NAME=${1:-"demo-project"}
GROUP_ID="com.example"
ARTIFACT_ID=${2:-"demo-project"}
BASE_PACKAGE="${GROUP_ID}.${ARTIFACT_ID}"
PACKAGE_DIR=${BASE_PACKAGE//./\/}

# ---- Create directory structure -----------------------------------------
mkdir -p "src/main/java/${PACKAGE_DIR}/config"
mkdir -p "src/main/java/${PACKAGE_DIR}/controller"
mkdir -p "src/main/java/${PACKAGE_DIR}/dto"
mkdir -p "src/main/java/${PACKAGE_DIR}/model"
mkdir -p "src/main/java/${PACKAGE_DIR}/repository"
mkdir -p "src/main/java/${PACKAGE_DIR}/service"
mkdir -p src/main/resources
mkdir -p "src/test/java/${PACKAGE_DIR}"

# ------------------------------------------------------------------
# Generate the pom.xml
# ------------------------------------------------------------------
cat > pom.xml <<EOF
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>${GROUP_ID}</groupId>
    <artifactId>${ARTIFACT_ID}</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starter Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Starter Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Spring Boot Starter Data Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- MySQL Connector -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok (optional) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Boot Starter Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF

# ------------------------------------------------------------------
# Dockerfile – multi‑stage build
# ------------------------------------------------------------------
cat > Dockerfile <<EOF
# Build stage
FROM maven:3.9.6-eclipse-temurin-17-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
EOF

# ------------------------------------------------------------------
# docker‑compose.yml
# ------------------------------------------------------------------
cat > docker-compose.yml <<EOF
version: "3.8"

services:
  app:
    build: .
    container_name: ${ARTIFACT_ID}-app
    ports:
      - "8080:8080"
    depends_on:
      - redis
      - mysql
    environment:
      SPRING_PROFILES_ACTIVE: dev
    restart: unless-stopped

  redis:
    image: redis:7
    container_name: ${ARTIFACT_ID}-redis
    ports:
      - "6379:6379"
    restart: unless-stopped

  mysql:
    image: mysql:8
    container_name: ${ARTIFACT_ID}-mysql
    environment:
      MYSQL_DATABASE: publicworks
      MYSQL_ROOT_PASSWORD: root
      MYSQL_USER: pw
      MYSQL_PASSWORD: pw
    ports:
      - "3306:3306"
    restart: unless-stopped
EOF

# ------------------------------------------------------------------
# .gitignore
# ------------------------------------------------------------------
cat > .gitignore <<EOF
target/
*.log
*.class
*.jar
*.war
*.ear
!*.jar
!*.war
!*.ear
**/node_modules
/.idea
/.project
/.settings
/.classpath
/.metadata
*.iml
*.swp
*.DS_Store
*.env
EOF

# ------------------------------------------------------------------
# README.md
# ------------------------------------------------------------------
cat > README.md <<EOF
# ${ARTIFACT_ID}

## Overview

This project implements the backend for a public works social network. It is built using Spring Boot 3.x, Java 17, Maven, and Docker. Redis is used for fast in‑memory data operations and MySQL is an optional relational database for persistent storage.

## Prerequisites

- Docker & Docker Compose
- Java 17
- Maven

## Getting Started

1. **Build the application**

   \`\`\`bash
   mvn clean package
   \`\`\`

2. **Run with Docker Compose**

   \`\`\`bash
   docker compose up --build
   \`\`\`

   The API will be available at <http://localhost:8080>.

3. **Run locally (without Docker)**

   \`\`\`bash
   mvn spring-boot:run
   \`\`\`

## Project Structure

- `src/main/java/${BASE_PACKAGE}` – Main source code
- `src/main/resources` – Configuration files
- `src/test/java/${BASE_PACKAGE}` – Unit tests

## Configuration

The application uses `application.yml` as the default configuration file. You can override settings by creating an `application-dev.yml` or `application-prod.yml`.

## Docker

The Dockerfile follows a multi‑stage build pattern for optimal image size. The `docker-compose.yml` defines services for the application, Redis, and MySQL.

## Development

- Use Lombok for reducing boilerplate.
- Use Spring Data JPA for database interactions.
- Use Spring Data Redis for caching and messaging.
- Use Spring Security for authentication and authorization.

Feel free to extend the skeleton with entities, repositories, services, and controllers as needed.

## Contributing

Pull requests are welcome! Please open an issue first to discuss any major changes.

## License

MIT
EOF

# ------------------------------------------------------------------
# Application configuration
# ------------------------------------------------------------------
cat > src/main/resources/application.yml <<EOF
server:
  port: 8080

spring:
  application:
    name: ${ARTIFACT_ID}
  datasource:
    url: jdbc:mysql://mysql:3306/publicworks?useSSL=false&allowPublicKeyRetrieval=true
    username: pw
    password: pw
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: false
  redis:
    host: redis
    port: 6379
    password:

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  health:
    db:
      enabled: true
    redis:
      enabled: true
EOF

cat > src/main/resources/application-dev.yml <<EOF
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/publicworks?useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
  redis:
    host: localhost
    port: 6379
EOF

cat > src/main/resources/application-prod.yml <<EOF
# Production settings can override the defaults
EOF

# ------------------------------------------------------------------
# Main application class
# ------------------------------------------------------------------
cat > src/main/java/$PACKAGE_DIR/PublicWorksApplication.java <<EOF
package ${BASE_PACKAGE};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PublicWorksApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublicWorksApplication.class, args);
    }
}
EOF

# ------------------------------------------------------------------
# Redis configuration
# ------------------------------------------------------------------
cat > src/main/java/$PACKAGE_DIR/config/RedisConfig.java <<EOF
package ${BASE_PACKAGE}.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
}
EOF

# ------------------------------------------------------------------
# Security configuration
# ------------------------------------------------------------------
cat > src/main/java/$PACKAGE_DIR/config/SecurityConfig.java <<EOF
package ${BASE_PACKAGE}.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(); // For simplicity; replace with JWT or OAuth2 in production
        return http.build();
    }
}
EOF

# ------------------------------------------------------------------
# Project entity
# ------------------------------------------------------------------
cat > src/main/java/$PACKAGE_DIR/model/Project.java <<EOF
package ${BASE_PACKAGE}.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 5000)
    private String description;

    private java.math.BigDecimal targetAmount;

    private java.math.BigDecimal collectedAmount = java.math.BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();
}
EOF

# ------------------------------------------------------------------
# DTO
# ------------------------------------------------------------------
cat > src/main/java/$PACKAGE_DIR/dto/ProjectDto.java <<EOF
package ${BASE_PACKAGE}.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProjectDto {
    private String title;
    private String description;
    private java.math.BigDecimal targetAmount;
}
EOF

# ------------------------------------------------------------------
# Repository
# ------------------------------------------------------------------
cat > src/main/java/$PACKAGE_DIR/repository/ProjectRepository.java <<EOF
package ${BASE_PACKAGE}.repository;

import ${BASE_PACKAGE}.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
EOF

# ------------------------------------------------------------------
# Service
# ------------------------------------------------------------------
cat > src/main/java/$PACKAGE_DIR/service/ProjectService.java <<EOF
package ${BASE_PACKAGE}.service;

import ${BASE_PACKAGE}.dto.ProjectDto;
import ${BASE_PACKAGE}.model.Project;
import ${BASE_PACKAGE}.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Project createProject(ProjectDto dto) {
        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setTargetAmount(dto.getTargetAmount());
        return projectRepository.save(project);
    }
}
EOF

# ------------------------------------------------------------------
# Controller
# ------------------------------------------------------------------
cat > src/main/java/$PACKAGE_DIR/controller/ProjectController.java <<EOF
package ${BASE_PACKAGE}.controller;

import ${BASE_PACKAGE}.dto.ProjectDto;
import ${BASE_PACKAGE}.model.Project;
import ${BASE_PACKAGE}.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAll() {
        return ResponseEntity.ok(projectService.findAll());
    }

    @PostMapping
    public ResponseEntity<Project> create(@RequestBody ProjectDto dto) {
        return ResponseEntity.ok(projectService.createProject(dto));
    }
}
EOF

# ------------------------------------------------------------------
# Test
# ------------------------------------------------------------------
cat > src/test/java/$PACKAGE_DIR/ProjectControllerTest.java <<EOF
package ${BASE_PACKAGE};

import ${BASE_PACKAGE}.controller.ProjectController;
import ${BASE_PACKAGE}.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import java.util.Collections;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    void getAll_ReturnsEmptyList() throws Exception {
        when(projectService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/projects")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
EOF

echo "Project scaffold created successfully."
echo "Run 'mvn clean package' to build, then 'docker compose up' to start services."
