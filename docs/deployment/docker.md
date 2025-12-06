# Docker Deployment

Deploy Keycloak Server using Docker and Docker Compose.

## Prerequisites

- Docker 20.10 or higher
- Docker Compose 2.0 or higher

## Quick Start

### 1. Build the Application

```bash
mvn clean package
```

### 2. Start Services

```bash
docker-compose up -d
```

### 3. Verify Deployment

```bash
# Check running containers
docker-compose ps

# View logs
docker-compose logs -f keycloak

# Check health
curl http://localhost:8080/health/ready
```

## Docker Compose Configuration

The `docker-compose.yml` includes:

- **PostgreSQL** - Database on port 5432
- **Keycloak** - Server on ports 8080/8443

### Services

```yaml
services:
  postgres:
    image: postgres:16-alpine
    # PostgreSQL database
  
  keycloak:
    build: .
    # Keycloak server
```

## Building Custom Image

### Build Image

```bash
# Build with Maven first
mvn clean package

# Build Docker image
docker build -t keycloak-server:latest .

# Build with custom tag
docker build -t keycloak-server:1.0.0 .
```

### Push to Registry

```bash
# Tag for Docker Hub
docker tag keycloak-server:latest ianlintner/keycloak-server:latest

# Login to Docker Hub
docker login

# Push image
docker push ianlintner/keycloak-server:latest
```

### Multi-architecture Build

```bash
# Setup buildx
docker buildx create --name mybuilder --use

# Build for multiple platforms
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t ianlintner/keycloak-server:latest \
  --push .
```

## Environment Configuration

### Development

```yaml
environment:
  KEYCLOAK_ADMIN: admin
  KEYCLOAK_ADMIN_PASSWORD: admin
  KC_DB: postgres
  KC_HTTP_ENABLED: "true"
```

### Production

```yaml
environment:
  KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN}
  KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}
  KC_DB: postgres
  KC_HOSTNAME: keycloak.example.com
  KC_PROXY: edge
  KC_HTTPS_CERTIFICATE_FILE: /opt/keycloak/conf/server.crt.pem
  KC_HTTPS_CERTIFICATE_KEY_FILE: /opt/keycloak/conf/server.key.pem
```

## Volume Management

### Persist Database Data

```yaml
volumes:
  postgres_data:
    driver: local
```

### Custom Providers

```yaml
volumes:
  - ./target:/opt/keycloak/providers
```

### Configuration Files

```yaml
volumes:
  - ./conf:/opt/keycloak/conf
```

## Networking

### Default Network

```yaml
networks:
  keycloak-network:
    driver: bridge
```

### Custom Network

```yaml
networks:
  keycloak-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.25.0.0/16
```

## Health Checks

### Keycloak Health Check

```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:8080/health/ready || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 5
  start_period: 60s
```

### PostgreSQL Health Check

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U keycloak"]
  interval: 10s
  timeout: 5s
  retries: 5
```

## Scaling

### Multiple Keycloak Instances

```bash
docker-compose up -d --scale keycloak=3
```

### Load Balancing

Use nginx or HAProxy:

```yaml
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - keycloak
```

## Logs

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f keycloak

# Last 100 lines
docker-compose logs --tail=100 keycloak
```

### Log Configuration

```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

## Backup and Restore

### Backup Database

```bash
docker-compose exec postgres pg_dump -U keycloak keycloak > backup.sql
```

### Restore Database

```bash
docker-compose exec -T postgres psql -U keycloak keycloak < backup.sql
```

## Troubleshooting

### Container Won't Start

```bash
# Check logs
docker-compose logs keycloak

# Inspect container
docker-compose exec keycloak /bin/bash

# Check ports
netstat -tuln | grep 8080
```

### Database Connection Issues

```bash
# Check database
docker-compose exec postgres psql -U keycloak -c "SELECT 1"

# Restart services
docker-compose restart
```

### Clean Restart

```bash
# Stop and remove containers
docker-compose down

# Remove volumes
docker-compose down -v

# Clean rebuild
docker-compose build --no-cache
docker-compose up -d
```

## Production Checklist

- [ ] Use strong passwords
- [ ] Enable HTTPS
- [ ] Configure proper hostname
- [ ] Set up monitoring
- [ ] Configure backups
- [ ] Limit resource usage
- [ ] Use secrets management
- [ ] Enable audit logging

## Next Steps

- [Kubernetes Deployment](kubernetes.md)
- [Production Setup](production.md)
- [Monitoring](../operations/monitoring.md)
