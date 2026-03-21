# Identity Service

A production-ready **Identity & Access Management (IAM) microservice** built with Spring Boot 4, implementing JWT-based authentication, Role-Based Access Control (RBAC), and OAuth2 Resource Server patterns.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Security](#security)
- [Testing](#testing)
- [Project Structure](#project-structure)

---

## Overview

Identity Service is a self-contained authentication and authorization microservice designed to be integrated into a microservices architecture. It handles user lifecycle management, JWT token issuance/revocation, and fine-grained permission control through a roles-and-permissions model.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Identity Service                     │
│                                                         │
│  ┌──────────────┐    ┌──────────────┐    ┌───────────┐  │
│  │  Controllers │───▶│   Services   │───▶│   JPA     │  │
│  │  /auth       │    │  Auth        │    │Repositorie│  │
│  │  /users      │    │  User        │    └─────┬─────┘  │
│  │  /roles      │    │  Role        │          │        │
│  │  /permissions│    │  Permission  │    ┌─────▼─────┐  │
│  └──────┬───────┘    └──────┬───────┘    │  MySQL /  │  │
│         │                   │            │  H2 (test)│  │
│  ┌──────▼───────────────────▼───────┐    └───────────┘  │
│  │         Security Layer           │                   │
│  │  JWT Filter → CustomJwtDecoder   │                   │
│  │  Method Security (@Pre/@Post)    │                   │
│  └──────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────┘
```

The service follows a layered architecture — **Controller → Service → Repository** — with MapStruct for DTO mapping and Spring Security for stateless JWT authentication.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.2 |
| Language | Java 21 |
| Security | Spring Security 6 + OAuth2 Resource Server |
| Authentication | JWT (Nimbus JOSE + JWT) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL (production), H2 (testing) |
| Mapping | MapStruct 1.6.3 |
| Build Tool | Maven (Wrapper included) |
| Code Generation | Lombok |
| Test Coverage | JaCoCo |
| Testing | JUnit 5 + Mockito + MockMvc |

---

## Features

- **JWT Authentication** — Stateless token-based auth with configurable expiry and refresh windows
- **Token Revocation** — Logout invalidates tokens via a persistent denylist; introspect endpoint validates token state before every request
- **Token Refresh** — Silent refresh flow without requiring re-login
- **RBAC** — Hierarchical Roles → Permissions model; roles and permissions are managed at runtime via API
- **Method-Level Authorization** — `@PreAuthorize` / `@PostAuthorize` guards on service methods
- **Custom Validation** — Annotation-driven date-of-birth constraint (`@DobConstraint`) with configurable minimum age
- **Global Exception Handling** — Unified error response format with structured error codes
- **Admin Bootstrapping** — Auto-creates default admin user and roles on first startup (MySQL only)
- **Actuator** — Health and metrics endpoints exposed out of the box
- **Profile-Aware Config** — Separate `application-prod.yaml` for production secrets via environment variables

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- MySQL 8+ (for local development)

### Run Locally

**1. Clone the repository**

```bash
git clone https://github.com/your-username/identity-service.git
cd identity-service
```

**2. Start a MySQL instance**

```bash
docker run -d \
  --name identity-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=identity-service \
  -p 3306:3306 \
  mysql:8
```

**3. Run the service**

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8080/identity`.

A default admin account (`admin` / `admin`) is created automatically on first run — **change the password immediately**.

### Run Tests

```bash
./mvnw test
```

### Build JAR

```bash
./mvnw clean package -DskipTests
java -jar target/identity-service-0.0.1-SNAPSHOT.jar
```

---

## Configuration

All configuration lives in `src/main/resources/application.yaml`. Sensitive values are injected via environment variables.

| Environment Variable | Default (dev) | Description |
|---|---|---|
| `DEV_DBMS_CONNECTION` | `jdbc:mysql://localhost:3306/identity-service` | JDBC connection URL |
| `DEV_DBMS_USERNAME` | `root` | Database username |
| `DEV_DBMS_PASSWORD` | `root` | Database password |
| `DEV_SIGNERKEY` | *(hardcoded dev key)* | HMAC-SHA512 signing key |
| `PROD_SIGNERKEY` | *(required in prod)* | Production signing key (via `application-prod.yaml`) |

### JWT Settings

```yaml
jwt:
  valid-duration: 3600        # Access token TTL (seconds)
  refreshable-duration: 36000 # Refresh window (seconds)
```

---

## API Reference

Base path: `/identity`

### Authentication

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `POST` | `/auth/token` | No | Obtain JWT access token |
| `POST` | `/auth/introspect` | No | Validate a token |
| `POST` | `/auth/refresh` | No | Refresh an access token |
| `POST` | `/auth/logout` | No | Revoke a token |

**Login example:**

```bash
curl -X POST http://localhost:8080/identity/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin"}'
```

```json
{
  "code": 1000,
  "result": {
    "token": "<jwt>",
    "authenticated": true
  }
}
```

### Users

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `POST` | `/users` | No | Register a new user |
| `GET` | `/users` | ADMIN role | List all users |
| `GET` | `/users/{id}` | Owner only | Get user by ID |
| `PUT` | `/users/{id}` | Authenticated | Update user |
| `DELETE` | `/users/{id}` | Authenticated | Delete user |
| `GET` | `/users/myInfo` | Authenticated | Get own profile |

### Roles & Permissions

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `POST` | `/roles` | Authenticated | Create a role |
| `GET` | `/roles` | Authenticated | List all roles |
| `DELETE` | `/roles/{role}` | Authenticated | Delete a role |
| `POST` | `/permissions` | Authenticated | Create a permission |
| `GET` | `/permissions` | Authenticated | List all permissions |
| `DELETE` | `/permissions/{name}` | Authenticated | Delete a permission |

### Error Response Format

All errors follow a consistent envelope:

```json
{
  "code": 1005,
  "message": "User not existed"
}
```

| Code | Meaning | HTTP Status |
|---|---|---|
| 1000 | Success | 200 |
| 1002 | User already existed | 400 |
| 1003 | Username too short | 400 |
| 1004 | Password too short | 400 |
| 1005 | User not found | 404 |
| 1006 | Unauthenticated | 401 |
| 1007 | Unauthorized | 403 |
| 1008 | Invalid date of birth | 400 |
| 9999 | Uncategorized error | 500 |

---

## Security

- **Algorithm**: HMAC-SHA512 (`HS512`) for JWT signing
- **Token Denylist**: Invalidated tokens are stored in the database; every request verifies against this list via `CustomJwtDecoder`
- **Password Hashing**: BCrypt with cost factor 10
- **CSRF**: Disabled (stateless API)
- **Public endpoints**: `POST /users`, `POST /auth/*` — all others require a valid bearer token
- **Method security**: `@PreAuthorize("hasRole('ADMIN')")` on admin-only operations; `@PostAuthorize` enforces ownership on user lookups
- **Production secrets**: Never committed — always loaded from environment variables in `application-prod.yaml`

---

## Testing

The project includes two test suites with an in-memory H2 database (MySQL-compatible mode):

**`UserServiceTest`** — Unit tests for the service layer using Mockito mocks:
- `createUser_validRequest_success`
- `createUser_userExisted_fail`
- `getMyInfo_valid_success`
- `getMyInfo_userNotFound_error`

**`UserControllerTest`** — Integration tests for the web layer using MockMvc:
- `createUser_validRequest_success`
- `createUser_usernameInvalid_fail`

Code coverage is measured with **JaCoCo** (excludes DTOs, entities, mappers, and config classes to focus on business logic).

```bash
./mvnw verify          # Run tests + generate coverage report
open target/site/jacoco/index.html
```

---

## Project Structure

```
src/
├── main/java/me/ihqqq/identity_service/
│   ├── configuration/       # Security, JWT decoder, app init
│   ├── constant/            # Predefined role names
│   ├── controller/          # REST controllers
│   ├── dto/
│   │   ├── request/         # Inbound DTOs + ApiResponse wrapper
│   │   └── response/        # Outbound DTOs
│   ├── entity/              # JPA entities (User, Role, Permission, InvalidatedToken)
│   ├── exception/           # AppException, ErrorCode enum, GlobalExceptionHandler
│   ├── mapper/              # MapStruct mappers
│   ├── repository/          # Spring Data JPA repositories
│   ├── service/             # Business logic
│   └── validator/           # Custom @DobConstraint annotation + validator
├── main/resources/
│   ├── application.yaml
│   └── application-prod.yaml
└── test/
    ├── java/                # Unit + integration tests
    └── resources/
        └── test.properties  # H2 datasource config
```

---

## License

This project is for portfolio and educational purposes.
