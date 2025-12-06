# Backup and Restore

Guide for backing up and restoring Keycloak Server data.

## Backup Strategy

### What to Backup

1. **Database** - All Keycloak configuration and user data
2. **Configuration files** - Custom configurations
3. **Custom themes** - UI customizations
4. **Custom providers** - JAR files

### Backup Frequency

| Data Type | Frequency | Retention |
|-----------|-----------|-----------|
| Database | Daily | 30 days |
| Weekly full | Weekly | 90 days |
| Configuration | On change | Indefinite |
| Providers | On change | Indefinite |

## Database Backup

### Manual Backup

```bash
# PostgreSQL backup
kubectl exec -n keycloak deployment/postgres -- \
  pg_dump -U keycloak keycloak > keycloak-backup-$(date +%Y%m%d).sql

# Compressed backup
kubectl exec -n keycloak deployment/postgres -- \
  pg_dump -U keycloak keycloak | gzip > keycloak-backup-$(date +%Y%m%d).sql.gz
```

### Automated Backup with CronJob

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: keycloak-backup
  namespace: keycloak
spec:
  schedule: "0 2 * * *"  # 2 AM daily
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
              BACKUP_FILE="/backup/keycloak-${TIMESTAMP}.sql.gz"
              
              echo "Starting backup at $(date)"
              pg_dump -h postgres -U keycloak keycloak | gzip > ${BACKUP_FILE}
              
              if [ $? -eq 0 ]; then
                echo "Backup completed successfully: ${BACKUP_FILE}"
                # Upload to S3 (if configured)
                # aws s3 cp ${BACKUP_FILE} s3://backups/keycloak/
              else
                echo "Backup failed!"
                exit 1
              fi
            volumeMounts:
            - name: backup
              mountPath: /backup
          restartPolicy: OnFailure
          volumes:
          - name: backup
            persistentVolumeClaim:
              claimName: backup-pvc
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: backup-pvc
  namespace: keycloak
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 50Gi
```

### Backup to Cloud Storage

#### AWS S3

```bash
# Install AWS CLI in backup container
apk add --no-cache aws-cli

# Backup and upload
pg_dump -h postgres -U keycloak keycloak | gzip | \
  aws s3 cp - s3://my-backups/keycloak/backup-$(date +%Y%m%d).sql.gz
```

#### Google Cloud Storage

```bash
# Install gsutil
pip install gsutil

# Backup and upload
pg_dump -h postgres -U keycloak keycloak | gzip | \
  gsutil cp - gs://my-backups/keycloak/backup-$(date +%Y%m%d).sql.gz
```

#### Azure Blob Storage

```bash
# Install Azure CLI
pip install azure-cli

# Backup and upload
pg_dump -h postgres -U keycloak keycloak | gzip | \
  az storage blob upload \
    --account-name mystorageaccount \
    --container-name backups \
    --name keycloak/backup-$(date +%Y%m%d).sql.gz \
    --file -
```

## Restore Database

### From SQL File

```bash
# Restore from uncompressed backup
kubectl exec -i -n keycloak deployment/postgres -- \
  psql -U keycloak keycloak < keycloak-backup-20240101.sql

# Restore from compressed backup
gunzip < keycloak-backup-20240101.sql.gz | \
  kubectl exec -i -n keycloak deployment/postgres -- \
  psql -U keycloak keycloak
```

### From Cloud Storage

```bash
# From S3
aws s3 cp s3://my-backups/keycloak/backup-20240101.sql.gz - | \
  gunzip | \
  kubectl exec -i -n keycloak deployment/postgres -- \
  psql -U keycloak keycloak

# From GCS
gsutil cp gs://my-backups/keycloak/backup-20240101.sql.gz - | \
  gunzip | \
  kubectl exec -i -n keycloak deployment/postgres -- \
  psql -U keycloak keycloak
```

## Realm Export/Import

### Export Realm

```bash
# Via Admin Console
# 1. Login to admin console
# 2. Select realm
# 3. Go to "Realm settings" → "Action" → "Partial export"
# 4. Select what to export
# 5. Download JSON file

# Via CLI
kubectl exec -it <keycloak-pod> -n keycloak -- \
  /opt/keycloak/bin/kc.sh export \
  --dir /tmp/export \
  --realm myrealm
