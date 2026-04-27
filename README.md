# Event Hub API

REST API for managing events, participants, and ticket purchases.

## Technologies

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Cache
- Spring Data Redis
- Flyway
- PostgreSQL
- Redis
- Spring Validation
- Springdoc OpenAPI (Swagger)
- Maven
- Docker
- Docker Compose

## How to run the project

### 1) Prerequisites

- Java 21 installed
- Maven 3.9+
- Docker and Docker Compose

### 2) Start the full stack with Docker

In the project root directory:

1. Create your environment file from the example:

```bash
cp .env.example .env
```

2. Fill in the credentials in the `.env` file

3. Start the containers:

```bash
docker compose up --build
```

This will start:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- PostgreSQL: `localhost:15432`
- Redis: `localhost:6379`

### 3) Run the application locally

If you prefer to run the API outside of Docker, start the dependencies first:

```bash
docker compose up -d postgres-eventhub redis-eventhub
```

Then export the required environment variables and run the application with Maven:

```bash
export DB_URL=jdbc:postgresql://localhost:15432/your-database
export POSTGRES_USER=your-username
export POSTGRES_PASSWORD=your-password
```

```bash
mvn spring-boot:run
```

Or, if you prefer to reuse the credentials from `.env`:

```bash
export POSTGRES_DB=your-database
export POSTGRES_USER=your-username
export POSTGRES_PASSWORD=your-password
export DB_URL=jdbc:postgresql://localhost:15432/${POSTGRES_DB}
mvn spring-boot:run
```

The application starts at:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 4) Environment variables

Credentials and sensitive parameters are not hardcoded in the project. To run with Docker Compose, define at least:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

Supported variables:

- `DB_URL` (default: `jdbc:postgresql://localhost:15432/eventhub-api-db`)
- `DB_DRIVER_CLASS_NAME` (default: `org.postgresql.Driver`)
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_PORT` (default: `15432`)
- `REDIS_HOST` (default: `localhost`)
- `REDIS_PORT` (default: `6379`)
- `APP_PORT` (default: `8080`)
- `CACHE_NAMES` (default: `events`)
- `CACHE_TTL` (default: `10m`)
- `LOG_LEVEL_ROOT` (default: `INFO`)
- `LOG_CONSOLE_FORMAT` (default: `logstash`)
- `SHUTDOWN_TIMEOUT_PER_PHASE` (default: `30s`)

### 5) Event cache

The event listing endpoint (`GET /events`) uses Redis cache.

- Configured cache: `events`
- Default TTL: `10m`
- Automatic invalidation on event creation, update, and deletion

## How to run the tests

```bash
mvn test
```

## Technical decisions

### 1) PostgreSQL as relational database

The domain involves relational entities with strong integrity (`events`, `participants`, `tickets`) and consistency rules such as:

- foreign keys between tickets, events, and participants;
- uniqueness constraint on participant email;
- indexes for queries by event and participant.

PostgreSQL was chosen for its robustness, constraint support, and good performance for this type of data model.

### 2) Redis for event listing cache

Redis was adopted for the event listing because:

- it reduces repeated database reads on paginated queries;
- it works well in a containerized environment;
- it allows configurable TTL and simple cache invalidation.

### 3) Flyway for schema versioning

Database changes are applied through versioned migrations, ensuring:

- traceability of changes;
- automatic schema initialization when the application starts.

### 4) Spring Data JPA

Adopted to reduce boilerplate in database access and speed up CRUD operation implementation, keeping the persistence code simple and consistent with the Spring ecosystem.

### 5) Jakarta Bean Validation

Input validations are declarative in DTOs and automatically executed at endpoints with `@Valid`, ensuring:

- fast failure for invalid payloads;
- centralized and reusable rules;
- standardized error responses.

### 6) Swagger/OpenAPI with Springdoc

API documentation was integrated via Springdoc to:

- facilitate exploration and manual testing of endpoints;
- expose an OpenAPI contract for integrations;