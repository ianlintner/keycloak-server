# Troubleshooting

Common issues and solutions for Keycloak Server.

## Pod Issues

### Pod Not Starting

**Symptoms:**
- Pod stuck in `CrashLoopBackOff`
- Pod in `Pending` state

**Diagnosis:**
```bash
# Check pod status
kubectl get pods -n keycloak

# Describe pod
kubectl describe pod <pod-name> -n keycloak

# Check logs
kubectl logs <pod-name> -n keycloak
kubectl logs <pod-name> -n keycloak --previous
```

**Common Causes:**
1. Insufficient resources
2. Database connection issues
3. Configuration errors
4. Image pull errors

**Solutions:**

**Resource Issues:**
```bash
# Check node resources
kubectl top nodes
kubectl describe node <node-name>

# Increase resources
kubectl patch deployment keycloak -n keycloak -p '
{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "keycloak",
          "resources": {
            "requests": {"memory": "2Gi", "cpu": "1000m"},
            "limits": {"memory": "4Gi", "cpu": "2000m"}
          }
        }]
      }
    }
  }
}'
```

### Database Connection Errors

**Symptoms:**
- Error: "Could not connect to database"
- Pod keeps restarting

**Diagnosis:**
```bash
# Check database pod
kubectl get pods -n keycloak -l app=postgres

# Test database connection
kubectl exec -it <keycloak-pod> -n keycloak -- \
  psql -h postgres -U keycloak -c "SELECT 1"

# Check database logs
kubectl logs -n keycloak <postgres-pod>
```

**Solutions:**

1. **Verify credentials:**
```bash
kubectl get secret keycloak-db-secret -n keycloak -o yaml
```

2. **Check service:**
```bash
kubectl get svc postgres -n keycloak
kubectl get endpoints postgres -n keycloak
```

3. **Fix connection URL:**
```yaml
env:
- name: KC_DB_URL
  value: jdbc:postgresql://postgres.keycloak.svc.cluster.local:5432/keycloak
```

## Performance Issues

### Slow Response Times

**Diagnosis:**
```bash
# Check resource usage
kubectl top pods -n keycloak

# Check metrics
curl http://keycloak-service:8080/metrics | grep http_server_requests
```

**Solutions:**

1. **Increase JVM memory:**
```yaml
env:
- name: JAVA_OPTS_APPEND
  value: "-Xms2048m -Xmx4096m"
```

2. **Database connection pool:**
```yaml
env:
- name: KC_DB_POOL_MAX_SIZE
  value: "50"
```

3. **Enable caching:**
```yaml
env:
- name: KC_CACHE
  value: ispn
- name: KC_CACHE_STACK
  value: kubernetes
```

### High Memory Usage

**Diagnosis:**
```bash
# Check memory
kubectl top pod <keycloak-pod> -n keycloak

# Get heap dump
kubectl exec -it <keycloak-pod> -n keycloak -- \
  jmap -dump:format=b,file=/tmp/heap.hprof 1
```

**Solutions:**

1. **Adjust heap size:**
```yaml
env:
- name: JAVA_OPTS_APPEND
  value: "-Xms1024m -Xmx2048m -XX:MaxMetaspaceSize=512m"
```

2. **Enable GC logging:**
```yaml
env:
- name: JAVA_OPTS_APPEND
  value: "-Xlog:gc*:file=/tmp/gc.log"
```

## Authentication Issues

### Users Cannot Login

**Symptoms:**
- Login page loads but authentication fails
- Invalid credentials error

**Diagnosis:**
```bash
# Check logs
kubectl logs -n keycloak <keycloak-pod> | grep ERROR

# Check realm settings
# Login to admin console and verify realm is active
```

**Solutions:**

1. **Verify user exists:**
   - Login to admin console
   - Go to Users
   - Search for user

2. **Check user credentials:**
   - Reset password via admin console
   - Ensure account is enabled

3. **Review event logs:**
   - Realm Settings → Events
   - Check Login Events

### Token Issues

**Symptoms:**
- Invalid token errors
- Token expired errors

