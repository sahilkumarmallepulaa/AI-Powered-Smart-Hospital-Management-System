# Enterprise Project Guidelines & Development Standards

## AI-Powered Smart Hospital Management System

---

## Document Control

| Information | Details |
|---|---|
| **Document Title** | Project Rules & Architectural Guidelines |
| **System Name** | AI-Powered Smart Hospital Management System |
| **Version** | 1.0.0 |
| **Status** | Active / Mandatory |
| **Author** | Principal Java Software Architect |
| **Target Audience** | All Software Engineers, AI Coding Assistants, Reviewers |

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Architecture Principles](#3-architecture-principles)
4. [Coding Standards](#4-coding-standards)
5. [Spring Boot Guidelines](#5-spring-boot-guidelines)
6. [Database Guidelines](#6-database-guidelines)
7. [API Standards](#7-api-standards)
8. [Git Workflow](#8-git-workflow)
9. [Security Rules](#9-security-rules)
10. [Logging Rules](#10-logging-rules)
11. [Testing Strategy](#11-testing-strategy)
12. [AI Development Rules](#12-ai-development-rules)

---

## 1. Project Overview

### 1.1 Purpose of the Project
The **AI-Powered Smart Hospital Management System** is a modern, enterprise-grade digital healthcare platform designed to streamline hospital operations, automate patient workflows, digitize clinical records, and support medical decision-making through integrated artificial intelligence (AI).

### 1.2 Core Goals
* **Operational Efficiency**: Reduce manual paper-based processes and administrative overhead by at least 60%.
* **Seamless Patient Experience**: Provide self-service tools for patient registration, appointment booking, medical timeline viewing, and AI-assisted symptom screening.
* **Clinical Decision Support**: Equipping clinicians with unified Electronic Health Records (EHR), structured consultation note-taking, and AI-driven clinical summaries.
* **Data Privacy & Compliance**: Maintain strict audit logs, role-based access control (RBAC), and encryption standards aligned with healthcare privacy regulations (HIPAA-inspired controls).
* **Scalable Architecture**: Build a maintainable Modular Monolith capable of smoothly evolving into microservices as system load and team size scale.

### 1.3 Target Users
1. **Patient**: Books appointments, views prescriptions and medical records, completes symptom checks.
2. **Doctor**: Reviews assigned patients, records consultation notes, diagnoses, writes prescriptions, manages schedule.
3. **Receptionist**: Handles walk-in registration, check-ins, appointment scheduling, and queue coordination.
4. **Admin**: Manages user accounts, hospital departments, system policies, analytics dashboards, and audit logs.

---

## 2. Technology Stack

### 2.1 Backend
* **Language**: Java 21 (LTS)
* **Framework**: Spring Boot 3.x
* **Build Tool**: Apache Maven
* **Security**: Spring Security 6.x + JSON Web Tokens (JWT)
* **Data Access**: Spring Data JPA + Hibernate ORM
* **Database**: MySQL 8.0+
* **Cache**: Redis 7.x
* **Messaging**: RabbitMQ 3.x

### 2.2 Frontend
* **Core Library**: React 18+
* **Build Tool**: Vite
* **HTTP Client**: Axios
* **Routing**: React Router v6+
* **Styling**: Vanilla CSS / Custom Design System (Strict modular styling)

### 2.3 DevOps & Infrastructure
* **Containerization**: Docker & Docker Compose
* **Version Control**: Git
* **Repository & CI/CD**: GitHub & GitHub Actions

### 2.4 Documentation & API Specs
* **API Documentation**: OpenAPI 3.0 (Swagger UI via `springdoc-openapi-starter-webmvc-ui`)

---

## 3. Architecture Principles

### 3.1 Modular Monolith Baseline
The system will initially be built as a **Modular Monolith**. 

```
                                ┌─────────────────────────────────────────────────┐
                                │             Spring Boot Application             │
                                │                                                 │
  Client (React SPA) ──(HTTPS)─►│  [Auth]    [Users]    [Patients]   [Doctors]    │
                                │  [Appts]   [EHR]      [Billing]    [Notif]      │
                                │  [AI]                                           │
                                └────────────────────────┬────────────────────────┘
                                                         │
                                               ┌─────────┴─────────┐
                                               │   MySQL / Redis   │
                                               └───────────────────┘
```

#### Key Rules for Monolith Modularity:
* **Package-by-Feature**: Code is organized logically by business domain rather than technical layer alone.
* **Strict Module Boundaries**: Modules must not directly query another module's database tables or internal entities. Inter-module interaction occurs exclusively via public Service Interfaces or asynchronous Spring Application Events.
* **Prepared for Microservices (Strangler Fig Pattern)**: Every module is designed so it can be extracted into an independent microservice in Phase 3 without refactoring business logic.

---

### 3.2 System Modules

| Module Name | Responsibility | Package Path | Primary Entities |
|---|---|---|---|
| **Authentication** | User login, JWT token issuance, refresh token management, password resets | `com.hospital.auth` | `User`, `Role`, `RefreshToken` |
| **Users** | Core user entity, account status, user role mappings | `com.hospital.user` | `User`, `Role` |
| **Patients** | Patient demographics, medical history, emergency contacts, patient profiles | `com.hospital.patient` | `Patient`, `MedicalHistory` |
| **Doctors** | Doctor credentials, specialties, department mappings, schedules, leaves | `com.hospital.doctor` | `Doctor`, `Department`, `DoctorSchedule` |
| **Appointments** | Booking lifecycle, slot generation, availability checks, queue check-ins | `com.hospital.appointment` | `Appointment`, `AppointmentSlot` |
| **Medical Records** | Clinical consultation notes, vitals, ICD diagnoses, visit history | `com.hospital.ehr` | `MedicalRecord`, `Vitals` |
| **Prescriptions** | Prescribed medications, dosage instructions, duration, medicine catalog | `com.hospital.prescription` | `Prescription`, `Medicine` |
| **Billing** | Invoicing, line item charges, payment tracking, financial receipts | `com.hospital.billing` | `Invoice`, `Payment` |
| **Notifications** | Asynchronous email/in-app notification delivery via RabbitMQ | `com.hospital.notification` | `Notification`, `NotificationTemplate` |
| **AI Services** | External OpenAI integration for symptom checker, slot recommendations, patient summaries | `com.hospital.ai` | `AiLog` |

---

## 4. Coding Standards

### 4.1 Naming Conventions

| Category | Standard | Example |
|---|---|---|
| **Package Names** | All lowercase, singular, domain-focused | `com.hospital.appointment.controller` |
| **Class Names** | `PascalCase`, noun-based, clear responsibility | `AppointmentService`, `PatientResponse` |
| **Interface Names** | `PascalCase`, noun or adjective | `NotificationService`, `Auditable` |
| **Method Names** | `camelCase`, verb-first, descriptive | `getPatientById()`, `cancelAppointment()` |
| **Variable Names** | `camelCase`, meaningful names | `scheduledAt`, `patientRepository` |
| **Constants** | `UPPER_SNAKE_CASE` | `MAX_LOGIN_ATTEMPTS`, `DEFAULT_PAGE_SIZE` |
| **REST Endpoints** | Lowercase, plural nouns, kebab-case paths | `/api/v1/appointments`, `/api/v1/doctor-schedules` |

---

### 4.2 Folder Organization

#### Backend (Spring Boot):
```
backend/src/main/java/com/hospital/
├── config/                  # Global Spring security, CORS, Redis, RabbitMQ config
├── shared/                  # Common DTOs, global exceptions, utility classes
├── auth/                    # Auth module
│   ├── controller/          # REST Endpoints
│   ├── service/             # Business Logic & Interfaces
│   ├── repository/          # Spring Data Repositories
│   ├── model/               # JPA Entities
│   ├── dto/                 # Request/Response DTOs
│   └── mapper/              # Entity <-> DTO Mappers
├── patient/                 # Patient domain module
├── appointment/             # Appointment domain module
└── ...                      # Other domain modules
```

#### Frontend (React + Vite):
```
frontend/src/
├── api/                     # Axios clients and API helper endpoints
├── auth/                    # Auth Context, token storage, protected routes
├── components/              # Reusable UI components
│   ├── common/              # Buttons, Modals, Tables, Input fields
│   └── layout/              # Navbar, Sidebar, Footer
├── pages/                   # Page components grouped by role
│   ├── patient/
│   ├── doctor/
│   ├── receptionist/
│   └── admin/
├── hooks/                   # Custom React hooks
├── utils/                   # Formatter helpers, date functions, validators
└── styles/                  # Global CSS theme & modular styles
```

---

## 5. Spring Boot Guidelines

### 5.1 Controller Layer (`@RestController`)
* Controllers must be **thin**. Their sole responsibility is handling HTTP requests, performing DTO validation, invoking service methods, and returning standardized API responses.
* **No business logic** or JPA repository calls allowed in controllers.
* Always use explicit HTTP mapping annotations (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`).
* Apply `@Valid` on request body parameters.

### 5.2 Service Layer (`@Service`)
* Services contain **all business logic**, transactional boundaries, and domain orchestration.
* Service classes must mark read-only methods with `@Transactional(readOnly = true)` and write methods with `@Transactional`.
* Services must accept DTOs or primitive parameters and return DTOs or domain models—**never leak HTTP-specific objects** (`HttpServletRequest`, etc.) into the service layer.

### 5.3 Repository Layer (`@Repository`)
* Repositories extend `JpaRepository<Entity, Long>`.
* Use Spring Data JPA query methods where possible. For complex queries, use explicit JPQL or native SQL using `@Query` with parameter binding (`:param`).
* Never put business logic in repositories.

### 5.4 DTO (Data Transfer Object) Usage
* **Never expose JPA Entities directly in API endpoints.**
* Every endpoint must use specific Request DTOs (e.g., `CreateAppointmentRequest`) and Response DTOs (e.g., `AppointmentResponse`).
* DTOs should be immutable (Java `record` types or final fields with Lombok `@Value` / `@Getter`).

### 5.5 Entity Usage
* JPA entities must be annotated with `@Entity` and `@Table(name = "plural_name")`.
* Use `@Id` with `@GeneratedValue(strategy = GenerationType.IDENTITY)` for primary keys.
* Never expose raw public fields; use private fields with getter/setter access.
* Entities must override `equals()` and `hashCode()` based on business key or ID.

### 5.6 Validation & Exception Handling
* Validate incoming payloads using `jakarta.validation` annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Future`).
* Handle all exceptions centrally using `@RestControllerAdvice` (`GlobalExceptionHandler`).
* Never return raw stack traces to the client. Map custom business exceptions (e.g., `ResourceNotFoundException`, `AppointmentConflictException`) to structured JSON error responses with appropriate HTTP status codes.

---

## 6. Database Guidelines

### 6.1 Naming & Structure
* **Table Names**: Lowercase, plural, `snake_case` (e.g., `patients`, `medical_records`).
* **Column Names**: Lowercase, singular, `snake_case` (e.g., `first_name`, `scheduled_date`).
* **Foreign Key Columns**: `{referenced_table_singular}_id` (e.g., `patient_id`, `doctor_id`).

### 6.2 Primary Keys & Indexes
* Primary keys must use `BIGINT AUTO_INCREMENT`.
* Add explicit database indexes (`idx_{table}_{column}`) on foreign keys, unique fields, and columns frequently filtered in `WHERE` clauses (e.g., `status`, `scheduled_date`, `email`).

### 6.3 Auditing & Soft Deletes
* Every table must contain audit timestamps:
  * `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  * `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
* Critical entities (e.g., `users`, `patients`, `medicines`) must use soft deletion via an `is_active BOOLEAN NOT NULL DEFAULT TRUE` flag instead of physical hard deletion.

### 6.4 Normalization
* The database must adhere strictly to **Third Normal Form (3NF)** to ensure data integrity and avoid update anomalies.
* Intentional denormalization is allowed only for performance reasons and must be explicitly documented in [`database/Database-Design.md`](file:///c:/Users/sahil/OneDrive/Documents/resume%20building/AI-Powered-Smart-Hospital-Management-System/database/Database-Design.md).

---

## 7. API Standards

### 7.1 Standard API Response Wrapper
All REST API endpoints must return data wrapped in a uniform JSON envelope:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-08-05T20:00:00Z"
}
```

### 7.2 Standard Error Response Wrapper
When an error occurs, the API must return a structured error payload:

```json
{
  "success": false,
  "message": "Validation failed",
  "errorCode": "INVALID_INPUT",
  "errors": [
    {
      "field": "email",
      "message": "Email address must be valid"
    }
  ],
  "timestamp": "2026-08-05T20:00:00Z"
}
```

### 7.3 Standard Pagination Envelope
Paged list endpoints (`GET`) must return structured pagination metadata:

```json
{
  "success": true,
  "message": "Patients fetched successfully",
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  },
  "timestamp": "2026-08-05T20:00:00Z"
}
```

### 7.4 HTTP Status Code Usage

| HTTP Code | Status | Usage |
|---|---|---|
| **200** | OK | Successful GET, PUT, or DELETE request |
| **201** | Created | Successful POST request creating a new resource |
| **204** | No Content | Successful request returning no body |
| **400** | Bad Request | Validation failure, malformed JSON |
| **401** | Unauthorized | Missing or invalid JWT token |
| **403** | Forbidden | Valid JWT token, but user lacks required role/permission |
| **404** | Not Found | Resource ID does not exist |
| **409** | Conflict | Double-booking, duplicate email, business rule violation |
| **422** | Unprocessable Entity | Business validation failure |
| **500** | Internal Server Error | Unhandled server exception |

---

## 8. Git Workflow

### 8.1 Branch Naming Conventions
* **Features**: `feature/<module>-<short-description>` (e.g., `feature/auth-jwt-implementation`)
* **Bug Fixes**: `bugfix/<module>-<short-description>` (e.g., `bugfix/appointment-slot-overlap`)
* **Hotfixes**: `hotfix/<short-description>` (e.g., `hotfix/cors-header-fix`)

### 8.2 Commit Message Standard (Conventional Commits)
Format: `<type>(<scope>): <short description>`

#### Types:
* `feat`: A new feature
* `fix`: A bug fix
* `docs`: Documentation changes
* `refactor`: Code refactoring without changing functionality
* `test`: Adding or updating tests
* `chore`: Maintenance tasks, build configuration updates

#### Examples:
```bash
feat(auth): implement JWT token generation and validation filter
fix(appointment): prevent double booking for the same doctor slot
docs(api): add OpenAPI annotations for patient controller
```

### 8.3 Pull Request Expectations
* PRs must target `main` (or designated dev branch).
* Clean build: `mvn clean test` must pass cleanly without failures.
* Self-code review completed against this `PROJECT_RULES.md` document.

---

## 9. Security Rules

### 9.1 Authentication & Tokens
* Authentication must be stateless via **Spring Security** and **JWT**.
* Access Tokens must have short expiration times (15 to 60 minutes).
* Refresh Tokens must be securely stored in Redis with revocation checks upon logout.

### 9.2 Password Security
* All user passwords must be hashed using **BCrypt** with a minimum strength factor of 10 (`BCryptPasswordEncoder`).
* Plaintext passwords must **never** be stored, logged, or returned in API responses.

### 9.3 Role-Based Access Control (RBAC)
* Enforce RBAC at the backend API layer using `@PreAuthorize("hasRole('ROLE_NAME')")` or `@PreAuthorize("hasAnyRole(...)")`.
* The frontend must respect role permissions, but **backend authorization is mandatory** for all protected routes.

### 9.4 Input Sanitization & Injection Defense
* All user input must be sanitized to prevent Cross-Site Scripting (XSS).
* SQL Injection is strictly prohibited: use Spring Data JPA parameter binding or PreparedStatements exclusively. **Never concatenate strings into dynamic SQL statements.**

---

## 10. Logging Rules

### 10.1 Logging Framework & Standard
* Use **SLF4J** with Logback (`@Slf4j` via Lombok).
* Log messages must be clear, structured, and informative.

### 10.2 Log Levels

| Level | Purpose | Example Use Case |
|---|---|---|
| `ERROR` | System failures requiring developer action | Database connection failures, unhandled exceptions, external API downtime |
| `WARN` | Recoverable unexpected situations | High latency detected, failed login attempt, fallback circuit triggered |
| `INFO` | Major business workflow milestones | User registered, appointment booked, prescription created |
| `DEBUG` | Fine-grained internal debugging | Detailed method parameters, token parsing steps (dev environment only) |

### 10.3 STRICT PROHIBITION — Never Log Sensitive Data
Under no circumstances should the following data be written to log files:
* ❌ Plaintext passwords or credentials
* ❌ Raw JWT tokens or secret keys
* ❌ Credit card numbers or payment gateway tokens
* ❌ Full Personally Identifiable Information (PII) such as unmasked national ID numbers or SSNs

---

## 11. Testing Strategy

### 11.1 Unit Tests
* **Framework**: JUnit 5 + Mockito.
* Focus: Isolate service layer business logic, utility classes, and custom validators.
* Aim for >80% coverage on core domain services.

### 11.2 Integration Tests
* **Framework**: `@SpringBootTest`, `@DataJpaTest`, `@AutoConfigureMockMvc`.
* Focus: Test database repositories, JPA queries, Spring Security filter chains, and end-to-end service orchestration using an in-memory database (H2) or test containers.

### 11.3 API Verification
* All REST controllers must have corresponding `MockMvc` test suites validating HTTP status codes, JSON payload structures, security rules, and error handling.

---

## 12. AI Development Rules

Any AI Agent, Assistant, or Software Engineer writing code for this repository must strictly adhere to the following rules:

1. **Mandatory Compliance**: Read and adhere strictly to this `PROJECT_RULES.md` file before generating or modifying any code.
2. **Scope Isolation**: Never modify unrelated files or change directory structures unless explicitly instructed.
3. **Architectural Rationale**: Always explain architectural decisions, tradeoffs, and design choices when submitting code changes.
4. **Consistency**: Maintain absolute consistency with existing package structures, naming conventions, security patterns, and API response wrappers.
5. **Production Quality**: Generated code must be production-grade—including proper exception handling, parameter validation, transactional bounds, and logging. No dummy fallbacks, swallow-exception blocks, or hardcoded secrets.
6. **Ambiguity Resolution**: If a user request or requirement is ambiguous, **stop and ask for explicit clarification** before generating code.

---

*End of Project Rules & Architectural Guidelines*
