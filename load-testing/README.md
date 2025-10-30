# Clinical Management Platform - Load Testing Guide

This guide provides instructions for load testing the Clinical Management Platform using Apache JMeter.

## Prerequisites

- Apache JMeter 5.5 or later
- Running instance of Clinical Management Platform
- Test user accounts configured

## Installation

### Download JMeter

```bash
# Download from Apache
wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz

# Extract
tar -xzf apache-jmeter-5.6.3.tgz
cd apache-jmeter-5.6.3
```

## Test Scenarios

The load test plan includes the following scenarios:

### 1. User Login - Ramp Up Test
- **Purpose**: Test authentication system under load
- **Users**: 50 concurrent users
- **Ramp-up**: 30 seconds
- **Iterations**: 10 per user
- **Total requests**: 500

### 2. Get Patients - Sustained Load
- **Purpose**: Test read-heavy operations
- **Users**: 100 concurrent users
- **Ramp-up**: 60 seconds
- **Iterations**: 20 per user
- **Total requests**: 2,000

## Running Load Tests

### GUI Mode (Development)

```bash
# Start JMeter GUI
./bin/jmeter.sh  # Linux/Mac
./bin/jmeter.bat # Windows

# Load test plan
File -> Open -> clinical-management-load-test.jmx

# Configure variables
- HOST: localhost (or your server)
- PORT: 8081
- PROTOCOL: http

# Run test
Click "Start" button (green play icon)
```

### CLI Mode (Production)

```bash
# Basic run
./bin/jmeter.sh -n -t load-testing/jmeter/clinical-management-load-test.jmx \
  -l results/result.jtl \
  -j results/jmeter.log

# With custom host
./bin/jmeter.sh -n -t load-testing/jmeter/clinical-management-load-test.jmx \
  -Jhost=clinical.example.com \
  -Jport=443 \
  -Jprotocol=https \
  -l results/result-$(date +%Y%m%d-%H%M%S).jtl

# Generate HTML report
./bin/jmeter.sh -g results/result.jtl -o results/html-report
```

## Test Configuration

### Environment Variables

```properties
# JMeter properties (jmeter.properties)
server.rmi.ssl.disable=true

# Increase heap size for large tests
JVM_ARGS="-Xms1g -Xmx1g"
```

### Load Profiles

#### Light Load (Smoke Test)
```bash
# Modify thread groups:
# User Login: 10 users, 10s ramp-up, 1 iteration
# Get Patients: 20 users, 20s ramp-up, 5 iterations
```

#### Medium Load (Standard Test)
```bash
# Default configuration
# User Login: 50 users, 30s ramp-up, 10 iterations
# Get Patients: 100 users, 60s ramp-up, 20 iterations
```

#### Heavy Load (Stress Test)
```bash
# Modify thread groups:
# User Login: 200 users, 60s ramp-up, 20 iterations
# Get Patients: 500 users, 120s ramp-up, 50 iterations
```

#### Spike Test
```bash
# Sudden burst of traffic
# User Login: 500 users, 10s ramp-up, 1 iteration
# Get Patients: 1000 users, 20s ramp-up, 5 iterations
```

## Analyzing Results

### Summary Report

View in JMeter GUI:
- Response times (min, max, average)
- Throughput (requests/second)
- Error rate
- Standard deviation

### Key Metrics

**Response Time Targets:**
- Login: < 500ms (p95)
- Get Patients: < 300ms (p95)
- Error Rate: < 1%
- Throughput: > 100 req/s

**Example Good Results:**
```
Label               Samples  Avg    Min   Max    Std Dev  Error %  Throughput
POST /api/auth/login  500   245ms  120ms  890ms  156ms    0.0%     16.67/sec
GET /api/patients    2000   180ms   80ms  650ms  123ms    0.2%     33.33/sec
```

### HTML Report

```bash
# Generate report
./bin/jmeter.sh -g results/result.jtl -o results/html-report

# View report
open results/html-report/index.html
```

The HTML report includes:
- Statistics table
- Response time graphs
- Throughput graphs
- Error distribution
- Top 5 slowest requests

## Advanced Scenarios

### Custom Test Plan

Create `custom-load-test.jmx` with:

```xml
<!-- Add more thread groups for -->
- Create Appointment
- Get Appointments by Doctor
- Create Prescription
- Generate PDF
- WebSocket connections
```

### Distributed Testing

