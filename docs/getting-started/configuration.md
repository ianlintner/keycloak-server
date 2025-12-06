# Configuration

This guide covers configuration options for Keycloak Server.

## Environment Variables

### Database Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `KC_DB` | Database type | `postgres` |
| `KC_DB_URL` | JDBC connection URL | `jdbc:postgresql://postgres:5432/keycloak` |
| `KC_DB_USERNAME` | Database username | `keycloak` |
| `KC_DB_PASSWORD` | Database password | `keycloak` |

### Server Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `KC_HOSTNAME` | Public hostname | `localhost` |
| `KC_HOSTNAME_STRICT` | Enforce hostname | `false` |
| `KC_HOSTNAME_STRICT_HTTPS` | Enforce HTTPS | `false` |
| `KC_HTTP_ENABLED` | Enable HTTP | `true` |
| `KC_PROXY` | Proxy mode | `edge` |

### Admin Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `KEYCLOAK_ADMIN` | Admin username | `admin` |
| `KEYCLOAK_ADMIN_PASSWORD` | Admin password | `admin` |

### Monitoring

| Variable | Description | Default |
|----------|-------------|---------|
| `KC_HEALTH_ENABLED` | Enable health checks | `true` |
| `KC_METRICS_ENABLED` | Enable metrics | `true` |

## Docker Compose Configuration

Edit `docker-compose.yml` to customize:

```yaml
environment:
  KC_DB: postgres
  KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
  KC_DB_USERNAME: keycloak
  KC_DB_PASSWORD: changeme
  KEYCLOAK_ADMIN: admin
  KEYCLOAK_ADMIN_PASSWORD: changeme
```

## Kubernetes Configuration

### Using ConfigMap

Edit `k8s/configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: keycloak-config
  namespace: keycloak
data:
  KC_DB: postgres
  KC_HOSTNAME: keycloak.example.com
  KC_PROXY: edge
```

### Using Secrets

Edit `k8s/secrets.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: keycloak-db-secret
  namespace: keycloak
type: Opaque
stringData:
  KC_DB_USERNAME: keycloak
  KC_DB_PASSWORD: your-secure-password
```

!!! warning
    Always use strong passwords in production and manage secrets properly!

## Custom Provider Configuration

Custom providers can be configured through:

1. **Environment variables**
2. **Keycloak admin console**
3. **Provider-specific configuration files**

Example configuration in Admin Console:
1. Go to Realm Settings
2. Navigate to Events tab
3. Select your custom event listener
4. Configure as needed

## TLS/SSL Configuration

For production, enable TLS:

```yaml
env:
  - name: KC_HTTPS_CERTIFICATE_FILE
    value: /opt/keycloak/conf/server.crt.pem
  - name: KC_HTTPS_CERTIFICATE_KEY_FILE
    value: /opt/keycloak/conf/server.key.pem
```

Mount certificates:
```yaml
volumeMounts:
  - name: tls-certs
    mountPath: /opt/keycloak/conf
volumes:
  - name: tls-certs
    secret:
      secretName: keycloak-tls
```

## Performance Tuning

### JVM Options

```yaml
env:
  - name: JAVA_OPTS_APPEND
    value: "-Xms512m -Xmx2048m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m"
```

### Database Connection Pool

```yaml
env:
  - name: KC_DB_POOL_INITIAL_SIZE
    value: "10"
  - name: KC_DB_POOL_MAX_SIZE
    value: "50"
  - name: KC_DB_POOL_MIN_SIZE
    value: "10"
```

## Next Steps

- [Development Guide](../development/building.md)
- [Deployment Guide](../deployment/kubernetes.md)
- [Monitoring Setup](../operations/monitoring.md)