```

### Import Realm

```bash
# Via Admin Console
# 1. Login to admin console
# 2. Click "Create realm"
# 3. Click "Browse" and select JSON file
# 4. Click "Create"

# Via CLI at startup
kubectl exec -it <keycloak-pod> -n keycloak -- \
  /opt/keycloak/bin/kc.sh import \
  --file /tmp/export/myrealm.json
```

## Disaster Recovery

### Recovery Procedure

1. **Prepare new environment**
```bash
# Deploy new Keycloak instance
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres-pvc.yaml
kubectl apply -f k8s/postgres-deployment.yaml
```

2. **Wait for database**
```bash
kubectl wait --for=condition=ready pod -l app=postgres -n keycloak --timeout=300s
```

3. **Restore database**
```bash
# Download backup
aws s3 cp s3://my-backups/keycloak/latest.sql.gz latest.sql.gz

# Restore
gunzip < latest.sql.gz | \
  kubectl exec -i -n keycloak deployment/postgres -- \
  psql -U keycloak keycloak
```

4. **Deploy Keycloak**
```bash
kubectl apply -f k8s/keycloak-deployment.yaml
kubectl apply -f k8s/keycloak-service.yaml
kubectl apply -f k8s/keycloak-ingress.yaml
```

5. **Verify**
```bash
kubectl get pods -n keycloak
curl https://keycloak.example.com/health/ready
```

### Recovery Time Objective (RTO)

| Scenario | Target RTO |
|----------|------------|
| Database failure | < 30 minutes |
| Pod failure | < 5 minutes (automatic) |
| Node failure | < 10 minutes (automatic) |
| Complete cluster failure | < 2 hours |

### Recovery Point Objective (RPO)

| Data Type | Target RPO |
|-----------|------------|
| Database | < 24 hours |
| Configuration | Real-time (version control) |

## Backup Verification

### Regular Testing

```bash
#!/bin/bash
# test-restore.sh

# 1. Create test namespace
kubectl create namespace keycloak-test

# 2. Deploy database
kubectl apply -f k8s/postgres-deployment.yaml -n keycloak-test

# 3. Restore backup
gunzip < latest-backup.sql.gz | \
  kubectl exec -i -n keycloak-test deployment/postgres -- \
  psql -U keycloak keycloak

# 4. Verify data
kubectl exec -n keycloak-test deployment/postgres -- \
  psql -U keycloak keycloak -c "SELECT count(*) FROM realm;"

# 5. Cleanup
kubectl delete namespace keycloak-test
```

### Backup Monitoring

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: backup-alerts
  namespace: keycloak
spec:
  groups:
  - name: backup
    interval: 1h
    rules:
    - alert: BackupFailed
      expr: kube_job_status_failed{job="keycloak-backup"} > 0
      for: 1h
      labels:
        severity: critical
      annotations:
        summary: Keycloak backup failed
        description: "Backup job has failed"
    
    - alert: BackupMissing
      expr: time() - kube_job_status_completion_time{job="keycloak-backup"} > 86400
      for: 1h
      labels:
        severity: warning
      annotations:
        summary: No recent backup
        description: "No successful backup in last 24 hours"
```

## Backup Best Practices

1. **3-2-1 Rule**
   - 3 copies of data
   - 2 different storage types
   - 1 off-site copy

2. **Encryption**
   - Encrypt backups at rest
   - Use encrypted connections for transfer

3. **Testing**
   - Test restores monthly
   - Document procedures
   - Train team

4. **Retention**
   - Keep daily backups for 30 days
   - Keep weekly backups for 90 days
   - Keep monthly backups for 1 year

5. **Monitoring**
   - Alert on backup failures
   - Track backup sizes
   - Monitor storage usage

## Backup Checklist

- [ ] Automated daily backups configured
- [ ] Backups uploaded to off-site storage
- [ ] Backup encryption enabled
- [ ] Restore procedure documented
- [ ] Regular restore testing scheduled
- [ ] Backup monitoring and alerts configured
- [ ] Retention policy implemented
- [ ] Team trained on restore procedures

## Next Steps

- [Monitoring Guide](monitoring.md)
- [Troubleshooting Guide](troubleshooting.md)
- [Production Deployment](../deployment/production.md)
