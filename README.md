# Secure Document Management & Authentication API

A production-grade backend REST API for **secure document upload, storage, retrieval, and authentication** built with **Spring Boot**, **Spring Security**, **JWT (30-Day Token)**, **Two-Factor Authentication (2FA / OTP)**, **Role-Based Access Control (RBAC)**, **HTTPS / SSL (TLS) with PKCS#12 Keystore**, and **JPA/Hibernate (H2 / PostgreSQL)**.

---

## 🚀 Key Features

### 🔐 Security & Authentication
- **HTTPS / SSL (TLS) Encryption**: Runs exclusively over HTTPS on port `8443` using an RSA PKCS#12 keystore (`keystore.p12`).
- **Auto-Generated Initial Password on Registration**:
  - Registers users with full name and email, automatically generating a secure password in the format `<FirstName>@<5-digit-number>` (e.g., `Sayani@84920`) and emailing the credentials.
- **Forgot & Reset Password Workflow**:
  - Dispatches a 6-digit OTP (5-minute validity) for password reset, updates the BCrypt-hashed password, and sends a confirmation notification email.
- **Device-Based Two-Factor Authentication (2FA / OTP)**:
  - Recognizes trusted devices via `deviceId`.
  - Unrecognized devices trigger a 6-digit OTP email alert to the registered user.
  - Verifying the OTP (`/api/v1/auth/verify-otp`) trusts the device and issues a **30-day JWT Bearer token**.
- **Clean Profile-Only Login Response**:
  - Subsequent logins on recognized devices (with credentials or 30-day token) return a lightweight user profile response without re-issuing tokens.
- **Custom Exception Handling (`CustomMessageException`)**:
  - Centralized `@RestControllerAdvice` returns **HTTP 200 OK** with structured error responses (`status: "FAIL"`, `message`, `success: false`, `timestamp`) for clean frontend/client handling.

### 📁 Secure Document Management
- **Authenticated Document Upload & Streaming Download**:
  - Multipart file upload (`multipart/form-data`) protected by Spring Security JWT Bearer authentication.
  - Streaming file downloads via Spring `UrlResource` with original filename (`Content-Disposition`) and MIME type headers.
- **Secure Storage Architecture**:
  - Physical files are saved to a configurable local directory (`uploads/documents/`) using UUID-obfuscated filenames to prevent collisions and path traversal attacks.
  - Structured metadata (`id`, `original_file_name`, `stored_file_name`, `file_type`, `file_size`, `file_path`, `user_id`, `uploaded_at`) is persisted in the relational database.
- **Role-Based Access Control (RBAC)**:
  - **`ROLE_USER`**: Can upload documents and list, view, download, and delete **only their own** uploaded documents.
  - **`ROLE_ADMIN`**: Full administrative access to view, download, and manage documents across all users.
- **Database Support**:
  - Zero-setup **H2 In-Memory Database** enabled by default for instant local testing, with **PostgreSQL** driver and configuration pre-wired in `pom.xml` and `application.properties`.

---

## 🛠️ Tech Stack

