# 🏥 Clinical Management Platform

**A comprehensive, production-ready healthcare management system for medical clinics and hospitals.**

Manage patients, appointments, prescriptions, invoices, and internal communications with enterprise-grade security, real-time notifications, and automated workflows.

[![Production Ready](https://img.shields.io/badge/production-ready-brightgreen)](https://github.com)
[![Test Coverage](https://img.shields.io/badge/coverage-80%25-brightgreen)](https://github.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-18-red)](https://angular.io)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org)

---

## 📦 Repository Structure

**This platform consists of TWO separate repositories:**

### 🔧 **Backend Repository** (This Repository)
- **Repository**: https://github.com/warEnd237/clinical-management-backend
- **Technology**: Spring Boot 3.x + Java 21 + PostgreSQL
- **Purpose**: RESTful API, business logic, database, authentication
- **Standalone**: Can run independently for API development

### 🎨 **Frontend Repository** (Separate)
- **Repository**: https://github.com/warEnd237/clinical-management-frontend  
- **Technology**: Angular 19 + TailwindCSS + TypeScript
- **Purpose**: User interface, real-time updates, responsive UI
- **Requires**: Backend API running on http://localhost:8081

### 🔗 **How They Connect**
```
┌─────────────────────┐         ┌─────────────────────┐
│  Angular Frontend   │────────▶│  Spring Boot API    │
│  Port: 4200         │  HTTP   │  Port: 8081         │
│                     │◀────────│                     │
│  - UI Components    │ WebSocket│  - REST Endpoints  │
│  - State Management │         │  - Business Logic   │
│  - Routing          │         │  - Authentication   │
└─────────────────────┘         └──────────┬──────────┘
                                           │
                                           ▼
                                ┌─────────────────────┐
                                │  PostgreSQL DB      │
                                │  Port: 5432         │
                                └─────────────────────┘
```

**Communication:**
- **REST API**: Frontend calls backend endpoints (e.g., `/api/patients`, `/api/appointments`)
- **WebSocket**: Real-time notifications via STOMP over WebSocket
- **Authentication**: JWT tokens in Authorization header
- **CORS**: Backend allows `http://localhost:4200` (configurable)

---

## ✨ Features

### 👥 Patient Management
- Complete patient records with medical history
- Search and filter patients by name, email, or ID
- Secure patient data storage with encryption
- GDPR-compliant data handling

### 📅 Appointment Scheduling
- Real-time availability checking
- Automated conflict detection
- Email notifications for confirmations and reminders
- Multi-doctor scheduling support
- Cancellation with notice period enforcement

### 💊 Prescription Management
- Digital prescription generation
- PDF export with doctor signature
- Medication tracking and history
- Drug interaction warnings

### 💰 Invoicing & Billing
- Automated invoice generation
- Tax calculation and tracking
- Payment status management
- PDF invoice export
- Financial reporting

### 🔐 Security & Authentication
- JWT-based authentication
- Role-based access control (Admin, Doctor, Secretary)
- Password encryption with BCrypt
- Secure session management
- API endpoint protection

### 💬 Internal Messaging
- Real-time WebSocket messaging
- User-to-user communication
- Message read status tracking
- Notification system

### 📄 PDF Generation
- Prescription PDFs with professional formatting
- Invoice PDFs with clinic branding
- Automated document generation
- Digital signature support

### 📊 Monitoring & Metrics
- Prometheus metrics integration
- Grafana dashboards
- Health check endpoints
- Performance monitoring
- Error tracking

---

## 🏗️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21 (LTS)
- **Database**: PostgreSQL 15+
- **Security**: Spring Security + JWT
- **Real-time**: WebSocket (STOMP/SockJS)
- **PDF**: Apache PDFBox 3.0
- **Email**: JavaMailSender + Thymeleaf
- **Migrations**: Flyway
- **API Docs**: Swagger/OpenAPI
- **Testing**: JUnit 5, Mockito, MockMvc
- **Monitoring**: Micrometer + Prometheus

### Frontend
- **Framework**: Angular 18
- **Styling**: TailwindCSS
- **State Management**: RxJS
- **Real-time**: STOMP.js
- **HTTP**: HttpClient
- **Routing**: Angular Router
- **Forms**: Reactive Forms
- **Testing**: Cypress (E2E)

### Infrastructure
- **Containerization**: Docker + Docker Compose
- **Orchestration**: Kubernetes + Helm
- **Web Server**: Nginx
- **Load Testing**: Apache JMeter
- **CI/CD Ready**: GitHub Actions compatible

---

## 🚀 Quick Start

### Prerequisites
- **Docker & Docker Compose** (recommended)
- **Java 21** (for local development)
- **Node.js 20+** (for frontend development)
- **PostgreSQL 15+** (if running without Docker)
- **Maven 3.9+** (for backend builds)

### Option 1: Full Stack Setup (Backend + Frontend)

**Note**: This option requires both repositories.

1. **Clone both repositories**
   ```bash
   # Clone backend
   git clone https://github.com/warEnd237/clinical-management-backend.git
   cd clinical-management-backend
   
   # Clone frontend (in a separate directory)
   cd ..
   git clone https://github.com/warEnd237/clinical-management-frontend.git
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your settings
   ```

3. **Start all services**
   ```bash
   docker-compose up -d
   ```

4. **Access the application**
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8081
   - Swagger UI: http://localhost:8081/swagger-ui/
   - API Docs: http://localhost:8081/v3/api-docs
   - Database: localhost:5432

### Option 2: Backend Only (API Development)

If you only need the backend API:

1. **Clone backend repository**
   ```bash
   git clone https://github.com/warEnd237/clinical-management-backend.git
   cd clinical-management-backend
   ```

2. **Setup database** (see Database section above)

3. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your database and email settings
   ```

4. **Run backend**
   ```bash
   cd clinical-management-fresh
   ./mvnw spring-boot:run
   ```

5. **Test API**
   - Swagger UI: http://localhost:8081/swagger-ui/
   - Test endpoints with Postman or curl

### Option 3: Frontend + Backend (Local Development)

**Step 1 - Start Backend:**
```bash
cd clinical-management-backend/clinical-management-fresh
./mvnw spring-boot:run
# Backend runs on http://localhost:8081
```

**Step 2 - Start Frontend** (in new terminal):
```bash
cd clinical-management-frontend
npm install
npm start
# Frontend runs on http://localhost:4200
```

**Step 3 - Access Application:**
- Open browser: http://localhost:4200
- Frontend automatically connects to backend on port 8081

**Database:**
```bash
# Using Docker
docker run -d \
  --name postgres \
  -e POSTGRES_DB=clinical_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

---

## 📁 Project Structure

```
clinical-management/
├── clinical-management-fresh/      # Spring Boot Backend
│   ├── src/main/java/             # Java source code
│   │   └── com/clinical/backend/
│   │       ├── config/            # Configuration classes
│   │       ├── controller/        # REST controllers
│   │       ├── dto/               # Data Transfer Objects
│   │       ├── entity/            # JPA entities
│   │       ├── repository/        # Data repositories
│   │       ├── service/           # Business logic
│   │       ├── security/          # Security components
│   │       ├── scheduler/         # Scheduled tasks
│   │       └── exception/         # Custom exceptions
│   ├── src/main/resources/
│   │   ├── db/migration/          # Flyway migrations
│   │   └── templates/             # Email templates
│   └── src/test/                  # Unit & integration tests
│
├── clinical-management-frontend/   # Angular Frontend
│   ├── src/app/
│   │   ├── components/            # UI components
│   │   ├── services/              # Angular services
│   │   ├── guards/                # Route guards
│   │   ├── models/                # TypeScript models
│   │   └── interceptors/          # HTTP interceptors
│   └── cypress/                   # E2E tests
│
├── helm/                          # Kubernetes Helm charts
│   └── clinical-management/
│       ├── templates/             # K8s manifests
│       └── values.yaml            # Configuration
│
├── monitoring/                    # Monitoring config
│   └── grafana/dashboards/        # Grafana dashboards
│
├── load-testing/                  # Load testing
│   └── jmeter/                    # JMeter test plans
│
├── docker-compose.yml             # Docker Compose config
├── .env.example                   # Environment variables template
└── README.md                      # This file
```

---

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the project root:

```bash
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=clinical_db
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_jwt_secret_key_here
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=2592000000

# Email (Gmail example)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@clinical.com

# Application
SERVER_PORT=8081
FRONTEND_URL=http://localhost:4200
```

### Application Properties

The backend uses `application.yml` for configuration:
- Database connection settings
- JWT configuration
- Email SMTP settings
- Actuator endpoints
- Prometheus metrics

---

## 🧪 Testing

### Run Unit Tests

**Backend (131 tests, 80% coverage):**
```bash
cd clinical-management-fresh
./mvnw test
./mvnw jacoco:report
# View coverage: target/site/jacoco/index.html
```

**Frontend (E2E tests):**
```bash
cd clinical-management-frontend
npm run cypress:open
```

### Load Testing

```bash
cd load-testing
# Run JMeter tests
jmeter -n -t jmeter/clinical-management-load-test.jmx -l results/result.jtl
```

---

## 📊 API Documentation

### Swagger UI
Access interactive API documentation at:
- **Interactive UI**: http://localhost:8081/swagger-ui/
- **JSON Spec**: http://localhost:8081/v3/api-docs
- **YAML Spec**: http://localhost:8081/v3/api-docs.yaml

### Key Endpoints

**Authentication:**
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/logout` - User logout

**Patients:**
- `GET /api/patients` - List all patients
- `POST /api/patients` - Create patient
- `GET /api/patients/{id}` - Get patient details
- `PUT /api/patients/{id}` - Update patient
- `DELETE /api/patients/{id}` - Delete patient (Admin only)

**Appointments:**
- `POST /api/appointments` - Create appointment
- `GET /api/appointments/{id}` - Get appointment
- `POST /api/appointments/{id}/cancel` - Cancel appointment
- `GET /api/appointments/doctor/{id}` - Get doctor appointments
- `GET /api/appointments/patient/{id}` - Get patient appointments

**Prescriptions:**
- `POST /api/prescriptions` - Create prescription
- `GET /api/prescriptions/{id}` - Get prescription
- `GET /api/prescriptions/patient/{id}` - Get patient prescriptions

**Invoices:**
- `POST /api/invoices` - Create invoice
- `GET /api/invoices/{id}` - Get invoice
- `POST /api/invoices/{id}/pay` - Mark as paid
- `GET /api/invoices/unpaid` - Get unpaid invoices

---

## 🐳 Docker Deployment

### Build Images

```bash
# Backend
cd clinical-management-fresh
docker build -t clinical-backend:1.0.0 .

# Frontend
cd clinical-management-frontend
docker build -t clinical-frontend:1.0.0 .
```

### Run with Docker Compose

```bash
docker-compose up -d
docker-compose logs -f
```

---

## ☸️ Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (v1.24+)
- Helm 3.x
- kubectl configured

### Deploy with Helm

```bash
# Create namespace
kubectl create namespace clinical-management

# Install chart
helm install clinical-management ./helm/clinical-management \
  --namespace clinical-management \
  --values helm/clinical-management/values.yaml

# Check status
kubectl get all -n clinical-management
```

### Configure Monitoring

```bash
# Access Prometheus
kubectl port-forward svc/prometheus 9090:9090 -n clinical-management

# Access Grafana
kubectl port-forward svc/grafana 3000:80 -n clinical-management
```

---

## 👤 Default Users

After seed data is loaded:

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@clinical.com | Admin@123 |
| Doctor | doctor@clinical.com | Doctor@123 |
| Secretary | secretary@clinical.com | Secretary@123 |

**⚠️ Change these passwords in production!**

---

## 📈 Monitoring & Metrics

### Health Checks
- `/actuator/health` - Application health
- `/actuator/health/liveness` - Kubernetes liveness probe
- `/actuator/health/readiness` - Kubernetes readiness probe

### Metrics
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/metrics` - Application metrics

### Grafana Dashboards
Pre-configured dashboards available in `monitoring/grafana/dashboards/`:
- HTTP request rates
- Response times (p95, p99)
- CPU & memory usage
- Error rates
- Database connections

---

## 🔒 Security

### Authentication Flow
1. User submits credentials to `/api/auth/login`
2. Server validates and returns JWT tokens
3. Client stores tokens securely
4. Client includes access token in Authorization header
5. Server validates token on each request
6. Token refresh using refresh token when expired

### Password Security
- BCrypt hashing with salt
- Minimum 8 characters
- Password complexity requirements

### API Security
- JWT token validation
- Role-based access control
- CORS configuration
- SQL injection prevention (JPA/Hibernate)
- XSS protection

---

## 🚨 Troubleshooting

### Frontend Can't Connect to Backend

**Problem**: Frontend shows "Connection refused" or API errors

**Solutions**:
```bash
# 1. Check backend is running
curl http://localhost:8081/actuator/health
# Should return: {"status":"UP"}

# 2. Check CORS configuration in backend
# In application.yml, verify:
# cors.allowed-origins: http://localhost:4200

# 3. Verify frontend API URL
# In frontend: src/environments/environment.ts
# Should be: apiUrl: 'http://localhost:8081/api'

# 4. Check browser console for errors
# Open DevTools (F12) and check Console/Network tabs
```

### WebSocket Connection Failed

**Problem**: Real-time notifications not working

**Solutions**:
```bash
# 1. Check WebSocket endpoint
curl http://localhost:8081/ws/info

# 2. Verify in browser console:
# Should see: "Connecting to WebSocket..."
# Should NOT see: "WebSocket connection failed"

# 3. Check that user is logged in
# WebSocket requires valid JWT token
```

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -U postgres -d clinical_db

# Reset database if needed
psql -U postgres -c "DROP DATABASE IF EXISTS clinical_db;"
psql -U postgres -c "CREATE DATABASE clinical_db;"
```

### Backend Won't Start
```bash
# Check logs
docker-compose logs backend

# Verify environment variables
cat .env

# Check if port 8081 is available
lsof -i :8081  # Mac/Linux
netstat -ano | findstr :8081  # Windows

# Rebuild
docker-compose down
docker-compose up --build
```

### Frontend Build Errors
```bash
# Clear cache
rm -rf node_modules package-lock.json
npm install

# Check Node version
node --version  # Should be 20+

# Verify backend is running
curl http://localhost:8081/api/auth/login
```

### "Invalid JWT Token" Errors

**Problem**: Getting 401 Unauthorized errors

**Solutions**:
```bash
# 1. Check JWT_SECRET matches between .env and application.yml
# 2. Try logging in again to get fresh token
# 3. Check token expiration in .env:
#    JWT_ACCESS_TOKEN_EXPIRATION=900000  # 15 minutes
# 4. Clear browser localStorage and login again
```

---

## 📚 Additional Documentation

- **[Developer Handbook](./DEVELOPER_HANDBOOK.md)** - Comprehensive development guide
- **[Helm Chart README](./helm/README.md)** - Kubernetes deployment guide
- **[Load Testing Guide](./load-testing/README.md)** - Performance testing guide

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 📞 Support

For issues and questions:
- **GitHub Issues**: [Create an issue](https://github.com/your-repo/issues)
- **Email**: support@clinical.com
- **Documentation**: See `DEVELOPER_HANDBOOK.md`

---

## 🎯 Project Status

- ✅ **Production Ready**
- ✅ 80% Test Coverage (131 tests)
- ✅ Docker & Kubernetes Ready
- ✅ Monitoring Configured
- ✅ Load Tested
- ✅ Security Hardened
- ✅ API Documentation Complete

---

**Built with ❤️ for modern healthcare management**
