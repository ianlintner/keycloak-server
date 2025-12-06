# API Reference

This page provides reference documentation for custom APIs and providers.

## Custom Event Listener API

### CustomEventListenerProvider

Provider for handling Keycloak events.

#### Methods

##### `onEvent(Event event)`

Handles user events such as login, logout, register, etc.

**Parameters:**
- `event` (Event): The user event object

**Event Types:**
- `LOGIN` - User login
- `LOGOUT` - User logout
- `REGISTER` - User registration
- `UPDATE_EMAIL` - Email updated
- `UPDATE_PASSWORD` - Password changed
- `VERIFY_EMAIL` - Email verification

**Example:**
```java
@Override
public void onEvent(Event event) {
    if (event.getType() == EventType.LOGIN) {
        String userId = event.getUserId();
        String ipAddress = event.getIpAddress();
        // Handle login event
    }
}
```

##### `onEvent(AdminEvent event, boolean includeRepresentation)`

Handles admin events such as creating/updating users, realms, clients, etc.

**Parameters:**
- `event` (AdminEvent): The admin event object
- `includeRepresentation` (boolean): Whether to include resource representation

**Operation Types:**
- `CREATE` - Resource created
- `UPDATE` - Resource updated
- `DELETE` - Resource deleted
- `ACTION` - Action performed

**Example:**
```java
@Override
public void onEvent(AdminEvent event, boolean includeRepresentation) {
    if (event.getOperationType() == OperationType.CREATE) {
        String resourcePath = event.getResourcePath();
        // Handle creation event
    }
}
```

## Custom Provider Factory

### CustomEventListenerProviderFactory

Factory for creating provider instances.

#### Methods

##### `create(KeycloakSession session)`

Creates a new provider instance.

**Parameters:**
- `session` (KeycloakSession): The Keycloak session

**Returns:** EventListenerProvider instance

**Example:**
```java
@Override
public EventListenerProvider create(KeycloakSession session) {
    return new CustomEventListenerProvider(session);
}
```

##### `getId()`

Returns the unique provider ID.

**Returns:** String provider identifier

**Example:**
```java
@Override
public String getId() {
    return "custom-event-listener";
}
```

## Health Endpoints

### Health Check

**Endpoint:** `GET /health`

Returns overall health status.

**Response:**
```json
{
  "status": "UP",
  "checks": []
}
```

### Readiness Check

**Endpoint:** `GET /health/ready`

Indicates if the server is ready to accept requests.

**Response:**
```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Database connections health check",
      "status": "UP"
    }
  ]
}
```

### Liveness Check

**Endpoint:** `GET /health/live`

Indicates if the server is running.

**Response:**
```json
{
  "status": "UP",
  "checks": []
}
```

## Metrics Endpoint

### Prometheus Metrics

**Endpoint:** `GET /metrics`

Returns Prometheus-compatible metrics.

**Key Metrics:**
- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_threads_current` - Current thread count
- `http_server_requests_seconds` - HTTP request duration
- `keycloak_logins_total` - Total login count
- `keycloak_failed_logins_total` - Failed login count

**Example Output:**
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space"} 1.234567E8

# HELP keycloak_logins_total Total number of successful logins
# TYPE keycloak_logins_total counter
keycloak_logins_total{realm="myrealm"} 42
```

## Environment Variables Reference

### Database

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `KC_DB` | Database vendor | Yes | `postgres` |
| `KC_DB_URL` | JDBC URL | Yes | - |
| `KC_DB_USERNAME` | Database user | Yes | - |
| `KC_DB_PASSWORD` | Database password | Yes | - |
| `KC_DB_POOL_INITIAL_SIZE` | Initial pool size | No | `0` |
| `KC_DB_POOL_MIN_SIZE` | Minimum pool size | No | `0` |
| `KC_DB_POOL_MAX_SIZE` | Maximum pool size | No | `100` |

### Server

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `KC_HOSTNAME` | Public hostname | No | - |
| `KC_HOSTNAME_STRICT` | Enforce hostname | No | `true` |
| `KC_HTTP_ENABLED` | Enable HTTP | No | `false` |
| `KC_HTTP_PORT` | HTTP port | No | `8080` |
| `KC_HTTPS_PORT` | HTTPS port | No | `8443` |
| `KC_PROXY` | Proxy mode | No | `none` |

### Clustering

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `KC_CACHE` | Cache type | No | `ispn` |
| `KC_CACHE_STACK` | Cache stack | No | `udp` |
| `JGROUPS_DISCOVERY_PROTOCOL` | Discovery protocol | No | `dns.DNS_PING` |

### Monitoring

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `KC_HEALTH_ENABLED` | Enable health | No | `false` |
| `KC_METRICS_ENABLED` | Enable metrics | No | `false` |
| `KC_LOG_LEVEL` | Log level | No | `info` |

## Error Codes

### Common Error Codes

| Code | Description |
|------|-------------|
| `invalid_token` | The provided token is invalid |
| `invalid_grant` | The authorization grant is invalid |
| `unauthorized_client` | Client authentication failed |
| `invalid_client` | Invalid client credentials |
| `unsupported_grant_type` | Grant type not supported |

## Configuration File Reference

### keycloak.conf

Example configuration file:

```properties
# Database
db=postgres
db-url=jdbc:postgresql://localhost/keycloak
db-username=keycloak
db-password=password

# Hostname
hostname=keycloak.example.com
hostname-strict=false

# HTTP
http-enabled=true
http-port=8080

# HTTPS
https-port=8443
https-certificate-file=/path/to/cert.pem
https-certificate-key-file=/path/to/key.pem

# Proxy
proxy=edge

# Clustering
cache=ispn
cache-stack=kubernetes

# Monitoring
health-enabled=true
metrics-enabled=true
```

## See Also

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/)
- [Custom Providers Guide](development/custom-providers.md)
