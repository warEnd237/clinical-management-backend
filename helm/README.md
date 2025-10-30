# Clinical Management Platform - Helm Deployment Guide

This guide provides instructions for deploying the Clinical Management Platform to Kubernetes using Helm.

## Prerequisites

- Kubernetes cluster (v1.24+)
- Helm 3.x installed
- kubectl configured
- (Optional) cert-manager for TLS certificates
- (Optional) Prometheus Operator for monitoring

## Quick Start

### 1. Install Dependencies

```bash
# Add required Helm repositories
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

### 2. Create Namespace

```bash
kubectl create namespace clinical-management
```

### 3. Configure Values

Create a `values-prod.yaml` file with your production settings:

```yaml
backend:
  image:
    repository: your-registry/clinical-backend
    tag: "1.0.0"
  
  env:
    jwtSecret: "your-strong-secret-key-here"
    corsOrigins: "https://your-domain.com"
  
  email:
    username: "your-email@gmail.com"
    password: "your-app-password"

postgresql:
  auth:
    password: "strong-database-password"

ingress:
  hosts:
    - host: your-domain.com
      paths:
        - path: /api
          pathType: Prefix
          service: backend
        - path: /
          pathType: Prefix
          service: frontend
  
  tls:
    - secretName: clinical-tls
      hosts:
        - your-domain.com
```

### 4. Install the Chart

```bash
# Dry run to validate
helm install clinical-management ./helm/clinical-management \
  --namespace clinical-management \
  --values values-prod.yaml \
  --dry-run --debug

# Install
helm install clinical-management ./helm/clinical-management \
  --namespace clinical-management \
  --values values-prod.yaml
```

### 5. Verify Deployment

```bash
# Check pod status
kubectl get pods -n clinical-management

# Check services
kubectl get svc -n clinical-management

# Check ingress
kubectl get ingress -n clinical-management

# View logs
kubectl logs -f deployment/clinical-management-backend -n clinical-management
```

## Configuration

### Backend Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `backend.enabled` | Enable backend deployment | `true` |
| `backend.replicaCount` | Number of replicas | `2` |
| `backend.image.repository` | Backend image repository | `clinical-backend` |
| `backend.image.tag` | Backend image tag | `1.0.0` |
| `backend.resources.limits.cpu` | CPU limit | `1000m` |
| `backend.resources.limits.memory` | Memory limit | `1024Mi` |
| `backend.autoscaling.enabled` | Enable HPA | `true` |
| `backend.autoscaling.minReplicas` | Minimum replicas | `2` |
| `backend.autoscaling.maxReplicas` | Maximum replicas | `10` |

### Frontend Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `frontend.enabled` | Enable frontend deployment | `true` |
| `frontend.replicaCount` | Number of replicas | `2` |
| `frontend.image.repository` | Frontend image repository | `clinical-frontend` |
| `frontend.image.tag` | Frontend image tag | `1.0.0` |

### Database Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `postgresql.enabled` | Enable PostgreSQL | `true` |
| `postgresql.auth.username` | Database username | `postgres` |
| `postgresql.auth.password` | Database password | `postgres` |
| `postgresql.auth.database` | Database name | `clinical_db` |
| `postgresql.primary.persistence.size` | PVC size | `10Gi` |

## Monitoring Setup

### Enable Prometheus Monitoring

```bash
# Install Prometheus Operator
kubectl create -f https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/bundle.yaml

# Verify ServiceMonitor is created
kubectl get servicemonitor -n clinical-management
```

### Access Prometheus

```bash
kubectl port-forward svc/prometheus-operated -n clinical-management 9090:9090
```

Visit: http://localhost:9090

### Install Grafana

```bash
# Install Grafana
helm repo add grafana https://grafana.github.io/helm-charts
helm install grafana grafana/grafana -n clinical-management

# Get admin password
kubectl get secret --namespace clinical-management grafana -o jsonpath="{.data.admin-password}" | base64 --decode

# Port forward
kubectl port-forward svc/grafana -n clinical-management 3000:80
```

Visit: http://localhost:3000

Import the dashboard from `monitoring/grafana/dashboards/clinical-management-dashboard.json`

## Scaling

### Manual Scaling

```bash
# Scale backend
kubectl scale deployment clinical-management-backend -n clinical-management --replicas=5

# Scale frontend
kubectl scale deployment clinical-management-frontend -n clinical-management --replicas=3
```

### Autoscaling

HPA is enabled by default and will scale based on CPU/Memory:
- Backend: 2-10 replicas
- Frontend: 2-5 replicas

View HPA status:

```bash
kubectl get hpa -n clinical-management
```

## Upgrading

```bash
# Upgrade deployment
helm upgrade clinical-management ./helm/clinical-management \
  --namespace clinical-management \
  --values values-prod.yaml

# Rollback if needed
helm rollback clinical-management -n clinical-management
```

## Backup & Restore

### Database Backup

```bash
# Create backup
kubectl exec -n clinical-management \
  $(kubectl get pod -n clinical-management -l app.kubernetes.io/name=postgresql -o jsonpath='{.items[0].metadata.name}') \
  -- pg_dump -U postgres clinical_db > backup-$(date +%Y%m%d).sql

# Restore
kubectl exec -i -n clinical-management \
  $(kubectl get pod -n clinical-management -l app.kubernetes.io/name=postgresql -o jsonpath='{.items[0].metadata.name}') \
  -- psql -U postgres clinical_db < backup.sql
```

## Troubleshooting

### Pod Not Starting

```bash
# Describe pod
kubectl describe pod <pod-name> -n clinical-management

# View events
kubectl get events -n clinical-management --sort-by='.lastTimestamp'
```

### Database Connection Issues

```bash
# Test connection
kubectl run -it --rm debug --image=postgres:15 --restart=Never -n clinical-management \
  -- psql -h clinical-management-postgresql -U postgres -d clinical_db
```

### View Application Logs

```bash
# Backend logs
kubectl logs -f deployment/clinical-management-backend -n clinical-management

# Frontend logs
kubectl logs -f deployment/clinical-management-frontend -n clinical-management
```

## Uninstall

```bash
# Delete release
helm uninstall clinical-management -n clinical-management

# Delete namespace
kubectl delete namespace clinical-management
```

## Production Checklist

- [ ] Update all default passwords
- [ ] Configure TLS certificates
- [ ] Set up database backups
- [ ] Configure monitoring alerts
- [ ] Set resource limits appropriately
- [ ] Enable network policies
- [ ] Configure log aggregation
- [ ] Set up disaster recovery plan
- [ ] Review security settings
- [ ] Configure auto-scaling thresholds

## Support

For issues or questions:
- GitHub: https://github.com/clinical-management/platform
- Email: support@clinical.com
