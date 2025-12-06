# Installation

This guide will help you install and set up the Keycloak Server.

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- Docker and Docker Compose (for containerized deployment)
- Kubernetes cluster (for K8s deployment)

## Local Development Setup

### 1. Clone the Repository

```bash
git clone https://github.com/ianlintner/keycloak-server.git
cd keycloak-server
```

### 2. Build the Project

```bash
mvn clean package
```

This will:
- Compile the Java code
- Run tests
- Package the custom providers into a JAR file

### 3. Start with Docker Compose

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- Keycloak server on port 8080

### 4. Access Keycloak

Open your browser and navigate to:
- Admin Console: http://localhost:8080
- Default credentials: `admin` / `admin`

!!! warning
    Change the default admin password immediately in production environments!

## System Requirements

### Minimum Requirements
- CPU: 2 cores
- RAM: 2 GB
- Disk: 10 GB

### Recommended Requirements
- CPU: 4 cores
- RAM: 4 GB
- Disk: 20 GB

## Database Setup

Keycloak requires a database. We support PostgreSQL:

### PostgreSQL
```bash
docker run -d \
  --name keycloak-postgres \
  -e POSTGRES_DB=keycloak \
  -e POSTGRES_USER=keycloak \
  -e POSTGRES_PASSWORD=keycloak \
  -p 5432:5432 \
  postgres:16-alpine
```

## Next Steps

- [Quick Start Guide](quick-start.md)
- [Configuration Options](configuration.md)
- [Development Guide](../development/building.md)