**Solutions:**

1. **Check token lifetime:**
   - Realm Settings → Tokens
   - Adjust token lifetimes

2. **Verify clock sync:**
```bash
# Check time on pod
kubectl exec -it <keycloak-pod> -n keycloak -- date

# Ensure NTP is configured on nodes
```

## SSL/TLS Issues

### Certificate Errors

**Symptoms:**
- SSL handshake failures
- Certificate validation errors

**Solutions:**

1. **Verify certificate:**
```bash
kubectl get secret keycloak-tls -n keycloak -o yaml
```

2. **Check ingress configuration:**
```bash
kubectl describe ingress keycloak-ingress -n keycloak
```

3. **Test certificate:**
```bash
openssl s_client -connect keycloak.example.com:443 -showcerts
```

## Network Issues

### Pod Cannot Reach Services

**Diagnosis:**
```bash
# Test connectivity
kubectl exec -it <keycloak-pod> -n keycloak -- \
  curl -v http://postgres:5432

# Check network policies
kubectl get networkpolicies -n keycloak
```

**Solutions:**

1. **Verify DNS:**
```bash
kubectl exec -it <keycloak-pod> -n keycloak -- \
  nslookup postgres.keycloak.svc.cluster.local
```

2. **Check service endpoints:**
```bash
kubectl get endpoints -n keycloak
```

## Storage Issues

### PVC Not Binding

**Symptoms:**
- PVC in `Pending` state
- Pod cannot start due to volume mount failure

**Diagnosis:**
```bash
# Check PVC
kubectl get pvc -n keycloak
kubectl describe pvc postgres-pvc -n keycloak

# Check PV
kubectl get pv
```

**Solutions:**

1. **Verify storage class:**
```bash
kubectl get storageclass
```

2. **Create PV manually (if needed):**
```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: postgres-pv
spec:
  capacity:
    storage: 10Gi
  accessModes:
    - ReadWriteOnce
  hostPath:
    path: /data/postgres
```

## Common Error Messages

### "Database not found"

**Solution:**
```bash
# Create database
kubectl exec -it <postgres-pod> -n keycloak -- \
  psql -U postgres -c "CREATE DATABASE keycloak;"
```

### "Connection refused"

**Solution:**
1. Check if service exists
2. Verify port numbers
3. Check firewall rules
4. Review network policies

### "Out of memory"

**Solution:**
```yaml
# Increase memory limits
resources:
  limits:
    memory: "4Gi"
```

### "Too many open files"

**Solution:**
```yaml
# Increase file descriptor limit
securityContext:
  sysctls:
  - name: fs.file-max
    value: "65536"
```

## Debug Mode

### Enable Debug Logging

```yaml
env:
- name: KC_LOG_LEVEL
  value: DEBUG
```

### Remote Debugging

```yaml
env:
- name: DEBUG
  value: "true"
- name: DEBUG_PORT
  value: "*:5005"
```

Port forward:
```bash
kubectl port-forward <keycloak-pod> -n keycloak 5005:5005
```

## Support Resources

### Getting Help

1. **Check documentation:**
   - [Keycloak Docs](https://www.keycloak.org/documentation)
   - This documentation

2. **Community resources:**
   - [Keycloak Discourse](https://keycloak.discourse.group/)
   - [Stack Overflow](https://stackoverflow.com/questions/tagged/keycloak)

3. **GitHub issues:**
   - [Report bugs](https://github.com/ianlintner/keycloak-server/issues)

### Collecting Debug Information

```bash
# Pod information
kubectl get pod <keycloak-pod> -n keycloak -o yaml > pod-info.yaml

# Logs
kubectl logs <keycloak-pod> -n keycloak > keycloak.log

# Events
kubectl get events -n keycloak --sort-by='.lastTimestamp' > events.log

# Describe resources
kubectl describe deployment keycloak -n keycloak > deployment-info.txt
```

## Next Steps

- [Monitoring Guide](monitoring.md)
- [Backup Guide](backup.md)
- [Production Deployment](../deployment/production.md)