- **Backend**: Java 17+, Spring Boot 4.x, Spring Web MVC, Spring Security, Spring Data JPA, Spring Mail
- **Security**: JJWT (`0.12.6`), BCrypt Password Hashing, HTTPS/TLS (PKCS#12 RSA Keystore), Stateless Sessions
- **Database**: H2 (Default Dev/Demo), PostgreSQL (Production Ready)
- **Tools**: Maven Wrapper (`mvnw`), Lombok, JUnit 5, Postman

---

## 📂 Project Structure

```text
src/main/java/com/sayani/secure_auth_api/
├── controller/
│   ├── AuthController.java          # /register, /login, /verify-otp, /forgot-password, /reset-password
│   └── DocumentController.java      # /upload, /list-documens, /get-document-by-id, /download, /delete-document
├── dto/
│   ├── AuthResponse.java            # Standardized auth response payload
│   ├── DocumentResponse.java        # Document metadata & download URI response DTO
│   ├── ForgotPasswordRequest.java   # Request DTO for password reset OTP
│   ├── LoginRequest.java            # Login DTO (supports credentials or 30-day token)
│   ├── RegisterRequest.java         # User registration DTO (auto-generates password if omitted)
│   ├── ResetPasswordRequest.java    # Request DTO with OTP & new password
│   └── VerifyOtpRequest.java        # 2FA OTP verification DTO
├── entity/
│   ├── Document.java                # Document JPA Entity (metadata & owner relationship)
│   ├── Role.java                    # User role enum (ROLE_USER, ROLE_ADMIN)
│   └── User.java                    # User JPA Entity (implements UserDetails, device & OTP tracking)
├── exception/
│   ├── CustomMessageException.java  # Custom business exception returning HTTP 200 with message
│   └── GlobalExceptionHandler.java  # Centralized @RestControllerAdvice exception handler
├── repository/
│   ├── DocumentRepository.java      # Spring Data JPA Repository for documents
│   └── UserRepository.java          # Spring Data JPA Repository for users
├── security/
│   ├── ApplicationConfig.java       # PasswordEncoder, UserDetailsService, AuthenticationManager
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter for Bearer token validation
│   ├── JwtService.java              # 30-day JJWT token generation & validation service
│   └── SecurityConfig.java          # Stateless Spring Security Filter Chain configuration
├── service/
│   ├── AuthService.java             # Authentication, auto-password, 2FA/OTP & password reset logic
│   ├── DocumentService.java         # Document management service interface
│   ├── EmailService.java            # SMTP email service with console logger fallback
│   └── impl/
│       └── DocumentServiceImpl.java # Secure file storage, streaming download & RBAC implementation
└── SecureAuthApiApplication.java    # Spring Boot application entry point
```

---

## 🧪 Complete API Reference & Postman Workflow

> **Postman Setup**:
> 1. Import **`Secure_auth_api.postman_collection.json`** from the project root into Postman (**File → Import**).
> 2. Disable SSL verification for the local self-signed certificate: **Settings → General → SSL certificate verification = OFF**.

### Authentication APIs (`/api/v1/auth`)

| # | Endpoint | Method | Description |
| :-: | :--- | :---: | :--- |
| 1 | `/api/v1/auth/register` | `POST` | Register with `name` & `email` (auto-generates password & sends email) |
| 2 | `/api/v1/auth/forgot-password` | `POST` | Send a 6-digit password reset OTP to the registered email |
| 3 | `/api/v1/auth/reset-password` | `POST` | Verify reset OTP, update password, and send confirmation email |
| 4 | `/api/v1/auth/login` | `POST` | Login from a new device (triggers 2FA OTP) or trusted device (returns profile) |
| 5 | `/api/v1/auth/verify-otp` | `POST` | Verify 2FA OTP, trust device, and issue a **30-day JWT Bearer token** |

#### Example: Register User
```json
// POST https://localhost:8443/api/v1/auth/register
{
  "name": "Sayani Ghosh",
  "email": "sayani@example.com"
}
```

#### Example: Verify 2FA OTP (Issues 30-Day Token)
```json
// POST https://localhost:8443/api/v1/auth/verify-otp
{
  "email": "sayani@example.com",
  "otp": "123456",
  "deviceId": "device-A"
}
```

#### Example: Login from Trusted Device-A
```json
// Response (200 OK)
{
  "name": "Sayani Ghosh",
  "email": "sayani@example.com",
  "role": "ROLE_USER",
  "message": "Authentication successful (Recognized Device)",
  "twoFactorRequired": false,
  "deviceId": "device-A"
}
```

---

### Secure Document Management APIs (`/api/v1/documents`)

> All document endpoints require header: `Authorization: Bearer <YOUR_30_DAY_JWT_TOKEN>`

| # | Endpoint | Method | Params / Body | Description |
| :-: | :--- | :---: | :--- | :--- |
| 6 | `/api/v1/documents/upload` | `POST` | `form-data` → `file` (File) | Upload a document securely to storage |
| 7 | `/api/v1/documents/list-documens` | `GET` | — | List accessible documents (RBAC: own files for User, all for Admin) |
| 8 | `/api/v1/documents/get-document-by-id` | `GET` | Query: `id=1` | Fetch metadata for a specific document |
| 9 | `/api/v1/documents/download` | `GET` | Query: `id=1` | Stream & download the original file |
| 10 | `/api/v1/documents/delete-document` | `DELETE` | Query: `id=1` | Delete document from disk and database (RBAC enforced) |

#### Example: Upload Document Response (`201 CREATED`)
```json
{
  "id": 1,
  "fileName": "security_report.pdf",
  "fileType": "application/pdf",
  "fileSize": 1048576,
  "uploadedBy": "sayani@example.com",
  "uploadedAt": "2026-11-18T12:35:00",
  "downloadUrl": "/api/v1/documents/download/1",
  "message": "Document uploaded successfully."
}
```

---

## ⚙️ Running Locally

### Start the Application
```powershell
.\mvnw.cmd spring-boot:run
```
The server starts over HTTPS at **`https://localhost:8443`**.

### Run Unit & Integration Tests
```powershell
.\mvnw.cmd test
```

### Switching to PostgreSQL (Optional)
In `src/main/resources/application.properties`, comment out the H2 datasource lines and uncomment the PostgreSQL configuration block with your local database credentials.

---

## 📄 License & Terms of Use

Copyright (c) 2026 Sayani Ghosh. All rights reserved.

This project is licensed under a **Proprietary License** for portfolio and evaluation purposes only. Unauthorized copying, redistribution, modification, commercial use, or re-publishing without express written permission is strictly prohibited. See the [LICENSE](LICENSE) file for complete details.

