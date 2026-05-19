# CleanCity Backend - Complete Setup Guide

## Overview

This guide provides complete instructions for setting up the CleanCity Spring Boot backend with Supabase PostgreSQL database.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Supabase account (free tier works)
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Architecture Summary

### Tech Stack
- **Backend Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: Supabase PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **API Documentation**: SpringDoc OpenAPI (Swagger)

### Project Structure
```
backend/java/
├── src/main/
│   ├── java/com/cleancity/
│   │   ├── CleanCityApplication.java          # Main application class
│   │   ├── controller/                        # REST API controllers
│   │   │   ├── AuthController.java
│   │   │   └── PickupController.java
│   │   ├── dto/                              # Data Transfer Objects
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── PickupRequest.java
│   │   │   ├── PickupResponse.java
│   │   │   ├── ConfirmPickupRequest.java
│   │   │   └── ApiResponse.java
│   │   ├── entity/                           # JPA Entities
│   │   │   ├── User.java
│   │   │   ├── Resident.java
│   │   │   ├── Collector.java
│   │   │   ├── Pickup.java
│   │   │   ├── Report.java
│   │   │   ├── Zone.java
│   │   │   └── *Enum.java
│   │   ├── exception/                        # Custom exceptions
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── UnauthorizedException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── repository/                       # JPA Repositories
│   │   │   ├── UserRepository.java
│   │   │   ├── ResidentRepository.java
│   │   │   ├── CollectorRepository.java
│   │   │   ├── PickupRepository.java
│   │   │   ├── ReportRepository.java
│   │   │   └── ZoneRepository.java
│   │   ├── security/                         # Security configuration
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── CustomUserDetailsService.java
│   │   └── service/                          # Business logic
│   │       └── PickupService.java
│   └── resources/
│       ├── application.properties            # Application configuration
│       └── application-dev.properties         # Dev environment config
├── pom.xml                                   # Maven dependencies
└── .env.example                              # Environment variables template

backend/supabase/
├── schema.sql                                # Database schema
└── rls_policies.sql                          # Row Level Security policies
```

## Setup Instructions

### Step 1: Set Up Supabase Database

1. **Create Supabase Account**
   - Go to https://supabase.com
   - Sign up for a free account
   - Create a new project called "cleancity"

2. **Get Database Credentials**
   - Go to Project Settings > Database
   - Copy the following:
     - Connection string (JDBC format)
     - Database password
     - Database host
     - Database port (5432)

3. **Run SQL Schema**
   - Go to SQL Editor in Supabase
   - Copy and execute the contents of `backend/supabase/schema.sql`
   - This will create all tables, indexes, triggers, and functions

4. **Enable Row Level Security**
   - Copy and execute the contents of `backend/supabase/rls_policies.sql`
   - This will set up security policies for data access

### Step 2: Configure Environment Variables

1. **Create `.env` file**
   ```bash
   cd backend/java
   cp .env.example .env
   ```

2. **Edit `.env` file**
   ```env
   SUPABASE_JDBC_URL=jdbc://postgresql://your-project.supabase.co:5432/postgres
   SUPABASE_DB_USERNAME=postgres
   SUPABASE_DB_PASSWORD=your_supabase_password
   JWT_SECRET=YourSuperSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong
   JWT_EXPIRATION=86400000
   JWT_REFRESH_EXPIRATION=604800000
   CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
   SPRING_PROFILE=dev
   ```

3. **Generate Secure JWT Secret**
   ```bash
   # On Linux/Mac
   openssl rand -base64 32
   
   # On Windows (PowerShell)
   [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
   ```

### Step 3: Build and Run the Backend

#### Option A: Using Maven Command Line

1. **Navigate to project directory**
   ```bash
   cd backend/java
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

#### Option B: Using IDE (IntelliJ IDEA)

1. **Open the project**
   - File > Open > Select `backend/java` directory
   - Wait for Maven to import dependencies

2. **Configure Run Configuration**
   - Run > Edit Configurations
   - Add new Spring Boot configuration
   - Main class: `com.cleancity.CleanCityApplication`
   - Environment variables: Load from `.env` file

3. **Run the application**
   - Click the green run button
   - Or press Shift + F10

### Step 4: Verify Backend is Running

1. **Check Console Output**
   ```
   ==========================================
   🌍 CleanCity Backend Started Successfully!
   📊 API Documentation: http://localhost:8080/api/swagger-ui.html
   🏥 Health Check: http://localhost:8080/api/actuator/health
   ==========================================
   ```

2. **Test Health Endpoint**
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

3. **Access Swagger UI**
   - Open browser: http://localhost:8080/api/swagger-ui.html
   - You should see the API documentation

### Step 5: Test Authentication

1. **Register a Resident**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register/resident \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "fullName=John Doe&email=john@example.com&password=password123&phone=0712345678&area=CBD"
   ```

