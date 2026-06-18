# AML Transaction Monitoring System

Anti-Money Laundering (AML) transaction monitoring and SAR (Suspicious Activity Report) generation system.

## Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Node.js 18+** and **npm**
- **PostgreSQL 15** (or use Docker)
- **Docker & Docker Compose** (optional, for containerized setup)

## Project Structure

```
Project/
├── aml-monitoring/
│   ├── backend/          # Spring Boot REST API (Java 17, Maven)
│   ├── frontend/         # React + Vite frontend
│   └── docker-compose.yml
├── backend/              # Standalone backend (Dockerfile + Maven)
└── docker-compose.yml
```

## Quick Start with Docker

The easiest way to run everything:

```bash
cd aml-monitoring
docker-compose up --build
```

This starts:
- **PostgreSQL** on port `5432`
- **Backend API** on port `8080`

Then start the frontend separately (see below).

## Running the Backend (Manual)

### 1. Start PostgreSQL

Make sure PostgreSQL is running with the following defaults (or set env vars):

| Variable      | Default     |
|---------------|-------------|
| `DB_HOST`     | `localhost` |
| `DB_PORT`     | `5432`      |
| `DB_NAME`     | `aml_db`    |
| `DB_USER`     | `aml_user`  |
| `DB_PASSWORD` | `aml_pass`  |

You can start just the database via Docker:

```bash
cd aml-monitoring
docker-compose up db
```

### 2. Build and Run

```bash
cd aml-monitoring/backend
mvn clean install
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

Swagger UI is available at **http://localhost:8080/swagger-ui.html**.

## Running the Frontend

```bash
cd aml-monitoring/frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173** (Vite default) and proxies `/api` requests to the backend at `http://localhost:8080`.

## Running Tests

### Backend tests

```bash
cd aml-monitoring/backend
mvn test
```

Integration tests use an H2 in-memory database (`application-test.yml`).

### Frontend

```bash
cd aml-monitoring/frontend
npm run build
```

## API Endpoints

The backend exposes REST endpoints under `/api`, including:
- Transaction monitoring
- SAR list and generation
- Audit log
