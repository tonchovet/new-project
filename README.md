# Demo Application

## What Is This?

A small, **reactive** Spring Boot application that shows how to combine:

| Layer | Tool | Purpose |
|-------|------|---------|
| **Persistence** | MySQL 8 + R2DBC | Store user/account data reactively |
| **Caching** | Redis | Cache frequently read data |
| **Blockchain** | Web3j | Interact with a local Ethereum node (Ganache, Geth, etc.) |
| **Web API** | Spring WebFlux | Expose non‑blocking REST endpoints |

It serves as a reference for building a modern micro‑service that touches a relational DB, an in‑memory cache, and a blockchain network—all in a single, lightweight JAR.

---

## Prerequisites

| Item | Minimum Version |
|------|-----------------|
| JDK | **Java 21** (the app is built with JDK 21) |
| Maven | **3.9.x** or newer |
| Docker & Docker Compose | *Optional* – for local dev environment |

If you don't want to run Docker Compose, you must have:

- A running MySQL 8 instance (`demo/demo` database, user `demo` / password `demo` by default).
- A running Redis instance.

---

## Configuration

The application reads configuration from `src/main/resources/application.yml`.  
The Docker Compose setup overrides the defaults via environment variables:

| Variable | Meaning | Default |
|----------|---------|---------|
| `R2DBC_URL` | R2DBC connection string for MySQL | `r2dbc:mysql://localhost:3306/demo` |
| `R2DBC_USERNAME` | MySQL user | `demo` |
| `R2DBC_PASSWORD` | MySQL password | `demo` |
| `REDIS_URL` | Redis URL | `redis://localhost:6379/0` |

You can override any of these by exporting them in your shell or by editing the `.env` file used by Docker Compose.

---

## Building the Project

```bash
# Clean, compile, and create an executable jar (skipping tests)
mvn clean package -DskipTests
