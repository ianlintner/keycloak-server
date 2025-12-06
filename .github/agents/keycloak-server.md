# Keycloak Server Agent Instructions

## Overview
This repository contains a customized Keycloak OAuth server (version 26.4.7) with custom providers, Docker support, and Kubernetes deployment manifests.

## Project Structure
- `src/main/java` - Java source code for custom Keycloak providers
- `src/test/java` - Unit tests
- `k8s/` - Kubernetes deployment manifests
- `docs/` - MkDocs documentation
- `Dockerfile` - Docker image definition
- `docker-compose.yml` - Local development setup

## Building the Project

### Local Build
```bash
mvn clean package
```

### Docker Build
```bash
mvn clean package
docker build -t keycloak-server:latest .
```

### Local Development
```bash
docker-compose up -d
```
Access Keycloak at http://localhost:8080 (admin/admin)

## Testing

### Run Tests
```bash
mvn test
```

### Run with Coverage
```bash
mvn verify
```

## Deployment

### Kubernetes Deployment
```bash
# Apply all manifests
kubectl apply -f k8s/

# Or apply individually
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres-pvc.yaml
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/keycloak-deployment.yaml
kubectl apply -f k8s/keycloak-service.yaml
kubectl apply -f k8s/keycloak-ingress.yaml
```

### Verify Deployment
```bash
kubectl get pods -n keycloak
kubectl get services -n keycloak
kubectl logs -f deployment/keycloak -n keycloak
```

## Custom Providers
This project includes example custom providers:
- `CustomEventListenerProvider` - Logs user and admin events

To add new providers:
1. Create a new class implementing the appropriate Keycloak SPI interface
2. Create a factory class implementing the corresponding factory interface
3. Register the factory in `META-INF/services/`
4. Rebuild and redeploy

## Configuration

### Environment Variables
Key environment variables for configuration:
- `KC_DB` - Database type (postgres)
- `KC_DB_URL` - Database connection URL
- `KC_DB_USERNAME` - Database username
- `KC_DB_PASSWORD` - Database password
- `KEYCLOAK_ADMIN` - Admin username
- `KEYCLOAK_ADMIN_PASSWORD` - Admin password
- `KC_HOSTNAME` - Public hostname
- `KC_PROXY` - Proxy mode (edge, reencrypt, passthrough)

### Kubernetes Secrets
Update secrets in `k8s/secrets.yaml` before deploying to production.

## CI/CD
GitHub Actions workflows:
- `.github/workflows/ci.yml` - Build, test, and push Docker images
- `.github/workflows/deploy.yml` - Deploy to Kubernetes

Required secrets:
- `DOCKER_USERNAME` - Docker Hub username
- `DOCKER_PASSWORD` - Docker Hub password/token
- `KUBE_CONFIG` - Base64-encoded kubeconfig

## Documentation
Documentation is maintained using MkDocs. To serve locally:
```bash
cd docs
mkdocs serve
```

## Common Tasks

### Update Keycloak Version
1. Update `keycloak.version` in `pom.xml`
2. Update base image in `Dockerfile`
3. Update docker-compose.yml if needed
4. Test thoroughly before deploying

### Add Dependencies
1. Add to `pom.xml`
2. Rebuild: `mvn clean package`
3. Rebuild Docker image

### Troubleshooting
- Check logs: `kubectl logs -f deployment/keycloak -n keycloak`
- Check health: `kubectl get pods -n keycloak`
- Access metrics: http://keycloak-host:8080/metrics
- Access health: http://keycloak-host:8080/health

## Support
For issues or questions, please open a GitHub issue.
