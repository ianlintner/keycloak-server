# Building

This guide covers building the Keycloak Server from source.

## Prerequisites

- JDK 17 or higher
- Maven 3.8 or higher
- Git

## Building from Source

### Clone the Repository

```bash
git clone https://github.com/ianlintner/keycloak-server.git
cd keycloak-server
```

### Build with Maven

```bash
# Clean build
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Build with specific profile
mvn clean package -P production
```

### Build Output

The build produces:
- `target/keycloak-server-1.0.0-SNAPSHOT.jar` - Custom provider JAR
- `target/classes/` - Compiled classes
- `target/test-classes/` - Compiled test classes

## Development Build

For development with fast iteration:

```bash
# Install to local Maven repository
mvn clean install

# Continuous build
mvn compile -Dmaven.compiler.fork=true
```

## Docker Build

### Build Docker Image

```bash
# Build the JAR first
mvn clean package

# Build Docker image
docker build -t keycloak-server:latest .

# Build with tag
docker build -t keycloak-server:1.0.0 .
```

### Multi-stage Build

The Dockerfile uses multi-stage builds for optimization:

```dockerfile
# Stage 1: Builder
FROM quay.io/keycloak/keycloak:26.4.7 as builder
# ... build steps

# Stage 2: Runtime
FROM quay.io/keycloak/keycloak:26.4.7
# ... runtime setup
```

## Build Profiles

### Default Profile

```bash
mvn clean package
```

### Production Profile

```bash
mvn clean package -P production
```

### Debug Profile

```bash
mvn clean package -P debug
```

## IDE Setup

### IntelliJ IDEA

1. Open the project: File → Open → Select `pom.xml`
2. Wait for Maven import
3. Set JDK to 17: File → Project Structure → Project SDK
4. Run tests: Right-click on test class → Run

### Eclipse

1. Import Maven project: File → Import → Existing Maven Projects
2. Select the project directory
3. Wait for workspace build
4. Run tests: Right-click on test class → Run As → JUnit Test

### VS Code

1. Install Java Extension Pack
2. Open the project folder
3. Maven will auto-import
4. Run tests from Testing view

## Build Troubleshooting

### Maven Dependency Issues

```bash
# Force update
mvn clean package -U

# Clear local repository
rm -rf ~/.m2/repository/com/ianlintner
mvn clean package
```

### Java Version Issues

```bash
# Check Java version
java -version

# Use specific Java version (macOS/Linux)
export JAVA_HOME=/path/to/jdk-17
mvn clean package
```

### Memory Issues

```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"
mvn clean package
```

## Next Steps

- [Custom Providers](custom-providers.md)
- [Testing Guide](testing.md)
- [Docker Deployment](../deployment/docker.md)
