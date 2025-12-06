# Keycloak Server

Welcome to the Keycloak Server documentation. This is a customized Keycloak OAuth server implementation based on Keycloak version 26.0.7.

## Overview

This project provides:

- 🔐 **Custom Keycloak Server** - Extended with custom providers and configurations
- 🐳 **Docker Support** - Pre-configured Docker and Docker Compose setup
- ☸️ **Kubernetes Ready** - Complete K8s deployment manifests
- 🔄 **CI/CD Pipeline** - GitHub Actions workflows for automated builds and deployments
- 📚 **Comprehensive Documentation** - Detailed guides and API references

## Features

- Custom event listener providers
- PostgreSQL database integration
- Health and metrics endpoints enabled
- Kubernetes-ready with horizontal scaling
- Production-ready Docker images
- Automated CI/CD pipelines

## Quick Links

- [Installation Guide](getting-started/installation.md)
- [Quick Start](getting-started/quick-start.md)
- [Custom Providers](development/custom-providers.md)
- [Kubernetes Deployment](deployment/kubernetes.md)

## Architecture

```
┌─────────────────┐
│   Load Balancer │
│    (Ingress)    │
└────────┬────────┘
         │
    ┌────▼────┐
    │ Keycloak│
    │ Service │
    └────┬────┘
         │
    ┌────▼────────┐
    │  Keycloak   │
    │   Pods      │
    │ (Replicas)  │
    └────┬────────┘
         │
    ┌────▼────────┐
    │  PostgreSQL │
    │   Database  │
    └─────────────┘
```

## Technology Stack

- **Java 17** - Programming language
- **Keycloak 26.0.7** - OAuth/OIDC server
- **PostgreSQL 16** - Database
- **Maven** - Build tool
- **Docker** - Containerization
- **Kubernetes** - Orchestration
- **GitHub Actions** - CI/CD

## Getting Started

To get started with Keycloak Server, follow our [Installation Guide](getting-started/installation.md).

For a quick local setup:

```bash
# Clone the repository
git clone https://github.com/ianlintner/keycloak-server.git
cd keycloak-server

# Build the project
mvn clean package

# Start with Docker Compose
docker-compose up -d

# Access Keycloak
open http://localhost:8080
```

Default credentials: `admin` / `admin` (change in production!)

## Support

- 📖 [Documentation](https://ianlintner.github.io/keycloak-server)
- 🐛 [Issue Tracker](https://github.com/ianlintner/keycloak-server/issues)
- 💬 [Discussions](https://github.com/ianlintner/keycloak-server/discussions)

## License

This project is licensed under the MIT License - see the LICENSE file for details.
