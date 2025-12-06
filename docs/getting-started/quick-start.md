# Quick Start

Get up and running with Keycloak Server in minutes.

## Quick Start with Docker Compose

The fastest way to get started is using Docker Compose:

```bash
# Clone the repository
git clone https://github.com/ianlintner/keycloak-server.git
cd keycloak-server

# Build the custom providers
mvn clean package

# Start all services
docker-compose up -d

# Check the logs
docker-compose logs -f keycloak
```

Wait for Keycloak to start (about 30-60 seconds), then access:
- **Admin Console**: http://localhost:8080
- **Username**: admin
- **Password**: admin

## First Steps

### 1. Login to Admin Console

Navigate to http://localhost:8080 and login with `admin` / `admin`.

### 2. Create a Realm

1. Click on "Create Realm" in the dropdown
2. Enter a realm name (e.g., `myrealm`)
3. Click "Create"

### 3. Create a Client

1. Go to "Clients" in the left menu
2. Click "Create client"
3. Enter Client ID (e.g., `myclient`)
4. Click "Next" and configure as needed
5. Click "Save"

### 4. Create a User

1. Go to "Users" in the left menu
2. Click "Create new user"
3. Fill in username and other details
4. Click "Create"
5. Go to "Credentials" tab
6. Set a password for the user

### 5. Test Authentication

You can now test authentication using your newly created user.

## Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn verify
```

## Building Custom Providers

```bash
# Build the project
mvn clean package

# The JAR will be in target/
ls -lh target/*.jar
```

## Stopping Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Next Steps

- [Configuration Guide](configuration.md)
- [Custom Providers](../development/custom-providers.md)
- [Kubernetes Deployment](../deployment/kubernetes.md)

## Troubleshooting

### Keycloak won't start
- Check if ports 8080 or 5432 are already in use
- Check Docker logs: `docker-compose logs keycloak`
- Ensure PostgreSQL is healthy: `docker-compose ps`

### Can't access admin console
- Verify Keycloak is running: `docker-compose ps`
- Check if service is ready: `curl http://localhost:8080/health/ready`
- Review logs for errors

### Database connection issues
- Verify PostgreSQL is running
- Check database credentials in docker-compose.yml
- Ensure network connectivity between containers
