# Keycloak Server

[![CI](https://github.com/ianlintner/keycloak-server/actions/workflows/ci.yml/badge.svg)](https://github.com/ianlintner/keycloak-server/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Keycloak](https://img.shields.io/badge/Keycloak-26.0.7-blue)](https://www.keycloak.org/)

A customized Keycloak OAuth/OIDC server (version 26.0.7) with custom providers, Docker support, and Kubernetes deployment manifests.

## 🚀 Features

- **Custom Keycloak Server** - Extended with custom event listener providers
- **Docker Support** - Pre-configured Dockerfile and docker-compose.yml
- **Kubernetes Ready** - Complete K8s deployment manifests with HA support
- **CI/CD Pipeline** - GitHub Actions workflows for automated builds and deployments
- **Comprehensive Documentation** - MkDocs-based documentation site
- **PostgreSQL Integration** - Production-ready database setup
- **Health & Metrics** - Built-in health checks and Prometheus metrics
- **Security Hardened** - Network policies, pod security, and secret management

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Development](#development)
- [Deployment](#deployment)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)

## ⚡ Quick Start

### Using Docker Compose

```bash
# Clone the repository
git clone https://github.com/ianlintner/keycloak-server.git
cd keycloak-server

# Build the custom providers
mvn clean package

# Start all services
docker-compose up -d

# Access Keycloak
open http://localhost:8080
```

**Default credentials:** `admin` / `admin` (change immediately!)

### Using Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/

# Wait for deployment
kubectl wait --for=condition=ready pod -l app=keycloak -n keycloak --timeout=600s

# Check status
kubectl get pods -n keycloak
```

## 📦 Prerequisites

### Local Development
- Java 17 or higher
- Maven 3.8 or higher
- Docker 20.10+ and Docker Compose 2.0+

### Kubernetes Deployment
- Kubernetes cluster 1.24+
- kubectl configured
- Helm 3.x (optional)

## 🛠️ Installation

### Build from Source

```bash
# Clone repository
git clone https://github.com/ianlintner/keycloak-server.git
cd keycloak-server

# Build with Maven
mvn clean package

# Run tests
mvn test
```

### Docker Build

```bash
# Build custom providers first
mvn clean package

# Build Docker image
docker build -t keycloak-server:latest .

# Run locally
docker-compose up -d
```

## 💻 Development

### Project Structure

```
keycloak-server/
├── src/
│   ├── main/
│   │   ├── java/          # Custom Java providers
│   │   └── resources/     # Configuration files
│   └── test/              # Unit tests
├── k8s/                   # Kubernetes manifests
├── docs/                  # MkDocs documentation
├── .github/
│   ├── workflows/         # CI/CD pipelines
│   └── agents/           # Agent instructions
├── Dockerfile            # Docker image definition
├── docker-compose.yml    # Local development setup
├── pom.xml              # Maven configuration
└── mkdocs.yml           # Documentation config
```

### Custom Providers

This project includes example custom providers:

- **CustomEventListenerProvider** - Logs user and admin events

To add new providers:
1. Create provider class implementing Keycloak SPI interface
2. Create factory class
3. Register in `META-INF/services/`
4. Rebuild and redeploy

Example:
```java
public class CustomEventListenerProvider implements EventListenerProvider {
    @Override
    public void onEvent(Event event) {
        // Handle user events
        System.out.println("Event: " + event.getType());
    }
}
```

See [Custom Providers Documentation](docs/development/custom-providers.md) for details.

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=CustomEventListenerProviderTest

# Run with coverage
mvn verify
```

## 🚢 Deployment

### Docker Deployment

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f keycloak

# Stop services
docker-compose down
```

### Kubernetes Deployment

```bash
# Create namespace and deploy
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres-pvc.yaml
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/keycloak-deployment.yaml
kubectl apply -f k8s/keycloak-service.yaml
kubectl apply -f k8s/keycloak-ingress.yaml

# Check status
kubectl get pods -n keycloak
kubectl get services -n keycloak

# View logs
kubectl logs -f deployment/keycloak -n keycloak
```

### Production Deployment

For production deployments, ensure:
- Strong passwords configured in secrets
- SSL/TLS certificates installed
- Resource limits set appropriately
- Monitoring and alerting configured
- Backup strategy implemented
- High availability with multiple replicas

See [Production Deployment Guide](docs/deployment/production.md) for complete checklist.

## 📚 Documentation

Comprehensive documentation is available using MkDocs:

```bash
# Install MkDocs
pip install mkdocs mkdocs-material

# Serve documentation locally
mkdocs serve

# Access at http://localhost:8000
```

### Documentation Sections

- **Getting Started** - Installation, quick start, configuration
- **Development** - Building, custom providers, testing
- **Deployment** - Docker, Kubernetes, production setup
- **Operations** - Monitoring, troubleshooting, backup & restore
- **API Reference** - Custom provider APIs and configuration

## 🔧 Configuration

### Environment Variables

Key configuration options:

| Variable | Description | Default |
|----------|-------------|---------|
| `KC_DB` | Database type | `postgres` |
| `KC_DB_URL` | Database URL | `jdbc:postgresql://postgres:5432/keycloak` |
| `KC_HOSTNAME` | Public hostname | `localhost` |
| `KEYCLOAK_ADMIN` | Admin username | `admin` |
| `KEYCLOAK_ADMIN_PASSWORD` | Admin password | `admin` |

See [Configuration Guide](docs/getting-started/configuration.md) for all options.

## 🔍 Monitoring

Keycloak exposes health and metrics endpoints:

- **Health:** `http://localhost:8080/health`
- **Readiness:** `http://localhost:8080/health/ready`
- **Liveness:** `http://localhost:8080/health/live`
- **Metrics:** `http://localhost:8080/metrics`

Integrate with Prometheus, Grafana, and AlertManager for comprehensive monitoring.

See [Monitoring Guide](docs/operations/monitoring.md) for setup instructions.

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation:** [Full Documentation](https://ianlintner.github.io/keycloak-server)
- **Issues:** [GitHub Issues](https://github.com/ianlintner/keycloak-server/issues)
- **Discussions:** [GitHub Discussions](https://github.com/ianlintner/keycloak-server/discussions)

## 📊 Architecture

```
┌─────────────────────────────────────┐
│      Ingress / Load Balancer        │
└────────────────┬────────────────────┘
                 │
    ┌────────────▼────────────┐
    │   Keycloak Service      │
    │     (LoadBalancer)      │
    └────────────┬────────────┘
                 │
    ┌────────────▼────────────┐
    │   Keycloak Pods         │
    │   (Replicas: 2+)        │
    └────────────┬────────────┘
                 │
    ┌────────────▼────────────┐
    │  PostgreSQL Service     │
    │     (ClusterIP)         │
    └────────────┬────────────┘
                 │
    ┌────────────▼────────────┐
    │   PostgreSQL Pod        │
    │   (with PVC)            │
    └─────────────────────────┘
```

## 🔐 Security

- Custom providers are sandboxed
- Network policies restrict pod communication
- Secrets managed via Kubernetes secrets
- SSL/TLS enabled for production
- Regular security scanning via Trivy

## 🏷️ Version

**Current Version:** 1.0.0-SNAPSHOT  
**Keycloak Version:** 26.0.7  
**Java Version:** 17  
**PostgreSQL Version:** 16

## 📈 Roadmap

- [ ] Add more custom provider examples
- [ ] Helm chart support
- [ ] Additional authentication providers
- [ ] Enhanced monitoring dashboards
- [ ] Multi-region deployment guide

---

**Built with ❤️ for secure authentication and authorization**
