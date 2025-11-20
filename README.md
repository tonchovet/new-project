# demo-project

## Overview

This project implements the backend for a public works social network. It is built using Spring Boot 3.x, Java 17, Maven, and Docker. Redis is used for fast in‑memory data operations and MySQL is an optional relational database for persistent storage.

## Prerequisites

- Docker & Docker Compose
- Java 17
- Maven

## Getting Started

1. **Build the application**

   ```bash
   mvn clean package
   ```

2. **Run with Docker Compose**

   ```bash
   docker compose up --build
   ```

   The API will be available at <http://localhost:8080>.

3. **Run locally (without Docker)**

   ```bash
   mvn spring-boot:run
   ```

## Project Structure

-  – Main source code
-  – Configuration files
-  – Unit tests

## Configuration

The application uses  as the default configuration file. You can override settings by creating an  or .

## Docker

The Dockerfile follows a multi‑stage build pattern for optimal image size. The  defines services for the application, Redis, and MySQL.

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
