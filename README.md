# Secure Auth REST API (Spring Boot & SSL/TLS, 2FA/OTP & JWT)

A production-grade, secure authentication and authorization REST API built with **Spring Boot**, **Spring Security**, **Two-Factor Authentication (2FA / OTP)**, **Email Notification Service**, **JSON Web Tokens (JWT)**, and **HTTPS / SSL (TLS)** using an RSA PKCS#12 Keystore.

---

## 🚀 Key Features

- **HTTPS / SSL (TLS) Encryption**: Server runs exclusively over HTTPS on port `8443` using a local PKCS#12 RSA keystore (`keystore.p12`).
- **Two-Factor Authentication (2FA) & Device Recognition**:
  - Automatically identifies user devices via `deviceId`.
  - When logging in from a recognized/trusted device, direct JWT authentication is granted.
  - When an unrecognized or different device attempts login, a cryptographically secure 6-digit OTP is generated (valid for 5 minutes) and dispatched to the registered user's email.
- **Email Service**:
  - **Account Registration**: Dispatches a welcome confirmation email with user credentials.
  - **Security Alerts & OTP**: Sends real-time security alerts with the OTP code when new device logins are detected.
  - In development/testing, all dispatched emails & OTP codes are logged prominently to the terminal console so testing is seamless even without an external SMTP server.
- **Dynamic Credentials-Based JWT Authentication**:
  - JWT tokens are generated dynamically upon user authentication without needing external static properties in `application.properties`.
- **Spring Security Architecture**:
  - Stateless session policy (`SessionCreationPolicy.STATELESS`).
  - Custom `JwtAuthenticationFilter` validating Bearer tokens on protected endpoints.
  - `BCryptPasswordEncoder` for cryptographic password hashing.
  - Role-based authorization (`ROLE_USER`, `ROLE_ADMIN`).
- **In-Memory H2 Database**: Automated entity schema generation with JPA / Hibernate.
- **Centralized Exception Handling**: `@RestControllerAdvice` returning formatted, standardized JSON responses for validation and authentication errors.

---

## 🛠️ Project Structure

```
src/main/java/com/sayani/secure_auth_api/
├── controller/
│   ├── AuthController.java          # /register, /login, and /verify-otp endpoints
│   └── DemoController.java          # Protected test endpoints (requires JWT)
├── dto/
│   ├── AuthResponse.java            # Standardized response payload (status, token, 2FA flag)
│   ├── LoginRequest.java            # Login DTO with optional deviceId
│   ├── RegisterRequest.java         # User registration DTO
│   └── VerifyOtpRequest.java        # 2FA OTP verification DTO
├── entity/
│   ├── Role.java                    # User role enum (ROLE_USER, ROLE_ADMIN)
│   └── User.java                    # User JPA Entity (implements UserDetails, device & OTP tracking)
├── exception/
│   └── GlobalExceptionHandler.java  # Centralized error handling
├── repository/
│   └── UserRepository.java          # Spring Data JPA Repository
├── security/
│   ├── ApplicationConfig.java       # PasswordEncoder, UserDetailsService, AuthManager
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter for Bearer token validation
│   ├── JwtService.java              # JJWT token generation and validation service
│   └── SecurityConfig.java          # Spring Security Filter Chain configuration
├── service/
│   ├── AuthService.java             # Core authentication, device check & OTP logic
│   └── EmailService.java            # Email dispatch service with console fallback
└── SecureAuthApiApplication.java    # Spring Boot main entry point
```

---

## ⚙️ Configuration (`src/main/resources/application.properties`)

```properties
# Application Name
spring.application.name=secure-auth-api

# SSL / HTTPS Configuration (Keystore generated via keytool)
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=password123
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=authkeystore

# In-Memory H2 Database
spring.datasource.url=jdbc:h2:mem:authdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 🧪 Postman & cURL Testing Workflow

> **Note on Self-Signed SSL Certificate:**
> Because the keystore uses a self-signed certificate for `localhost`, add `-k` or `--insecure` with `curl`, or in **Postman Settings -> General -> Turn OFF "SSL certificate verification"**.
> You can also import the provided **`secure-auth-api.postman_collection.json`** directly into Postman!

### Step 1: Register User (Triggers Registration Email)
```bash
curl -k -X POST https://localhost:8443/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sayani",
    "email": "sayani@example.com",
    "password": "password123"
  }'
```
**Response (`201 Created`):**
```json
{
  "status": "REGISTERED",
  "name": "Sayani",
  "email": "sayani@example.com",
  "role": "ROLE_USER",
  "message": "User registered successfully. Credentials sent to registered email."
}
```

---

### Step 2: Login from Device-A (First Time -> 2FA Triggered)
```bash
curl -k -X POST https://localhost:8443/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sayani@example.com",
    "password": "password123",
    "deviceId": "device-A"
  }'
```
**Response:**
```json
{
  "status": "OTP_REQUIRED",
  "email": "sayani@example.com",
  "name": "Sayani",
  "deviceId": "device-A",
  "twoFactorRequired": true,
  "message": "Unrecognized device detected. A 6-digit OTP has been sent to your registered email."
}
```
*(Check your console/terminal for the OTP code banner)*

---

### Step 3: Verify OTP & Register Device-A (Returns JWT)
```bash
curl -k -X POST https://localhost:8443/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sayani@example.com",
    "otp": "<6_DIGIT_OTP_FROM_CONSOLE>",
    "deviceId": "device-A"
  }'
```
**Response (`200 OK`):**
```json
{
  "status": "SUCCESS",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "name": "Sayani",
  "email": "sayani@example.com",
  "role": "ROLE_USER",
  "deviceId": "device-A",
  "twoFactorRequired": false,
  "message": "Two-Factor Authentication verified successfully. Device registered."
}
```

---

### Step 4: Login Again from Recognized Device-A (Direct Login -> Returns JWT)
```bash
curl -k -X POST https://localhost:8443/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sayani@example.com",
    "password": "password123",
    "deviceId": "device-A"
  }'
```
**Response (`200 OK`):**
Directly returns JWT without requiring OTP!

---

### Step 5: Someone Else Trying to Login from Device-B (Alerts & Sends OTP)
```bash
curl -k -X POST https://localhost:8443/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sayani@example.com",
    "password": "password123",
    "deviceId": "device-B"
  }'
```
**Response:**
Returns `OTP_REQUIRED` and dispatches a security alert with OTP to the user's registered email!

---

### Step 6: Access Protected Endpoint
```bash
curl -k -X GET https://localhost:8443/api/v1/demo/hello \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```
**Response (`200 OK`):**
```json
{
  "status": "SUCCESS",
  "message": "Hello! You have accessed a secure endpoint protected by SSL/TLS and JWT!"
}
```