2. **Login**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"john@example.com","password":"password123"}'
   ```

3. **Copy the JWT token** from the response

### Step 6: Test Pickup Confirmation (Critical Fix)

1. **Create a Pickup**
   ```bash
   curl -X POST http://localhost:8080/api/pickups \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -d '{
       "wasteType": "PLASTIC",
       "location": "123 Main St",
       "scheduledDate": "2026-05-20",
       "scheduledTime": "09:00:00",
       "zone": "CBD"
     }'
   ```

2. **Confirm the Pickup** (as Collector or Admin)
   ```bash
   curl -X POST http://localhost:8080/api/pickups/confirm \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer COLLECTOR_JWT_TOKEN" \
     -d '{
       "pickupId": "PICKUP_UUID_FROM_STEP_1",
       "notes": "Pickup completed successfully"
     }'
   ```

3. **Verify in Database**
   - Go to Supabase SQL Editor
   - Run: `SELECT * FROM pickups WHERE id = 'PICKUP_UUID';`
   - Verify status is 'COMPLETED'
   - Verify completed_at is populated

## Troubleshooting

### Issue: Database Connection Failed

**Symptoms:**
```
Connection refused: connect
FATAL: password authentication failed
```

**Solutions:**
1. Verify Supabase credentials are correct
2. Check if Supabase project is active
3. Verify JDBC URL format: `jdbc:postgresql://host:port/database`
4. Check if your IP is whitelisted in Supabase

### Issue: JWT Token Expired

**Symptoms:**
```
401 Unauthorized
Expired JWT token
```

**Solutions:**
1. Login again to get a new token
2. Increase JWT expiration time in `.env`
3. Implement token refresh mechanism

### Issue: CORS Errors

**Symptoms:**
```
Access to fetch has been blocked by CORS policy
```

**Solutions:**
1. Add frontend URL to `CORS_ALLOWED_ORIGINS` in `.env`
2. Check SecurityConfig.java CORS configuration
3. Ensure frontend sends correct headers

### Issue: Pickup Confirmation Not Working

**Symptoms:**
```
Pickup status not updating in database
403 Forbidden
```

**Solutions:**
1. Verify user has COLLECTOR or ADMIN role
2. Check if pickup is assigned to the collector
3. Verify pickup status is not already COMPLETED
4. Check backend logs for error messages

## Production Deployment

### Environment Variables for Production

```env
SUPABASE_JDBC_URL=jdbc://postgresql://your-production-db.supabase.co:5432/postgres
SUPABASE_DB_USERNAME=postgres
SUPABASE_DB_PASSWORD=strong_production_password
JWT_SECRET=very_long_secure_random_string_at_least_256_bits
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
CORS_ALLOWED_ORIGINS=https://your-production-domain.com
SPRING_PROFILE=prod
SERVER_PORT=8080
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_COM_CLEANCITY=INFO
```

### Build for Production

```bash
mvn clean package -DskipTests
```

### Run JAR File

```bash
java -jar target/cleancity-backend-1.0.0.jar
```

### Docker Deployment (Optional)

Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/cleancity-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t cleancity-backend .
docker run -p 8080:8080 --env-file .env cleancity-backend
```

## Security Recommendations

1. **Use HTTPS in production**
2. **Rotate JWT secrets regularly**
3. **Implement rate limiting**
4. **Enable database connection encryption**
5. **Use environment variables for all secrets**
6. **Enable audit logging**
7. **Regular security updates**
8. **Implement API key authentication for admin operations**
9. **Use Supabase's built-in backup features**
10. **Monitor database performance and query times**

## Performance Optimization

1. **Database Indexing**
   - Already implemented in schema.sql
   - Monitor slow queries with Supabase

2. **Connection Pooling**
   - HikariCP configured in application.properties
   - Adjust pool size based on traffic

3. **Caching**
   - Consider Redis for frequently accessed data
   - Cache user sessions and pickup lists

4. **Query Optimization**
   - Use JPA fetch joins to avoid N+1 queries
   - Implement pagination for large datasets

## Monitoring

### Health Check
```
GET /api/actuator/health
```

### Metrics
```
GET /api/actuator/metrics
```

### Log Files
- Location: `logs/cleancity-backend.log`
- Configure log rotation in application.properties

## Support

For issues or questions:
1. Check backend logs: `logs/cleancity-backend.log`
2. Review Swagger documentation: `http://localhost:8080/api/swagger-ui.html`
3. Check Supabase logs in dashboard
4. Verify database schema is correctly applied

## Summary

The CleanCity backend is now:
- ✅ Built with Spring Boot 3.x and Java 17
- ✅ Integrated with Supabase PostgreSQL
- ✅ Secured with JWT authentication
- ✅ Implements RBAC (Role-Based Access Control)
- ✅ Has proper error handling and validation
- ✅ Includes API documentation with Swagger
- ✅ Pickup confirmation issue is FIXED
- ✅ Production-ready and scalable
- ✅ Follows clean architecture principles
- ✅ Implements Row Level Security (RLS)

The critical pickup confirmation issue has been resolved by:
1. Creating a proper API endpoint `POST /pickups/confirm`
2. Implementing business logic in PickupService
3. Adding proper validation and error handling
4. Updating frontend to call the API instead of localStorage
5. Adding authentication and authorization checks
6. Implementing database transactions for data integrity
