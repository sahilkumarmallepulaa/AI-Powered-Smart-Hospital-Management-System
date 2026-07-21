# Software Requirements Specification (SRS)

## AI-Powered Smart Hospital Management System

| Document Information | |
|---|---|
| **Project Name** | AI-Powered Smart Hospital Management System |
| **Version** | 1.0 |
| **Date** | July 21, 2026 |
| **Status** | Draft |
| **Prepared By** | Software Architecture Team |

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Problem Statement](#2-problem-statement)
3. [Objectives](#3-objectives)
4. [Scope of the Project](#4-scope-of-the-project)
5. [User Roles](#5-user-roles)
6. [Functional Requirements](#6-functional-requirements)
7. [Non-Functional Requirements](#7-non-functional-requirements)
8. [System Modules](#8-system-modules)
9. [AI Features](#9-ai-features)
10. [Assumptions](#10-assumptions)
11. [Constraints](#11-constraints)
12. [Future Enhancements](#12-future-enhancements)

---

## 1. Project Overview

The **AI-Powered Smart Hospital Management System** is an enterprise-grade digital platform designed to modernize hospital operations, improve patient care, and support clinical decision-making through intelligent automation.

The system provides a unified web-based interface for patients, doctors, receptionists, and administrators. It centralizes core hospital workflows—such as patient registration, appointment scheduling, electronic health records (EHR), billing, and inventory management—while integrating AI capabilities for symptom analysis, appointment recommendations, and operational insights.

### Key Characteristics

- **Web-based platform** accessible from desktop and mobile browsers
- **Role-based access control** ensuring each user sees only what they need
- **Centralized data management** with secure storage of patient and hospital records
- **AI-assisted workflows** that support—not replace—clinical judgment
- **Modular architecture** allowing independent development and scaling of system components

### Intended Audience for This Document

This SRS is written for stakeholders, developers, testers, and project managers who need a clear understanding of *what* the system must do. It describes requirements in plain language and avoids implementation-specific code.

---

## 2. Problem Statement

Hospitals and clinics today face significant operational and clinical challenges:

| Challenge | Impact |
|---|---|
| **Manual, paper-based processes** | Slow registration, lost records, duplicate data entry |
| **Fragmented systems** | Patient data scattered across departments with no single source of truth |
| **Inefficient appointment management** | Long wait times, overbooked doctors, missed appointments |
| **Limited decision support** | Clinicians lack timely insights from historical patient data |
| **Poor visibility for administrators** | Difficulty tracking inventory, staff workload, and hospital performance |
| **Patient dissatisfaction** | Lack of self-service options and transparency in care journey |

Existing hospital management solutions often address only one area (e.g., billing or scheduling) and lack modern AI capabilities. There is a need for an integrated, secure, and intelligent platform that connects patients, staff, and administrators in a single ecosystem.

---

## 3. Objectives

The primary objectives of this project are:

### 3.1 Operational Efficiency

- Reduce manual data entry and paperwork by at least 60% for front-desk and administrative tasks
- Automate appointment scheduling, reminders, and queue management
- Provide real-time dashboards for hospital administrators

### 3.2 Improved Patient Experience

- Enable patients to register, book appointments, and view medical records online
- Reduce average wait times through intelligent slot allocation and queue optimization
- Send automated notifications for appointments, prescriptions, and lab results

### 3.3 Clinical Support

- Provide doctors with a unified view of patient history, vitals, diagnoses, and prescriptions
- Offer AI-assisted symptom analysis and preliminary insights to support clinical decisions
- Maintain accurate, auditable electronic health records

### 3.4 Data Security and Compliance

- Protect sensitive health information through encryption, authentication, and role-based access
- Maintain audit trails for all critical actions (record updates, access logs, prescription changes)
- Align with healthcare data protection best practices (e.g., HIPAA-inspired controls where applicable)

### 3.5 Scalability and Maintainability

- Design a modular system that can grow from a single clinic to a multi-department hospital
- Support future integration with lab systems, pharmacy systems, and insurance providers

---

## 4. Scope of the Project

### 4.1 In Scope

The following capabilities are included in the initial release:

| Area | Included Capabilities |
|---|---|
| **User Management** | Registration, login, role assignment, profile management |
| **Patient Management** | Patient profiles, medical history, emergency contacts |
| **Appointment Management** | Booking, rescheduling, cancellation, reminders, queue management |
| **Doctor Portal** | Patient list, consultation notes, prescriptions, schedule management |
| **Receptionist Portal** | Walk-in registration, check-in, appointment coordination |
| **Admin Portal** | User management, department management, analytics, inventory overview |
| **Electronic Health Records (EHR)** | Diagnoses, prescriptions, lab results, visit history |
| **AI Features** | Symptom checker, appointment slot recommendations, basic analytics |
| **Notifications** | Email and in-app alerts for appointments and critical updates |
| **Reporting** | Basic operational and clinical reports for administrators |

### 4.2 Out of Scope (Initial Release)

The following are explicitly excluded from the first version but may be considered in future releases:

- Native mobile applications (iOS/Android)
- Integration with external laboratory information systems (LIS)
- Integration with insurance claim processing systems
- Telemedicine / video consultation
- Full pharmacy dispensing and inventory automation
- Hardware integration (bedside monitors, IoT devices)
- Multi-hospital / multi-tenant SaaS deployment
- Regulatory certification (e.g., FDA, CE marking for diagnostic AI)

### 4.3 System Boundaries

```
┌─────────────────────────────────────────────────────────────────┐
│                    HOSPITAL MANAGEMENT SYSTEM                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │ Patient  │  │  Doctor  │  │Reception │  │    Admin     │   │
│  │  Portal  │  │  Portal  │  │  Portal  │  │   Dashboard  │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────┬───────┘   │
│       │              │              │                │           │
│       └──────────────┴──────────────┴────────────────┘           │
│                              │                                   │
│                    ┌─────────▼─────────┐                        │
│                    │   Backend API     │                        │
│                    │  (Business Logic) │                        │
│                    └─────────┬─────────┘                        │
│              ┌───────────────┼───────────────┐                  │
│              │               │               │                  │
│       ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐           │
│       │   MySQL     │ │    Redis    │ │  RabbitMQ   │           │
│       │  Database   │ │   (Cache)   │ │ (Messaging) │           │
│       └─────────────┘ └─────────────┘ └─────────────┘           │
│                              │                                   │
│                    ┌─────────▼─────────┐                        │
│                    │    AI Services    │                        │
│                    │  (OpenAI / ML)    │                        │
│                    └───────────────────┘                        │
└─────────────────────────────────────────────────────────────────┘
         ▲                                    │
         │                                    ▼
    End Users                          Email / SMS Gateway
  (Browser Access)                     (External Service)
```

---

## 5. User Roles

The system defines four primary user roles. Each role has distinct permissions and access to specific modules.

### 5.1 Patient

**Description:** An individual receiving medical care at the hospital. Patients interact with the system primarily through a self-service portal.

**Primary Goals:**
- Register and manage personal profile
- Book, reschedule, or cancel appointments
- View medical history, prescriptions, and lab results
- Receive appointment reminders and health-related notifications
- Use AI symptom checker for preliminary guidance before booking

**Access Level:** Limited to own data only

**Key Permissions:**

| Action | Allowed |
|---|---|
| View own profile and medical records | Yes |
| Book and manage own appointments | Yes |
| View own prescriptions and lab results | Yes |
| Use AI symptom checker | Yes |
| View other patients' records | No |
| Modify hospital settings | No |
| Access admin or doctor dashboards | No |

---

### 5.2 Doctor

**Description:** A licensed medical professional responsible for diagnosing, treating, and managing patients.

**Primary Goals:**
- View assigned patient list and upcoming appointments
- Access complete patient medical history during consultations
- Record diagnoses, prescriptions, and clinical notes
- Manage personal schedule and availability
- Receive AI-assisted insights based on patient symptoms and history

**Access Level:** Access to assigned and consulting patients

**Key Permissions:**

| Action | Allowed |
|---|---|
| View assigned patients' records | Yes |
| Create and update diagnoses and prescriptions | Yes |
| Manage own schedule and availability | Yes |
| Access AI diagnostic support tools | Yes |
| View hospital-wide analytics | No |
| Manage user accounts | No |
| Modify inventory or billing settings | No |

---

### 5.3 Receptionist

**Description:** Front-desk staff responsible for patient intake, appointment coordination, and day-to-day operational support.

**Primary Goals:**
- Register walk-in and new patients
- Check in patients for appointments
- Assist with appointment booking and rescheduling
- Manage daily queue and waiting room flow
- Handle basic patient inquiries

**Access Level:** Operational access; limited clinical write access

**Key Permissions:**

| Action | Allowed |
|---|---|
| Register new patients | Yes |
| Check in patients for appointments | Yes |
| Book, reschedule, cancel appointments (on behalf of patients) | Yes |
| View patient contact and appointment details | Yes |
| View full medical history or write prescriptions | No |
| Access admin analytics or user management | No |
| Modify doctor schedules | No |

---

### 5.4 Admin

**Description:** Hospital administrator or IT manager with full system oversight and configuration authority.

**Primary Goals:**
- Manage user accounts and role assignments
- Configure departments, doctors, and hospital settings
- Monitor system performance and operational analytics
- Oversee inventory and resource allocation
- Generate reports for hospital leadership
- Maintain system security and audit compliance

**Access Level:** Full system access (with audit logging)

**Key Permissions:**

| Action | Allowed |
|---|---|
| Create, update, deactivate user accounts | Yes |
| Assign and modify user roles | Yes |
| Configure departments, specialties, and hospital settings | Yes |
| View all operational and clinical analytics | Yes |
| Manage inventory and resource records | Yes |
| Access audit logs and system configuration | Yes |
| Override clinical records without audit trail | No |

---

### 5.5 Role Hierarchy Summary

```
                    ┌─────────────┐
                    │    Admin    │  ← Full system control
                    └──────┬──────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
   ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
   │   Doctor    │  │ Receptionist│  │   Patient   │
   │  (Clinical) │  │ (Operational)│  │ (Self-serv) │
   └─────────────┘  └─────────────┘  └─────────────┘
```

---

## 6. Functional Requirements

Functional requirements describe *what* the system must do. Each requirement is assigned a unique identifier for traceability.

### 6.1 Authentication and Authorization

| ID | Requirement | Priority |
|---|---|---|
| FR-AUTH-01 | The system shall allow users to register with email, password, and role selection (patient self-registration; other roles assigned by admin) | High |
| FR-AUTH-02 | The system shall authenticate users via secure login (email/username and password) | High |
| FR-AUTH-03 | The system shall issue JWT tokens upon successful authentication for stateless session management | High |
| FR-AUTH-04 | The system shall enforce role-based access control (RBAC) on all API endpoints and UI views | High |
| FR-AUTH-05 | The system shall support password reset via email verification link | Medium |
| FR-AUTH-06 | The system shall lock accounts after a configurable number of failed login attempts | Medium |
| FR-AUTH-07 | The system shall log all authentication events (login, logout, failed attempts) | High |

### 6.2 Patient Management

| ID | Requirement | Priority |
|---|---|---|
| FR-PAT-01 | The system shall allow patients to create and update their profile (name, date of birth, gender, contact, emergency contact) | High |
| FR-PAT-02 | The system shall store patient medical history including allergies, chronic conditions, and past surgeries | High |
| FR-PAT-03 | The system shall assign a unique patient ID upon registration | High |
| FR-PAT-04 | The system shall allow receptionists to register patients on behalf of walk-in visitors | High |
| FR-PAT-05 | The system shall allow patients to upload and view identity documents (optional) | Low |
| FR-PAT-06 | The system shall display a consolidated patient timeline (visits, prescriptions, lab results) | High |

### 6.3 Appointment Management

| ID | Requirement | Priority |
|---|---|---|
| FR-APT-01 | The system shall allow patients to search available doctors by specialty, name, or department | High |
| FR-APT-02 | The system shall display available time slots based on doctor schedule and existing bookings | High |
| FR-APT-03 | The system shall allow patients to book, reschedule, and cancel appointments | High |
| FR-APT-04 | The system shall prevent double-booking of the same doctor time slot | High |
| FR-APT-05 | The system shall send appointment confirmation and reminder notifications (email/in-app) | High |
| FR-APT-06 | The system shall support appointment statuses: Scheduled, Checked-In, In-Progress, Completed, Cancelled, No-Show | High |
| FR-APT-07 | The system shall allow receptionists to check in patients and manage the daily queue | High |
| FR-APT-08 | The system shall allow doctors to view their daily and weekly appointment calendar | High |
| FR-APT-09 | The system shall recommend optimal appointment slots using AI based on urgency and doctor availability | Medium |

### 6.4 Electronic Health Records (EHR)

| ID | Requirement | Priority |
|---|---|---|
| FR-EHR-01 | The system shall allow doctors to record consultation notes during or after a visit | High |
| FR-EHR-02 | The system shall allow doctors to create and manage prescriptions (medication, dosage, duration) | High |
| FR-EHR-03 | The system shall store diagnoses linked to each visit with ICD-compatible codes (where applicable) | High |
| FR-EHR-04 | The system shall record patient vitals (blood pressure, temperature, heart rate, weight) per visit | High |
| FR-EHR-05 | The system shall allow attachment and viewing of lab reports and imaging results | Medium |
| FR-EHR-06 | The system shall maintain a complete audit trail of all EHR modifications | High |
| FR-EHR-07 | The system shall restrict EHR access to authorized roles only (doctor, assigned staff, patient for own records) | High |

### 6.5 Doctor Portal

| ID | Requirement | Priority |
|---|---|---|
| FR-DOC-01 | The system shall display a doctor dashboard with today's appointments, patient queue, and pending tasks | High |
| FR-DOC-02 | The system shall allow doctors to manage their weekly availability and leave schedules | High |
| FR-DOC-03 | The system shall provide quick access to patient history during active consultations | High |
| FR-DOC-04 | The system shall allow doctors to mark appointments as completed and add follow-up recommendations | High |
| FR-DOC-05 | The system shall display AI-generated patient summaries before consultations | Medium |

### 6.6 Receptionist Portal

| ID | Requirement | Priority |
|---|---|---|
| FR-REC-01 | The system shall provide a receptionist dashboard with today's appointments and walk-in queue | High |
| FR-REC-02 | The system shall allow receptionists to register new patients with minimal required fields | High |
| FR-REC-03 | The system shall allow receptionists to book appointments on behalf of patients | High |
| FR-REC-04 | The system shall display real-time queue status (waiting, in-consultation, completed) | High |
| FR-REC-05 | The system shall allow receptionists to print or share appointment confirmations | Medium |

### 6.7 Admin Portal

| ID | Requirement | Priority |
|---|---|---|
| FR-ADM-01 | The system shall provide an admin dashboard with key metrics (patients, appointments, revenue, occupancy) | High |
| FR-ADM-02 | The system shall allow admins to create, update, deactivate, and assign roles to user accounts | High |
| FR-ADM-03 | The system shall allow admins to manage departments, specialties, and doctor-department mappings | High |
| FR-ADM-04 | The system shall allow admins to configure hospital settings (working hours, appointment duration, notification templates) | High |
| FR-ADM-05 | The system shall provide inventory management for medical supplies and equipment | Medium |
| FR-ADM-06 | The system shall generate exportable reports (PDF/CSV) for appointments, patients, and revenue | Medium |
| FR-ADM-07 | The system shall display system audit logs with filtering by user, action, and date range | High |

### 6.8 Notifications

| ID | Requirement | Priority |
|---|---|---|
| FR-NOT-01 | The system shall send email notifications for appointment confirmations, reminders, and cancellations | High |
| FR-NOT-02 | The system shall display in-app notifications for all user roles | Medium |
| FR-NOT-03 | The system shall allow users to configure notification preferences | Low |
| FR-NOT-04 | The system shall queue notification delivery via message broker (RabbitMQ) for reliability | Medium |

### 6.9 Search and Reporting

| ID | Requirement | Priority |
|---|---|---|
| FR-SRCH-01 | The system shall allow authorized users to search patients by name, ID, or phone number | High |
| FR-SRCH-02 | The system shall allow filtering appointments by date, doctor, department, and status | High |
| FR-RPT-01 | The system shall generate daily, weekly, and monthly appointment summary reports | Medium |
| FR-RPT-02 | The system shall generate patient registration and visit trend reports | Medium |

---

## 7. Non-Functional Requirements

Non-functional requirements describe *how well* the system must perform. They define quality attributes, constraints, and operational characteristics.

### 7.1 Performance

| ID | Requirement | Target |
|---|---|---|
| NFR-PERF-01 | API response time for standard CRUD operations | ≤ 500 ms (95th percentile) |
| NFR-PERF-02 | Page load time for dashboard views | ≤ 3 seconds on standard broadband |
| NFR-PERF-03 | Concurrent users supported (initial deployment) | ≥ 500 simultaneous users |
| NFR-PERF-04 | Database query optimization for patient search | ≤ 1 second for up to 100,000 records |
| NFR-PERF-05 | AI symptom analysis response time | ≤ 10 seconds |

### 7.2 Security

| ID | Requirement |
|---|---|
| NFR-SEC-01 | All data in transit shall be encrypted using TLS 1.2 or higher |
| NFR-SEC-02 | Passwords shall be hashed using bcrypt or equivalent strong algorithm |
| NFR-SEC-03 | JWT tokens shall have configurable expiration and refresh mechanisms |
| NFR-SEC-04 | Sensitive health data at rest shall be encrypted in the database |
| NFR-SEC-05 | The system shall implement protection against OWASP Top 10 vulnerabilities (SQL injection, XSS, CSRF) |
| NFR-SEC-06 | All API endpoints shall validate input and sanitize output |
| NFR-SEC-07 | Session management shall invalidate tokens on logout and password change |
| NFR-SEC-08 | Admin actions shall be logged with user identity, timestamp, and action details |

### 7.3 Availability and Reliability

| ID | Requirement | Target |
|---|---|---|
| NFR-AVL-01 | System uptime during business hours | ≥ 99.5% |
| NFR-AVL-02 | Scheduled maintenance windows | Off-peak hours only; ≤ 4 hours/month |
| NFR-AVL-03 | Automated database backups | Daily, with 30-day retention |
| NFR-AVL-04 | Recovery Time Objective (RTO) | ≤ 4 hours |
| NFR-AVL-05 | Recovery Point Objective (RPO) | ≤ 1 hour |

### 7.4 Scalability

| ID | Requirement |
|---|---|
| NFR-SCL-01 | The backend shall be horizontally scalable via containerization (Docker) |
| NFR-SCL-02 | Database read operations shall be cacheable via Redis to reduce load |
| NFR-SCL-03 | Asynchronous tasks (notifications, reports) shall be processed via message queue |
| NFR-SCL-04 | The architecture shall support future migration to Kubernetes orchestration |

### 7.5 Usability

| ID | Requirement |
|---|---|
| NFR-USA-01 | The UI shall be responsive and usable on desktop, tablet, and mobile browsers |
| NFR-USA-02 | New users shall be able to complete patient registration in under 3 minutes |
| NFR-USA-03 | The system shall provide clear error messages and validation feedback |
| NFR-USA-04 | The interface shall follow consistent design patterns across all role-based portals |
| NFR-USA-05 | Critical actions (delete, cancel appointment) shall require confirmation |

### 7.6 Maintainability

| ID | Requirement |
|---|---|
| NFR-MNT-01 | The codebase shall follow modular architecture (separate backend, frontend, database layers) |
| NFR-MNT-02 | API endpoints shall be documented using OpenAPI (Swagger) specification |
| NFR-MNT-03 | Database schema changes shall be managed through version-controlled migrations |
| NFR-MNT-04 | The system shall support environment-based configuration (dev, staging, production) |

### 7.7 Compatibility

| ID | Requirement |
|---|---|
| NFR-CMP-01 | Frontend shall support latest two versions of Chrome, Firefox, Safari, and Edge |
| NFR-CMP-02 | Backend shall run on Java 21 with Spring Boot 3.x |
| NFR-CMP-03 | Database shall be compatible with MySQL 8.0 or higher |

### 7.8 Compliance and Audit

| ID | Requirement |
|---|---|
| NFR-CMP-01 | All access to patient health records shall be logged for audit purposes |
| NFR-CMP-02 | The system shall support data export for patient data portability requests |
| NFR-CMP-03 | AI-generated outputs shall be clearly labeled as assistive—not diagnostic—recommendations |

---

## 8. System Modules

The system is organized into distinct modules, each responsible for a specific domain. Modules communicate through well-defined APIs and shared infrastructure.

### 8.1 Module Overview

```
┌────────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React + Vite)                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│  │ Patient  │ │  Doctor  │ │Reception │ │  Admin   │ │   Auth   │ │
│  │   UI     │ │    UI    │ │   UI     │ │    UI    │ │    UI    │ │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │
└───────────────────────────────┬────────────────────────────────────┘
                                │ REST API (HTTPS)
┌───────────────────────────────▼────────────────────────────────────┐
│                      BACKEND (Spring Boot)                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│  │   Auth   │ │ Patient  │ │Appointment│ │   EHR    │ │  Admin   │ │
│  │  Module  │ │  Module  │ │  Module  │ │  Module  │ │  Module  │ │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐              │
│  │Notification│ │Reporting │ │Inventory │ │    AI    │              │
│  │  Module  │ │  Module  │ │  Module  │ │  Module  │              │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘              │
└───────────────────────────────┬────────────────────────────────────┘
                                │
         ┌──────────────────────┼──────────────────────┐
         │                      │                      │
  ┌──────▼──────┐       ┌──────▼──────┐       ┌──────▼──────┐
  │    MySQL    │       │    Redis    │       │  RabbitMQ   │
  └─────────────┘       └─────────────┘       └─────────────┘
```

### 8.2 Module Descriptions

#### 8.2.1 Authentication Module

**Purpose:** Manages user identity, login, registration, JWT token issuance, and role-based authorization.

**Responsibilities:**
- User registration and login
- Password hashing and validation
- JWT token generation and refresh
- Role and permission enforcement
- Session management and logout

**Dependencies:** User database tables, Redis (token blacklist)

---

#### 8.2.2 Patient Module

**Purpose:** Manages patient demographics, profiles, medical history, and patient-facing self-service features.

**Responsibilities:**
- Patient profile CRUD operations
- Medical history management (allergies, conditions, surgeries)
- Patient search and lookup
- Patient timeline aggregation

**Dependencies:** Authentication Module, EHR Module

---

#### 8.2.3 Appointment Module

**Purpose:** Handles the complete appointment lifecycle from booking to completion.

**Responsibilities:**
- Doctor schedule and availability management
- Slot generation and conflict detection
- Appointment booking, rescheduling, cancellation
- Queue management and check-in
- Appointment status workflow

**Dependencies:** Patient Module, Doctor schedules, Notification Module, AI Module

---

#### 8.2.4 Electronic Health Records (EHR) Module

**Purpose:** Stores and manages all clinical data associated with patient care.

**Responsibilities:**
- Consultation notes and visit records
- Diagnosis and prescription management
- Vitals recording
- Lab report and document attachments
- Audit trail for all record changes

**Dependencies:** Patient Module, Authentication Module

---

#### 8.2.5 Doctor Module

**Purpose:** Provides doctor-specific workflows and dashboard functionality.

**Responsibilities:**
- Doctor profile and specialty management
- Schedule and availability configuration
- Patient list and consultation interface
- Prescription and diagnosis entry

**Dependencies:** Appointment Module, EHR Module, AI Module

---

#### 8.2.6 Admin Module

**Purpose:** Central management hub for hospital configuration, users, and analytics.

**Responsibilities:**
- User account and role management
- Department and specialty configuration
- Hospital settings and policies
- Analytics dashboard and KPI tracking
- Audit log viewing

**Dependencies:** All other modules (read access)

---

#### 8.2.7 Notification Module

**Purpose:** Delivers timely alerts and messages to users across channels.

**Responsibilities:**
- Email notification dispatch
- In-app notification storage and display
- Notification template management
- Delivery queue processing via RabbitMQ

**Dependencies:** RabbitMQ, external email service (SMTP/SendGrid)

---

#### 8.2.8 Reporting Module

**Purpose:** Generates operational and clinical reports for administrators.

**Responsibilities:**
- Appointment summary reports
- Patient registration trends
- Doctor workload reports
- Export to PDF and CSV formats

**Dependencies:** Appointment Module, Patient Module, Admin Module

---

#### 8.2.9 Inventory Module

**Purpose:** Tracks medical supplies, equipment, and hospital resources.

**Responsibilities:**
- Inventory item registration and categorization
- Stock level tracking and low-stock alerts
- Supplier information management
- Usage logging

**Dependencies:** Admin Module, Notification Module

---

#### 8.2.10 AI Module

**Purpose:** Provides intelligent features that augment clinical and operational workflows.

**Responsibilities:**
- Symptom analysis and preliminary assessment
- Appointment slot recommendations
- Patient summary generation for doctors
- Operational analytics and trend detection

**Dependencies:** Patient Module, EHR Module, Appointment Module, external AI API (OpenAI)

*See [Section 9: AI Features](#9-ai-features) for detailed AI capability descriptions.*

---

## 9. AI Features

AI capabilities are designed to **assist** healthcare professionals and improve patient experience. They do not replace clinical judgment or provide definitive medical diagnoses.

### 9.1 AI Design Principles

| Principle | Description |
|---|---|
| **Assistive, not autonomous** | AI outputs are recommendations; final decisions remain with licensed professionals |
| **Transparency** | All AI-generated content is clearly labeled as machine-generated |
| **Privacy-first** | Patient data sent to AI services is anonymized or minimized where possible |
| **Fallback gracefully** | System functions normally if AI services are unavailable |
| **Auditability** | AI interactions are logged for review and compliance |

### 9.2 AI Feature Catalog

#### 9.2.1 AI Symptom Checker (Patient-Facing)

**Description:** Allows patients to describe symptoms in natural language and receive preliminary guidance on possible conditions and recommended next steps (e.g., book appointment, visit emergency room).

**Input:**
- Patient-described symptoms (text)
- Optional: age, gender, duration of symptoms

**Output:**
- List of possible conditions (with confidence indicators)
- Recommended urgency level (Routine, Soon, Urgent, Emergency)
- Suggested specialty or doctor type
- Disclaimer that this is not a medical diagnosis

**Integration Point:** Patient Portal → AI Module → OpenAI API

**Constraints:**
- Must display medical disclaimer prominently
- Must not store symptom data without patient consent
- Emergency symptoms shall trigger immediate "seek emergency care" messaging

---

#### 9.2.2 Smart Appointment Recommendations

**Description:** Recommends optimal appointment slots based on patient urgency, doctor availability, historical patterns, and specialty match.

**Input:**
- Patient profile and symptom urgency (from symptom checker or manual input)
- Doctor availability and schedule
- Historical appointment data

**Output:**
- Ranked list of recommended time slots
- Estimated wait time for each option
- Doctor match score based on specialty and patient history

**Integration Point:** Appointment Module → AI Module

**Constraints:**
- Recommendations are suggestions; patient or receptionist makes final booking decision
- Must respect doctor availability constraints

---

#### 9.2.3 AI Patient Summary (Doctor-Facing)

**Description:** Generates a concise summary of a patient's medical history, recent visits, current medications, and relevant alerts before a consultation.

**Input:**
- Patient EHR data (visits, diagnoses, prescriptions, vitals, allergies)
- Upcoming appointment context

**Output:**
- Structured summary (bullet points)
- Highlighted alerts (drug interactions, allergies, abnormal vitals)
- Recent trend indicators (e.g., blood pressure trend)

**Integration Point:** Doctor Portal → AI Module → EHR Module

**Constraints:**
- Summary is assistive; doctor must review full records
- Sensitive data handling per security requirements

---

#### 9.2.4 Operational Analytics Insights (Admin-Facing)

**Description:** Analyzes hospital operational data to surface trends, anomalies, and actionable insights.

**Input:**
- Appointment volumes, no-show rates, wait times
- Patient registration trends
- Inventory consumption patterns

**Output:**
- Trend reports (weekly/monthly)
- Anomaly alerts (unusual spike in no-shows, low inventory)
- Resource optimization suggestions (staffing, inventory reorder)

**Integration Point:** Admin Dashboard → AI Module → Reporting Module

**Constraints:**
- Aggregated data only; no individual patient identification in analytics

---

### 9.3 AI Technology Stack

| Component | Technology |
|---|---|
| AI Provider | OpenAI API (GPT-based models) |
| Integration | REST API calls from Spring Boot AI Module |
| Caching | Redis (cache frequent AI responses where appropriate) |
| Logging | All AI requests/responses logged with correlation IDs |

### 9.4 AI Safety and Compliance

- All AI outputs include visible disclaimers
- No AI feature shall be marketed or functioning as a diagnostic tool
- Patients must acknowledge terms before using AI symptom checker
- AI module shall implement rate limiting to prevent abuse
- Personally identifiable information (PII) shall be minimized in AI API requests

---

## 10. Assumptions

The following assumptions underpin this SRS. If any assumption proves invalid, requirements may need revision.

| ID | Assumption |
|---|---|
| ASM-01 | The hospital has reliable internet connectivity for web-based system access |
| ASM-02 | Users (patients, staff) have access to modern web browsers (Chrome, Firefox, Safari, Edge) |
| ASM-03 | The hospital will designate an administrator responsible for initial system configuration and user provisioning |
| ASM-04 | Doctors will maintain their availability schedules in the system |
| ASM-05 | An external email service (SMTP or cloud provider) is available for notification delivery |
| ASM-06 | OpenAI API (or equivalent) will be accessible and within budget for AI features |
| ASM-07 | MySQL, Redis, and RabbitMQ infrastructure will be provisioned via Docker Compose for development and staging |
| ASM-08 | Patient self-registration is allowed; other roles are provisioned by administrators |
| ASM-09 | The system will initially serve a single hospital or clinic (not multi-tenant) |
| ASM-10 | Staff will receive basic training on system usage before go-live |
| ASM-11 | Existing paper records may be manually migrated or entered during initial rollout |
| ASM-12 | Regulatory compliance requirements (HIPAA, local healthcare laws) will be clarified with legal counsel before production deployment |

---

## 11. Constraints

Constraints are limitations that bound the project scope, design, or implementation.

### 11.1 Technical Constraints

| ID | Constraint |
|---|---|
| CON-TECH-01 | Backend must be implemented in Java 21 with Spring Boot framework |
| CON-TECH-02 | Frontend must be implemented using React with Vite build tool |
| CON-TECH-03 | Primary database must be MySQL 8.0 or higher |
| CON-TECH-04 | Authentication must use JWT tokens; no server-side session storage for API |
| CON-TECH-05 | System must be containerized using Docker for deployment consistency |
| CON-TECH-06 | Message queuing must use RabbitMQ for asynchronous processing |
| CON-TECH-07 | Caching must use Redis for performance optimization |

### 11.2 Business Constraints

| ID | Constraint |
|---|---|
| CON-BUS-01 | Initial release must support four user roles: Patient, Doctor, Receptionist, Admin |
| CON-BUS-02 | Project must be deliverable in phases; MVP must cover core appointment and patient workflows |
| CON-BUS-03 | AI features must be clearly positioned as assistive tools, not diagnostic devices |
| CON-BUS-04 | System must operate within allocated cloud/infrastructure budget |

### 11.3 Regulatory and Legal Constraints

| ID | Constraint |
|---|---|
| CON-REG-01 | Patient health data must be protected according to applicable healthcare privacy regulations |
| CON-REG-02 | AI-generated content must include disclaimers and not constitute medical advice |
| CON-REG-03 | Audit trails must be maintained for all access to and modifications of health records |
| CON-REG-04 | Data retention and deletion policies must comply with local healthcare data laws |

### 11.4 Operational Constraints

| ID | Constraint |
|---|---|
| CON-OPS-01 | System maintenance windows must be scheduled during off-peak hours |
| CON-OPS-02 | Database backups must occur daily with tested restore procedures |
| CON-OPS-03 | Production deployment requires staging environment validation |

---

## 12. Future Enhancements

The following features are out of scope for the initial release but are planned for future iterations. They are documented here to guide architectural decisions and ensure the system can evolve.

### 12.1 Phase 2 Enhancements

| Enhancement | Description | Priority |
|---|---|---|
| **Telemedicine Integration** | Video consultation between doctors and patients | High |
| **Mobile Applications** | Native iOS and Android apps for patients and doctors | High |
| **SMS Notifications** | Appointment reminders and alerts via SMS gateway | Medium |
| **Billing and Invoicing** | Patient billing, insurance claim support, payment gateway integration | High |
| **Pharmacy Module** | Prescription fulfillment, drug inventory, interaction checking | Medium |
| **Lab Integration** | Connect with external laboratory information systems (LIS) | Medium |

### 12.2 Phase 3 Enhancements

| Enhancement | Description | Priority |
|---|---|---|
| **Multi-Hospital Support** | Multi-tenant architecture for hospital chains | Medium |
| **Advanced AI Diagnostics** | Medical imaging analysis, predictive health risk scoring | Medium |
| **IoT Integration** | Bedside monitor data ingestion, wearable device integration | Low |
| **Kubernetes Deployment** | Production orchestration with auto-scaling and self-healing | Medium |
| **AWS Cloud Migration** | Full cloud deployment with managed services (RDS, ElastiCache, SQS) | Medium |
| **Patient Portal Enhancements** | Health tracking, medication reminders, family account linking | Medium |

### 12.3 Long-Term Vision

| Enhancement | Description |
|---|---|
| **Population Health Analytics** | Aggregate anonymized data for epidemiological insights |
| **AI-Powered Triage** | Automated patient prioritization in emergency departments |
| **Blockchain Health Records** | Immutable, patient-controlled health record sharing |
| **Voice-Enabled Interface** | Voice commands for doctors during consultations |
| **Integration Marketplace** | API ecosystem for third-party healthcare app integrations |

---

## Appendix A: Glossary

| Term | Definition |
|---|---|
| **EHR** | Electronic Health Record — digital version of a patient's paper chart |
| **JWT** | JSON Web Token — compact, URL-safe token for securely transmitting claims |
| **RBAC** | Role-Based Access Control — restricting access based on user roles |
| **HIPAA** | Health Insurance Portability and Accountability Act (US healthcare privacy law) |
| **ICD** | International Classification of Diseases — standard diagnostic codes |
| **CRUD** | Create, Read, Update, Delete — basic data operations |
| **API** | Application Programming Interface — software intermediary for communication |
| **MVP** | Minimum Viable Product — earliest version with core functionality |
| **RTO** | Recovery Time Objective — maximum acceptable downtime after failure |
| **RPO** | Recovery Point Objective — maximum acceptable data loss after failure |

---

## Appendix B: Document Revision History

| Version | Date | Author | Changes |
|---|---|---|---|
| 1.0 | July 21, 2026 | Architecture Team | Initial SRS draft |

---

## Appendix C: Related Documents

| Document | Location | Description |
|---|---|---|
| README | `/README.md` | Project overview and technology stack |
| Architecture Diagrams | `/architecture/` | System design and component diagrams |
| Database Schema | `/database/` | MySQL schema and migrations |

---

*End of Software Requirements Specification*
