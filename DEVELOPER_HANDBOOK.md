# 👨‍💻 Clinical Management Platform - Developer Handbook

**Complete guide for developers to understand, build, and maintain the platform.**

---

## 📋 Table of Contents

1. [Getting Started](#-getting-started)
2. [Architecture Overview](#-architecture-overview)
3. [Backend Development](#-backend-development)
4. [Frontend Development](#-frontend-development)
5. [Database Schema](#-database-schema)
6. [Security Implementation](#-security-implementation)
7. [Testing Strategy](#-testing-strategy)
8. [Deployment Guide](#-deployment-guide)
9. [Monitoring & Observability](#-monitoring--observability)
10. [Troubleshooting](#-troubleshooting)

---

## 🚀 Getting Started

### Development Environment Setup

#### Required Software

1. **Java Development Kit 21**
   ```bash
   # Download from Oracle or use SDKMAN
   sdk install java 21.0.1-oracle
   java --version
   ```

2. **Node.js 20+**
   ```bash
   # Download from nodejs.org or use NVM
   nvm install 20
   node --version
   npm --version
   ```

3. **PostgreSQL 15+**
   ```bash
   # Using Docker
   docker run --name postgres \
     -e POSTGRES_PASSWORD=postgres \
     -e POSTGRES_DB=clinical_db \
     -p 5432:5432 \
     -d postgres:15
   ```

4. **Maven 3.9+** (or use included wrapper)
   ```bash
   ./mvnw --version
   ```

5. **IDE Recommendations**
   - **Backend**: IntelliJ IDEA (Ultimate or Community)
   - **Frontend**: VS Code with Angular extensions
   - **Database**: DBeaver or pgAdmin

#### Clone and Setup

```bash
# Clone repository
git clone <repository-url>
cd clinical-magement

# Backend setup
cd clinical-management-fresh
cp src/main/resources/application.yml.example src/main/resources/application.yml
# Edit application.yml with your settings

# Frontend setup
cd ../clinical-management-frontend
npm install

# Create .env file
cd ..
cp .env.example .env
# Edit .env with your configuration
```

---

## 🏛️ Architecture Overview

### System Architecture

```
┌─────────────────┐         ┌─────────────────┐
│   Angular 18    │◄────────┤   Nginx Proxy   │
│    Frontend     │  HTTP   └─────────────────┘
└────────┬────────┘                 │
         │ REST API                 │
         │ WebSocket                │
         ▼                          ▼
┌─────────────────┐         ┌─────────────────┐
│  Spring Boot    │◄────────┤   API Gateway   │
│    Backend      │         └─────────────────┘
└────────┬────────┘
         │
         ├─────► PostgreSQL (Data)
         ├─────► Email Server (SMTP)
         ├─────► PDF Generation
         └─────► Prometheus (Metrics)
```

### Technology Layers

**Presentation Layer** (Angular)
- Components & Templates
- Services & State Management
- Routing & Guards
- HTTP Interceptors

**Application Layer** (Spring Boot)
- REST Controllers
- WebSocket Handlers
- Security Filters
- Business Services

**Data Layer** (PostgreSQL)
- JPA Entities
- Repositories
- Flyway Migrations
- Database Constraints

**Infrastructure Layer**
- Docker Containers
- Kubernetes Pods
- Prometheus Monitoring
- Email Service

---

## 💻 Backend Development

### Project Structure

```
src/main/java/com/clinical/backend/
├── config/                 # Configuration classes
│   ├── SecurityConfig.java          # Spring Security setup
│   ├── WebSocketConfig.java         # WebSocket configuration
│   └── OpenApiConfig.java           # Swagger documentation
│
├── controller/             # REST API endpoints
│   ├── AuthController.java          # /api/auth/*
│   ├── PatientController.java       # /api/patients/*
│   ├── AppointmentController.java   # /api/appointments/*
│   ├── PrescriptionController.java  # /api/prescriptions/*
│   └── InvoiceController.java       # /api/invoices/*
│
├── service/                # Business logic
│   ├── AuthService.java             # Authentication
│   ├── PatientService.java          # Patient management
│   ├── AppointmentService.java      # Appointment scheduling
│   ├── EmailService.java            # Email notifications
│   ├── PdfService.java              # PDF generation
│   └── AuditService.java            # Audit logging
│
├── repository/             # Data access
│   ├── UserRepository.java
│   ├── PatientRepository.java
│   ├── AppointmentRepository.java
│   └── InvoiceRepository.java
│
├── entity/                 # JPA entities
│   ├── User.java
│   ├── Patient.java
│   ├── Appointment.java
│   ├── Prescription.java
│   └── Invoice.java
│
├── dto/                    # Data Transfer Objects
│   ├── auth/
│   ├── patient/
│   ├── appointment/
│   └── common/
│
├── security/               # Security components
│   ├── JwtUtil.java                 # JWT token handling
│   ├── JwtAuthenticationFilter.java # Request filter
│   └── CustomUserDetails.java       # User principal
│
└── exception/              # Custom exceptions
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

### Key Classes Explained

#### 1. AuthService.java

Handles authentication and token management:

```java
@Service
public class AuthService {
    
    // User login
    public AuthResponse login(LoginRequest request) {
        // 1. Authenticate user
        // 2. Generate access & refresh tokens
        // 3. Save refresh token to database
        // 4. Return AuthResponse with tokens
    }
    
    // Token refresh
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        // 1. Validate refresh token
        // 2. Generate new access token
        // 3. Return new tokens
    }
    
    // User logout
    public void logout(Long userId) {
        // 1. Invalidate refresh token
        // 2. Clear from database
    }
}
```

#### 2. AppointmentService.java

Manages appointment scheduling with conflict detection:

```java
@Service
public class AppointmentService {
    
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // 1. Validate patient and doctor exist
        // 2. Check doctor availability (no conflicts)
        // 3. Validate cancellation notice period
        // 4. Save appointment
        // 5. Send email notification
        // 6. Create notification for doctor & patient
        // 7. Log audit trail
    }
    
    private void checkDoctorAvailability(...) {
        // Uses PostgreSQL exclusion constraint
        // Prevents double-booking
    }
}
```

#### 3. EmailService.java

Sends templated emails using Thymeleaf:

```java
@Service
public class EmailService {
    
    public void sendAppointmentConfirmation(Appointment appointment) {
        // 1. Load Thymeleaf template
        // 2. Populate template variables
        // 3. Generate HTML content
        // 4. Send via JavaMailSender
        // 5. Handle errors gracefully
    }
}
```

### Adding a New Feature

**Example: Add Patient Notes Feature**

1. **Create Entity** (`entity/PatientNote.java`)
   ```java
   @Entity
   @Table(name = "patient_notes")
   public class PatientNote {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @ManyToOne
       @JoinColumn(name = "patient_id")
       private Patient patient;
       
       @ManyToOne
       @JoinColumn(name = "doctor_id")
       private Doctor doctor;
       
       private String content;
       private LocalDateTime createdAt;
   }
   ```

2. **Create Repository** (`repository/PatientNoteRepository.java`)
   ```java
   public interface PatientNoteRepository extends JpaRepository<PatientNote, Long> {
       List<PatientNote> findByPatientId(Long patientId);
   }
   ```

3. **Create DTOs** (`dto/note/NoteRequest.java`, `NoteResponse.java`)

4. **Create Service** (`service/PatientNoteService.java`)
   ```java
   @Service
   public class PatientNoteService {
       public NoteResponse createNote(NoteRequest request) {
           // Business logic
       }
   }
   ```

5. **Create Controller** (`controller/PatientNoteController.java`)
   ```java
   @RestController
   @RequestMapping("/api/notes")
   public class PatientNoteController {
       @PostMapping
       public ResponseEntity<ApiResponse<NoteResponse>> createNote(...) {
           // Handle HTTP request
       }
   }
   ```

6. **Create Migration** (`src/main/resources/db/migration/V3__create_patient_notes.sql`)
   ```sql
   CREATE TABLE patient_notes (
       id BIGSERIAL PRIMARY KEY,
       patient_id BIGINT REFERENCES patients(id),
       doctor_id BIGINT REFERENCES doctors(id),
       content TEXT NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

7. **Write Tests** (`test/java/.../service/PatientNoteServiceTest.java`)

---

## 🎨 Frontend Development

### Project Structure

```
src/app/
├── components/             # UI Components
│   ├── dashboard/
│   ├── patients/
│   ├── appointments/
│   ├── prescriptions/
│   └── invoices/
│
├── services/               # Angular Services
│   ├── auth.service.ts          # Authentication
│   ├── patient.service.ts       # Patient API calls
│   ├── appointment.service.ts   # Appointment API calls
│   └── websocket.service.ts     # WebSocket connection
│
├── guards/                 # Route Guards
│   ├── auth.guard.ts            # Login required
│   └── role.guard.ts            # Role-based access
│
├── interceptors/           # HTTP Interceptors
│   ├── auth.interceptor.ts      # Add JWT token
│   └── error.interceptor.ts     # Handle errors
│
├── models/                 # TypeScript Interfaces
│   ├── user.model.ts
│   ├── patient.model.ts
│   └── appointment.model.ts
│
└── app.routes.ts           # Application routing
```

### Key Services

#### AuthService (auth.service.ts)

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<User | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${environment.apiUrl}/auth/login`,
      { email, password }
    ).pipe(
      map(response => response.data),
      tap(authResponse => {
        localStorage.setItem('accessToken', authResponse.accessToken);
        localStorage.setItem('refreshToken', authResponse.refreshToken);
        this.currentUserSubject.next(authResponse.user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.currentUserSubject.next(null);
  }
}
```

#### HTTP Interceptor (auth.interceptor.ts)

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('accessToken');
  
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  
  return next(req);
};
```

### State Management

Uses RxJS BehaviorSubject for reactive state:

```typescript
// In service
private patientsSubject = new BehaviorSubject<Patient[]>([]);
public patients$ = this.patientsSubject.asObservable();

// In component
ngOnInit() {
  this.patientService.patients$.subscribe(patients => {
    this.patients = patients;
  });
}
```

---

## 🗄️ Database Schema

### Entity Relationship Diagram

```
┌────────────┐     ┌─────────────┐     ┌──────────────┐
│   Users    │1───M│   Doctors   │1───M│Appointments  │
└────────────┘     └─────────────┘     └──────────────┘
      │1                  │1                   │M
      │                   │                    │
      │M                  │M                   │1
┌────────────┐     ┌─────────────┐     ┌──────────────┐
│  Messages  │     │Prescriptions│     │   Patients   │
└────────────┘     └─────────────┘     └──────────────┘
                          │1                   │1
                          │                    │
                          │M                   │M
                   ┌─────────────┐     ┌──────────────┐
                   │   Items     │     │   Invoices   │
                   └─────────────┘     └──────────────┘
```

### Key Tables

**users**
- Primary authentication table
- Stores email, password hash, role
- One-to-one with Doctor or Secretary

**patients**
- Patient demographic information
- Medical history
- Contact details

**appointments**
- Links Patient and Doctor
- Has exclusion constraint to prevent overlaps
- Triggers email notifications

**prescriptions**
- Links to Appointment
- Contains medication details
- Can generate PDF

**invoices**
- Billing information
- Payment tracking
- Tax calculation

### Database Migrations

Located in `src/main/resources/db/migration/`:

- `V1__initial_schema.sql` - Create base tables
- `V2__seed_data.sql` - Insert test users

**Creating New Migration:**

```bash
# Create file: V3__add_feature.sql
# Naming: V{number}__{description}.sql

# Example:
# V3__add_patient_notes.sql
CREATE TABLE patient_notes (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT REFERENCES patients(id),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔐 Security Implementation

### JWT Authentication Flow

```
1. User Login
   POST /api/auth/login
   ↓
2. Server validates credentials
   ↓
3. Generate tokens:
   - Access Token (1 hour)
   - Refresh Token (30 days)
   ↓
4. Return tokens to client
   ↓
5. Client stores in localStorage
   ↓
6. Client includes token in requests:
   Authorization: Bearer {accessToken}
   ↓
7. Server validates token
   ↓
8. If valid, process request
   If expired, use refresh token
```

### JWT Configuration

In `application.yml`:
```yaml
jwt:
  secret: "your-256-bit-secret-key"
  access-token-expiration: 3600000    # 1 hour
  refresh-token-expiration: 2592000000 # 30 days
```

### Role-Based Access Control

**Roles:**
- `ADMIN` - Full system access
- `DOCTOR` - Medical operations
- `SECRETARY` - Scheduling, billing

**Implementation:**

```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/patients/{id}")
public ResponseEntity<?> deletePatient(@PathVariable Long id) {
    // Only admins can delete
}

@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
@PostMapping("/prescriptions")
public ResponseEntity<?> createPrescription(...) {
    // Doctors and admins can prescribe
}
```

### Password Security

- **Hashing**: BCrypt with salt (strength 12)
- **Validation**: Minimum 8 characters, complexity required
- **Storage**: Never store plain text

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

---

## 🧪 Testing Strategy

### Test Coverage Goals

- **Unit Tests**: 80%+ coverage
- **Integration Tests**: Critical flows
- **E2E Tests**: User journeys

### Backend Testing

**Unit Tests** (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    
    @Mock
    private AppointmentRepository repository;
    
    @InjectMocks
    private AppointmentService service;
    
    @Test
    void shouldCreateAppointment() {
        // Arrange
        when(repository.save(any())).thenReturn(appointment);
        
        // Act
        AppointmentResponse result = service.createAppointment(request);
        
        // Assert
        assertNotNull(result);
        verify(repository).save(any());
    }
}
```

**Controller Tests** (MockMvc)

```java
@WebMvcTest(PatientController.class)
class PatientControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PatientService patientService;
    
    @Test
    @WithMockUser(roles = "SECRETARY")
    void shouldGetAllPatients() throws Exception {
        mockMvc.perform(get("/api/patients"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

### Frontend Testing

**E2E Tests** (Cypress)

```typescript
describe('Login Flow', () => {
  it('should login successfully', () => {
    cy.visit('/login');
    cy.get('[data-test=email]').type('admin@clinical.com');
    cy.get('[data-test=password]').type('Admin@123');
    cy.get('[data-test=login-btn]').click();
    cy.url().should('include', '/dashboard');
  });
});
```

### Running Tests

```bash
# Backend unit tests
cd clinical-management-fresh
./mvnw test

# Generate coverage report
./mvnw jacoco:report
# View at: target/site/jacoco/index.html

# Frontend E2E tests
cd clinical-management-frontend
npm run cypress:open
```

---

## 🚢 Deployment Guide

### Local Development

```bash
# Terminal 1 - Database
docker run -d -p 5432:5432 \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=clinical_db \
  postgres:15

# Terminal 2 - Backend
cd clinical-management-fresh
./mvnw spring-boot:run

# Terminal 3 - Frontend
cd clinical-management-frontend
npm start
```

### Docker Compose

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Kubernetes Deployment

```bash
# Install with Helm
helm install clinical ./helm/clinical-management \
  --namespace clinical \
  --create-namespace

# Check status
kubectl get pods -n clinical

# Scale deployment
kubectl scale deployment clinical-backend --replicas=3 -n clinical

# View logs
kubectl logs -f deployment/clinical-backend -n clinical
```

### Production Checklist

- [ ] Change all default passwords
- [ ] Use strong JWT secret (256-bit)
- [ ] Configure HTTPS/TLS
- [ ] Set up database backups
- [ ] Configure email SMTP properly
- [ ] Enable monitoring (Prometheus/Grafana)
- [ ] Set resource limits (CPU/Memory)
- [ ] Configure log aggregation
- [ ] Set up alerts
- [ ] Review security settings

---

## 📊 Monitoring & Observability

### Health Checks

```bash
# Liveness probe (is app alive?)
curl http://localhost:8081/actuator/health/liveness

# Readiness probe (can it handle traffic?)
curl http://localhost:8081/actuator/health/readiness

# Full health check
curl http://localhost:8081/actuator/health
```

### Metrics

**Prometheus Metrics:**
```bash
# Access metrics endpoint
curl http://localhost:8081/actuator/prometheus

# Key metrics:
# - http_server_requests_seconds_count
# - jvm_memory_used_bytes
# - hikaricp_connections_active
# - process_cpu_usage
```

**Grafana Dashboards:**

Import dashboard from: `monitoring/grafana/dashboards/clinical-management-dashboard.json`

Panels include:
- HTTP request rate
- Response time (p95)
- CPU usage
- Memory usage
- Error rate
- Active connections

### Logging

**Backend Logging:**
```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MyService {
    public void doSomething() {
        log.info("Processing request for user: {}", userId);
        log.warn("Low disk space detected");
        log.error("Failed to process payment", exception);
    }
}
```

**Log Levels:**
- `ERROR` - Critical failures
- `WARN` - Important warnings
- `INFO` - General information
- `DEBUG` - Detailed debugging (development only)

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Application Won't Start

**Error:** `Unsatisfied dependency... JavaMailSender`

**Solution:**
```yaml
# Check application.yml has correct email config
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

#### 2. Database Connection Failed

**Error:** `Connection refused`

**Solutions:**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection settings
DB_HOST=localhost  # Use 'postgres' if in Docker network
DB_PORT=5432
DB_NAME=clinical_db
```

#### 3. JWT Token Invalid

**Error:** `401 Unauthorized`

**Solutions:**
- Check token hasn't expired
- Verify JWT secret matches between requests
- Ensure token format: `Authorization: Bearer {token}`

#### 4. Frontend Can't Connect to Backend

**Error:** `CORS error` or `Connection refused`

**Solutions:**
```typescript
// Check environment.ts
export const environment = {
  apiUrl: 'http://localhost:8081/api'  // Correct URL
};
```

```java
// Check CORS configuration
@CrossOrigin(origins = "http://localhost:4200")
```

### Debug Mode

**Enable Debug Logging:**

```yaml
logging:
  level:
    com.clinical.backend: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

**View SQL Queries:**
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

---

## 📚 Best Practices

### Code Style

- Follow Java naming conventions
- Use meaningful variable names
- Add JavaDoc for public methods
- Keep methods small and focused
- Use dependency injection
- Handle exceptions properly

### Git Workflow

```bash
# Create feature branch
git checkout -b feature/patient-notes

# Make changes and commit
git add .
git commit -m "feat: add patient notes feature"

# Push and create PR
git push origin feature/patient-notes
```

### Commit Messages

Format: `type(scope): description`

Types:
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `test` - Tests
- `refactor` - Code refactoring
- `chore` - Maintenance

Examples:
- `feat(appointments): add email notifications`
- `fix(auth): resolve token expiration issue`
- `docs(readme): update deployment instructions`

---

## 🎓 Learning Resources

### Spring Boot
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

### Angular
- [Angular Documentation](https://angular.io/docs)
- [RxJS Guide](https://rxjs.dev/guide/overview)
- [Angular Best Practices](https://angular.io/guide/styleguide)

### Database
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Flyway Migrations](https://flywaydb.org/documentation/)

### DevOps
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Charts](https://helm.sh/docs/)

---

## ✅ Development Checklist

Before submitting a PR:

- [ ] Code compiles without errors
- [ ] All tests pass (`./mvnw test`)
- [ ] Code coverage ≥ 80%
- [ ] No security vulnerabilities
- [ ] Code follows style guide
- [ ] Documentation updated
- [ ] API documented in Swagger
- [ ] Environment variables documented
- [ ] Database migrations created
- [ ] Logs are meaningful

---

**Happy Coding! 🚀**

For questions or support, check the main [README.md](./README.md) or contact the team.