```bash
# Start JMeter servers on multiple machines
./bin/jmeter-server

# Run distributed test
./bin/jmeter.sh -n -t test-plan.jmx \
  -R server1,server2,server3 \
  -l results/distributed-result.jtl
```

### Continuous Load Testing

```bash
# Run for specific duration (1 hour)
./bin/jmeter.sh -n -t test-plan.jmx \
  -l results/soak-test.jtl \
  -JthreadCount=50 \
  -Jduration=3600

# Monitor system resources during test
# Use Prometheus/Grafana or kubectl top
```

## Integration with CI/CD

### GitHub Actions Example

```yaml
name: Load Test
on: [push]

jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Install JMeter
        run: |
          wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz
          tar -xzf apache-jmeter-5.6.3.tgz
      
      - name: Run Load Test
        run: |
          apache-jmeter-5.6.3/bin/jmeter.sh -n \
            -t load-testing/jmeter/clinical-management-load-test.jmx \
            -l results/result.jtl \
            -Jhost=${{ secrets.TEST_HOST }}
      
      - name: Upload Results
        uses: actions/upload-artifact@v2
        with:
          name: load-test-results
          path: results/
```

## Performance Tuning

Based on load test results:

### If Response Times are High

1. **Scale horizontally**
```bash
kubectl scale deployment clinical-management-backend --replicas=5
```

2. **Optimize database queries**
- Add indexes
- Use connection pooling
- Enable query cache

3. **Enable caching**
- Redis for session storage
- HTTP caching headers

### If Throughput is Low

1. **Check resource limits**
```bash
kubectl top pods
```

2. **Increase resources**
```yaml
resources:
  limits:
    cpu: 2000m
    memory: 2048Mi
```

3. **Optimize application**
- Enable async processing
- Reduce database roundtrips
- Optimize serialization

## Monitoring During Tests

### Real-time Metrics

```bash
# Watch pod metrics
watch kubectl top pods -n clinical-management

# Watch HPA scaling
watch kubectl get hpa -n clinical-management

# Monitor Prometheus
kubectl port-forward svc/prometheus 9090:9090
```

### Application Metrics

Access: http://localhost:8081/actuator/metrics

Key metrics:
- `http.server.requests`
- `jvm.memory.used`
- `hikaricp.connections.active`
- `process.cpu.usage`

## Troubleshooting

### High Error Rate

```bash
# Check application logs
kubectl logs -f deployment/clinical-management-backend

# Check database connections
kubectl exec -it postgres-pod -- psql -U postgres -c "SELECT count(*) FROM pg_stat_activity;"
```

### JMeter Out of Memory

```bash
# Increase heap
export JVM_ARGS="-Xms2g -Xmx4g"

# Or edit jmeter.bat/jmeter.sh
```

### Connection Timeouts

```xml
<!-- Increase timeouts in test plan -->
<stringProp name="HTTPSampler.connect_timeout">10000</stringProp>
<stringProp name="HTTPSampler.response_timeout">30000</stringProp>
```

## Best Practices

1. **Start small** - Begin with smoke tests
2. **Ramp up gradually** - Don't start all users at once
3. **Monitor infrastructure** - Watch CPU, memory, network
4. **Run during off-peak hours** - For production testing
5. **Save results** - Keep history for comparison
6. **Test realistic scenarios** - Match production usage patterns
7. **Test data cleanup** - Clean up test data after runs

## Performance Benchmarks

### Expected Results (Per Pod)

| Metric | Target | Good | Excellent |
|--------|--------|------|-----------|
| Response Time (p95) | < 500ms | < 300ms | < 200ms |
| Throughput | 50 req/s | 100 req/s | 200 req/s |
| Error Rate | < 1% | < 0.5% | < 0.1% |
| CPU Usage | < 80% | < 60% | < 40% |
| Memory Usage | < 80% | < 60% | < 40% |

### Scaling Recommendations

Based on load tests:
- 0-100 users: 2 pods
- 100-500 users: 3-5 pods
- 500-1000 users: 6-8 pods
- 1000+ users: 10+ pods with load balancer

## Resources

- [JMeter Documentation](https://jmeter.apache.org/usermanual/)
- [Performance Testing Guide](https://martinfowler.com/articles/performance-testing.html)
- [Load Testing Best Practices](https://k6.io/docs/testing-guides/)

## Support

For issues or questions:
- Create an issue on GitHub
- Email: support@clinical.com
