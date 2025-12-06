# Kubernetes Deployment

Deploy Keycloak Server to Kubernetes.

## Prerequisites

- Kubernetes cluster (1.24+)
- kubectl configured
- Helm 3.x (optional)

## Quick Deployment

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check deployment
kubectl get pods -n keycloak
kubectl get services -n keycloak
```

## Deployment Steps

### 1. Create Namespace

```bash
kubectl apply -f k8s/namespace.yaml
```

### 2. Create Secrets

```bash
kubectl apply -f k8s/secrets.yaml
```

!!! warning
    Update secrets with strong passwords before deploying to production!

### 3. Create ConfigMap

```bash
kubectl apply -f k8s/configmap.yaml
```

### 4. Deploy Database

```bash
kubectl apply -f k8s/postgres-pvc.yaml
kubectl apply -f k8s/postgres-deployment.yaml
```

Wait for PostgreSQL to be ready:

```bash
kubectl wait --for=condition=ready pod -l app=postgres -n keycloak --timeout=300s
```

### 5. Deploy Keycloak

```bash
kubectl apply -f k8s/keycloak-deployment.yaml
kubectl apply -f k8s/keycloak-service.yaml
```

Wait for Keycloak to be ready:

```bash
kubectl wait --for=condition=ready pod -l app=keycloak -n keycloak --timeout=600s
```

### 6. Create Ingress (Optional)

```bash
kubectl apply -f k8s/keycloak-ingress.yaml
```

## Architecture

```
┌─────────────────────────────────────┐
│           Ingress Controller         │
│         (nginx/traefik/etc)          │
└────────────────┬────────────────────┘
                 │
    ┌────────────▼────────────┐
    │    Keycloak Service     │
    │     (LoadBalancer)      │
    └────────────┬────────────┘
                 │
    ┌────────────▼────────────┐
    │    Keycloak Pods        │
    │   (Replicas: 2)         │
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

## Configuration

### Update Secrets

Edit `k8s/secrets.yaml`:

```yaml
stringData:
  POSTGRES_PASSWORD: your-secure-password
  KC_DB_PASSWORD: your-secure-password
  KEYCLOAK_ADMIN_PASSWORD: your-admin-password
```

### Update ConfigMap

Edit `k8s/configmap.yaml`:

```yaml
data:
  KC_HOSTNAME: keycloak.yourdomain.com
  KC_PROXY: edge
```

### Update Ingress

Edit `k8s/keycloak-ingress.yaml`:

```yaml
spec:
  tls:
  - hosts:
    - keycloak.yourdomain.com
  rules:
  - host: keycloak.yourdomain.com
```

## Scaling

### Horizontal Scaling

```bash
# Scale to 3 replicas
kubectl scale deployment keycloak -n keycloak --replicas=3

# Auto-scaling
kubectl autoscale deployment keycloak -n keycloak \
  --cpu-percent=70 \
  --min=2 \
  --max=10
```

### Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: keycloak-hpa
  namespace: keycloak
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: keycloak
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

## Monitoring

### Check Status

```bash
# Pod status
kubectl get pods -n keycloak

# Service endpoints
kubectl get endpoints -n keycloak

# Logs
kubectl logs -f deployment/keycloak -n keycloak
```

### Health Checks

```bash
# Check readiness
kubectl get pods -n keycloak -o wide

# Port forward for testing
kubectl port-forward -n keycloak svc/keycloak 8080:8080

# Test health endpoint
curl http://localhost:8080/health/ready
```

## Storage

### PostgreSQL PVC

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: keycloak
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
  storageClassName: standard
```

### Storage Classes

For production, use appropriate storage class:

- AWS: `gp3`, `io1`
- GCP: `pd-ssd`, `pd-balanced`
- Azure: `managed-premium`

## Security

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
```

### Pod Security

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 1000
  seccompProfile:
    type: RuntimeDefault
```

## Updates

### Rolling Update

```bash
# Update image
kubectl set image deployment/keycloak \
  keycloak=ianlintner/keycloak-server:new-version \
  -n keycloak

# Check rollout status
kubectl rollout status deployment/keycloak -n keycloak
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/keycloak -n keycloak

# Rollback to specific revision
kubectl rollout undo deployment/keycloak -n keycloak --to-revision=2
```

## Backup

### Database Backup

```bash
# Create backup job
kubectl create job --from=cronjob/postgres-backup postgres-backup-manual -n keycloak

# Manual backup
kubectl exec -n keycloak deployment/postgres -- \
  pg_dump -U keycloak keycloak > backup-$(date +%Y%m%d).sql
```

### Backup CronJob

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
  namespace: keycloak
spec:
  schedule: "0 2 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:16-alpine
            command:
            - /bin/sh
            - -c
            - pg_dump -U keycloak -h postgres keycloak > /backup/backup-$(date +%Y%m%d).sql
            volumeMounts:
            - name: backup
              mountPath: /backup
          restartPolicy: OnFailure
          volumes:
          - name: backup
            persistentVolumeClaim:
              claimName: backup-pvc
```

## Troubleshooting

### Pod Not Starting

```bash
# Describe pod
kubectl describe pod -n keycloak -l app=keycloak

# Check events
kubectl get events -n keycloak --sort-by='.lastTimestamp'

# Check logs
kubectl logs -n keycloak -l app=keycloak --previous
```

### Database Connection Issues

```bash
# Test database connectivity
kubectl exec -n keycloak deployment/keycloak -- \
  psql -h postgres -U keycloak -c "SELECT 1"
```

### Performance Issues

```bash
# Check resource usage
kubectl top pods -n keycloak

# Check node resources
kubectl top nodes
```

## Production Checklist

- [ ] Update all secrets
- [ ] Configure TLS/SSL
- [ ] Set resource limits
- [ ] Configure HPA
- [ ] Set up monitoring
- [ ] Configure backups
- [ ] Implement network policies
- [ ] Configure pod disruption budget
- [ ] Set up logging
- [ ] Configure alerts

## Next Steps

- [Production Deployment](production.md)
- [Monitoring Setup](../operations/monitoring.md)
- [Backup Strategy](../operations/backup.md)
