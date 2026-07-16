# Daynix App

Daily Productivity & Hourly Task Tracking SaaS app.

This repository currently contains the runnable base skeleton only. Business logic and domain tables will be added in later phases.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- PostgreSQL 17
- Docker and Docker Compose

## Project Structure

```text
.
├── backend/
├── frontend/
└── docker-compose.yml
```

## Run With Docker Compose

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- OpenAPI UI: http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432

## Local Development

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

The frontend calls the backend health endpoint at `/api/health`.
