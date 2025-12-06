# Production Deployment

Best practices for deploying Keycloak Server to production.

## Production Requirements

### Infrastructure
- High availability setup (minimum 2 replicas)
- Load balancer with sticky sessions
- Database with replication
- SSL/TLS certificates
- Backup and disaster recovery plan

### Security
- Strong passwords
- Secret management (Vault, sealed secrets)
- Network policies
- Regular security updates
- Audit logging

## High Availability Setup

### Multiple Replicas

```yaml
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
```

### Pod Disruption Budget

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: keycloak-pdb
  namespace: keycloak
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: keycloak
```

### Anti-Affinity

```yaml
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
    - labelSelector:
        matchExpressions:
        - key: app
          operator: In
          values:
          - keycloak
      topologyKey: kubernetes.io/hostname
```

## Database Configuration

### PostgreSQL High Availability

Use managed database services:
- **AWS**: RDS PostgreSQL with Multi-AZ
- **GCP**: Cloud SQL with HA
- **Azure**: Azure Database for PostgreSQL with HA

### Connection Pool

```yaml
env:
- name: KC_DB_POOL_MIN_SIZE
  value: "10"
- name: KC_DB_POOL_MAX_SIZE
  value: "50"
- name: KC_DB_POOL_INITIAL_SIZE
  value: "10"
```

## SSL/TLS Configuration

### Using cert-manager

```bash
# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Create ClusterIssuer
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

### Ingress with TLS

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: keycloak-ingress
  namespace: keycloak
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - keycloak.example.com
    secretName: keycloak-tls
  rules:
  - host: keycloak.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: keycloak
            port:
              number: 8080
```

## Secret Management

### Using Sealed Secrets

```bash
# Install sealed-secrets
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.0/controller.yaml

# Create sealed secret
kubectl create secret generic keycloak-secrets \
  --from-literal=admin-password=secretpassword \
  --dry-run=client -o yaml | \
  kubeseal -o yaml > sealed-secret.yaml

# Apply sealed secret
kubectl apply -f sealed-secret.yaml
```

### Using External Secrets Operator

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: keycloak-secrets
  namespace: keycloak
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: vault-backend
    kind: SecretStore
  target:
    name: keycloak-secrets
  data:
  - secretKey: admin-password
    remoteRef:
      key: secret/keycloak
      property: admin-password
```

## Resource Management

### Resource Limits

```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "2000m"
```

### Quality of Service

Set guaranteed QoS by making requests == limits:

```yaml
resources:
  requests:
    memory: "2Gi"
    cpu: "1000m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

## Monitoring and Observability

### Prometheus Integration

```yaml
apiVersion: v1
kind: Service
metadata:
  name: keycloak-metrics
  namespace: keycloak
  labels:
    app: keycloak
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    prometheus.io/path: "/metrics"
spec:
  selector:
    app: keycloak
  ports:
  - name: metrics
    port: 8080
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

## Performance Tuning

### JVM Options

```yaml
env:
- name: JAVA_OPTS_APPEND
  value: >-
    -Xms2048m
    -Xmx2048m
    -XX:MetaspaceSize=256m
    -XX:MaxMetaspaceSize=512m
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=500
    -XX:+DisableExplicitGC
```

### Caching

```yaml
env:
- name: KC_CACHE
  value: ispn
- name: KC_CACHE_STACK
  value: kubernetes
```

## Backup Strategy

### Automated Backups

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: keycloak-backup
  namespace: keycloak
spec:
  schedule: "0 2 * * *"
  successfulJobsHistoryLimit: 3
  failedJobsHistoryLimit: 1
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:16-alpine
            env:
            - name: PGPASSWORD
              valueFrom:
                secretKeyRef:
                  name: keycloak-db-secret
                  key: POSTGRES_PASSWORD
            command:
            - /bin/sh
            - -c
            - |
              TIMESTAMP=$(date +%Y%m%d_%H%M%S)
              pg_dump -h postgres -U keycloak keycloak | \
              gzip > /backup/keycloak-${TIMESTAMP}.sql.gz
              # Upload to S3/GCS
              aws s3 cp /backup/keycloak-${TIMESTAMP}.sql.gz s3://backups/
            volumeMounts:
            - name: backup
              mountPath: /backup
          restartPolicy: OnFailure
          volumes:
          - name: backup
            emptyDir: {}
```

## Disaster Recovery

### Backup Restoration

```bash
# Download backup
aws s3 cp s3://backups/keycloak-20240101_020000.sql.gz .

# Restore to database
gunzip < keycloak-20240101_020000.sql.gz | \
  kubectl exec -i -n keycloak deployment/postgres -- \
  psql -U keycloak keycloak
```

### Regular Testing

- Test restores monthly
- Document recovery procedures
- Maintain runbooks
- Train team on recovery process

## Security Hardening

### Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: keycloak-network-policy
  namespace: keycloak
spec:
  podSelector:
    matchLabels:
      app: keycloak
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: postgres
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - namespaceSelector: {}
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
```

### Pod Security Standards

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: keycloak
  labels:
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/audit: restricted
    pod-security.kubernetes.io/warn: restricted
```

## Compliance

### Audit Logging

```yaml
env:
- name: KC_LOG_LEVEL
  value: INFO
- name: KC_LOG_CONSOLE_OUTPUT
  value: json
```

### GDPR Compliance

- Enable user data export
- Implement data retention policies
- Configure consent management
- Document data processing

## Deployment Checklist

- [ ] High availability configured (min 2 replicas)
- [ ] Database HA setup with backups
- [ ] SSL/TLS certificates configured
- [ ] Strong passwords and secret management
- [ ] Resource limits set appropriately
- [ ] Monitoring and alerting configured
- [ ] Backup strategy implemented and tested
- [ ] Network policies applied
- [ ] Pod security standards enforced
- [ ] Disaster recovery plan documented
- [ ] Performance testing completed
- [ ] Security audit performed
- [ ] Runbooks created
- [ ] Team trained on operations

## Next Steps

- [Monitoring Guide](../operations/monitoring.md)
- [Backup Guide](../operations/backup.md)
- [Troubleshooting Guide](../operations/troubleshooting.md)
