# Secure Auth REST API (Spring Boot & SSL/TLS & JWT)

A production-grade, secure authentication and authorization REST API built with **Spring Boot**, **Spring Security**, **JSON Web Tokens (JWT)**, and **HTTPS / SSL (TLS)** using an RSA PKCS#12 Keystore.

---

## 🚀 Key Features

- **HTTPS / SSL (TLS) Encryption**: Server runs exclusively over HTTPS on port `8443` using a local PKCS#12 RSA keystore (`keystore.p12`).
- **Stateless JWT Authentication**: Secure token generation, validation, and claim extraction using JJWT (`0.12.6`).
- **Spring Security Architecture**:
  - `SecurityFilterChain` configuring stateless session policy.
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
│   ├── AuthController.java      # Registration and Login endpoints
│   └── DemoController.java      # Protected test endpoints (requires JWT)
├── dto/
│   ├── AuthResponse.java        # Standardized JWT response payload
│   ├── LoginRequest.java        # Login DTO with validation
│   └── RegisterRequest.java     # User registration DTO
├── entity/
│   ├── Role.java                # User role enum (ROLE_USER, ROLE_ADMIN)
│   └── User.java                # User JPA Entity (implements UserDetails)
├── exception/
│   └── GlobalExceptionHandler.java # Centralized error handling
├── repository/
│   └── UserRepository.java      # Spring Data JPA Repository
├── security/
│   ├── ApplicationConfig.java   # PasswordEncoder, UserDetailsService, AuthManager
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter for Bearer token validation
│   ├── JwtService.java          # JJWT token generation and validation service
│   └── SecurityConfig.java      # Spring Security Filter Chain configuration
└── SecureAuthApiApplication.java # Spring Boot main entry point
```

---

## ⚙️ Configuration (`src/main/resources/application.properties`)

```properties
# SSL / HTTPS Configuration
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=password123
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=authkeystore

# Database Configuration (H2 In-Memory)
spring.datasource.url=jdbc:h2:mem:authdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration-ms=86400000
```

---

## 🧪 Testing the API

> **Note on Self-Signed SSL Certificate:**
> Because the keystore uses a self-signed certificate for `localhost`, add `-k` or `--insecure` with `curl`, or in **Postman Settings -> General -> Turn OFF "SSL certificate verification"**.

### 1. Register a New User
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
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "name": "Sayani",
  "email": "sayani@example.com",
  "role": "ROLE_USER",
  "message": "User registered successfully"
}
```

---

### 2. Login
```bash
curl -k -X POST https://localhost:8443/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sayani@example.com",
    "password": "password123"
  }'
```

---

### 3. Access Protected Endpoint (Without Token -> `403 Forbidden`)
```bash
curl -k -X GET https://localhost:8443/api/v1/demo/hello
```

---

### 4. Access Protected Endpoint (With Token -> `200 OK`)
```bash
curl -k -X GET https://localhost:8443/api/v1/demo/hello \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Response (`200 OK`):**
```json
{
  "status": "SUCCESS",
  "message": "Hello! You have accessed a secure endpoint protected by SSL/TLS and JWT!"
}
```

---

### 5. Access User Profile Endpoint
```bash
curl -k -X GET https://localhost:8443/api/v1/demo/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```
