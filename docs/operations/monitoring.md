# Monitoring

Comprehensive guide to monitoring Keycloak Server.

## Metrics Collection

### Prometheus Integration

Keycloak exposes metrics in Prometheus format at `/metrics`.

#### Enable Metrics

```yaml
env:
- name: KC_METRICS_ENABLED
  value: "true"
```

#### Key Metrics

**JVM Metrics:**
- `jvm_memory_used_bytes` - Memory usage
- `jvm_memory_max_bytes` - Maximum memory
- `jvm_threads_current` - Active threads
- `jvm_gc_pause_seconds` - GC pause duration

**HTTP Metrics:**
- `http_server_requests_seconds` - Request duration
- `http_server_requests_total` - Request count

**Keycloak Metrics:**
- `keycloak_logins_total` - Successful logins
- `keycloak_failed_logins_total` - Failed logins
- `keycloak_registrations_total` - User registrations
- `keycloak_sessions_active` - Active sessions

### Prometheus Configuration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: monitoring
data:
  prometheus.yml: |
    scrape_configs:
    - job_name: 'keycloak'
      kubernetes_sd_configs:
      - role: endpoints
        namespaces:
          names:
          - keycloak
      relabel_configs:
      - source_labels: [__meta_kubernetes_service_name]
        action: keep
        regex: keycloak
```

### ServiceMonitor

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: keycloak
  namespace: keycloak
spec:
  selector:
    matchLabels:
      app: keycloak
  endpoints:
  - port: http
    path: /metrics
    interval: 30s
```

## Grafana Dashboards

### Import Dashboard

1. Open Grafana
2. Go to Dashboards → Import
3. Enter dashboard ID: `10441` (Keycloak metrics)
4. Select Prometheus data source
5. Click Import

### Custom Dashboard

Example dashboard panels:

**Panel 1: Active Sessions**
```promql
keycloak_sessions_active{realm="myrealm"}
```

**Panel 2: Login Rate**
```promql
rate(keycloak_logins_total[5m])
```

**Panel 3: Failed Logins**
```promql
rate(keycloak_failed_logins_total[5m])
```

**Panel 4: Response Time**
```promql
histogram_quantile(0.95, 
  rate(http_server_requests_seconds_bucket[5m])
)
```

## Logging

### Log Levels

```yaml
env:
- name: KC_LOG_LEVEL
  value: INFO  # DEBUG, INFO, WARN, ERROR
```

### JSON Logging

```yaml
env:
- name: KC_LOG_CONSOLE_OUTPUT
  value: json
```

### ELK Stack Integration

#### Filebeat Configuration

```yaml
filebeat.inputs:
- type: container
  paths:
    - /var/log/containers/keycloak-*.log
  processors:
  - add_kubernetes_metadata:
      host: ${NODE_NAME}
      matchers:
      - logs_path:
          logs_path: "/var/log/containers/"

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
```

#### Logstash Pipeline

```
input {
  beats {
    port => 5044
  }
}

filter {
  if [kubernetes][container][name] == "keycloak" {
    json {
      source => "message"
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "keycloak-%{+YYYY.MM.dd}"
  }
}
```

### Loki Integration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: promtail-config
data:
  promtail.yaml: |
    server:
      http_listen_port: 9080
    
    clients:
      - url: http://loki:3100/loki/api/v1/push
    
    scrape_configs:
      - job_name: kubernetes-pods
        kubernetes_sd_configs:
          - role: pod
        relabel_configs:
          - source_labels: [__meta_kubernetes_namespace]
            target_label: namespace
          - source_labels: [__meta_kubernetes_pod_name]
            target_label: pod
```

## Alerting

### Prometheus Alerts

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: keycloak-alerts
  namespace: keycloak
spec:
  groups:
  - name: keycloak
    interval: 30s
    rules:
    - alert: KeycloakHighFailedLogins
      expr: rate(keycloak_failed_logins_total[5m]) > 10
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: High failed login rate
        description: "Failed login rate is {{ $value }} per second"
    
    - alert: KeycloakDown
      expr: up{job="keycloak"} == 0
      for: 1m
      labels:
        severity: critical
      annotations:
        summary: Keycloak is down
        description: "Keycloak has been down for more than 1 minute"
    
    - alert: KeycloakHighMemory
      expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: High memory usage
        description: "Memory usage is {{ $value | humanizePercentage }}"
    
    - alert: KeycloakSlowRequests
      expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: Slow request response time
        description: "95th percentile response time is {{ $value }}s"
```

### AlertManager Configuration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: alertmanager-config
  namespace: monitoring
data:
  alertmanager.yml: |
    global:
      resolve_timeout: 5m
    
    route:
      group_by: ['alertname', 'cluster']
      group_wait: 10s
      group_interval: 10s
      repeat_interval: 12h
      receiver: 'default'
      routes:
      - match:
          severity: critical
        receiver: 'critical'
    
    receivers:
    - name: 'default'
      slack_configs:
      - api_url: 'YOUR_SLACK_WEBHOOK'
        channel: '#alerts'
    
    - name: 'critical'
      slack_configs:
      - api_url: 'YOUR_SLACK_WEBHOOK'
        channel: '#critical-alerts'
      pagerduty_configs:
      - service_key: 'YOUR_PAGERDUTY_KEY'
```

## Health Checks

### Kubernetes Probes

```yaml
livenessProbe:
  httpGet:
    path: /health/live
    port: 8080
  initialDelaySeconds: 90
  periodSeconds: 10
  
readinessProbe:
  httpGet:
    path: /health/ready
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
```

### Health Check Endpoints

**Liveness:** `GET /health/live`
- Checks if application is running
- Returns 200 if alive

**Readiness:** `GET /health/ready`
- Checks if application is ready to serve traffic
- Checks database connectivity
- Returns 200 if ready

**Overall Health:** `GET /health`
- Comprehensive health check
- Returns status of all components

## Application Performance Monitoring

### Datadog Integration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: datadog-config
data:
  keycloak.yaml: |
    init_config:
    instances:
      - prometheus_url: http://keycloak:8080/metrics
        namespace: keycloak
        metrics:
          - keycloak_*
          - jvm_*
          - http_*
```

### New Relic Integration

```yaml
env:
- name: NEW_RELIC_APP_NAME
  value: "Keycloak Server"
- name: NEW_RELIC_LICENSE_KEY
  valueFrom:
    secretKeyRef:
      name: newrelic-secret
      key: license-key
```

## Tracing

### Jaeger Integration

```yaml
env:
- name: KC_TRACING_ENABLED
  value: "true"
- name: KC_TRACING_ENDPOINT
  value: "http://jaeger-collector:14268/api/traces"
```

## Dashboard Examples

### Query Examples

**Active Users:**
```promql
count(keycloak_sessions_active) by (realm)
```

**Login Success Rate:**
```promql
rate(keycloak_logins_total[5m]) / 
(rate(keycloak_logins_total[5m]) + rate(keycloak_failed_logins_total[5m]))
```

**Average Response Time:**
```promql
rate(http_server_requests_seconds_sum[5m]) / 
rate(http_server_requests_seconds_count[5m])
```

**Database Connection Pool:**
```promql
hikaricp_connections_active{pool="keycloak"}
```

## Next Steps

- [Troubleshooting Guide](troubleshooting.md)
- [Backup Guide](backup.md)
- [Production Deployment](../deployment/production.md)
