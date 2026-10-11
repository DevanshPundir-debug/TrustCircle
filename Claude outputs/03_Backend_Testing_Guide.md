# Trust Circle - Backend Testing & Implementation Guide

## Phase 1: Database & Backend Setup

---

## STEP 1: Database Initialization

### 1.1 Install MySQL (if not already installed)

```bash
# On Mac (using Homebrew)
brew install mysql

# On Windows
# Download from: https://dev.mysql.com/downloads/mysql/

# On Linux (Ubuntu/Debian)
sudo apt-get install mysql-server
```

### 1.2 Start MySQL Service

```bash
# Mac
brew services start mysql

# Windows
# MySQL runs as a service, start from Services manager

# Linux
sudo service mysql start
```

### 1.3 Verify MySQL Connection

```bash
mysql -u root -p
# Password: (leave blank initially or use your password)

# If you get a connection prompt, MySQL is running
EXIT;
```

### 1.4 Run Database Setup Script

```bash
# Navigate to your project directory
cd /path/to/Project

# Execute the SQL script
mysql -u root < ../01_setup_database.sql

# Or with password
mysql -u root -p[password] < ../01_setup_database.sql
```

### 1.5 Verify Database Creation

```bash
mysql -u root -p

USE trust_circle;

# Check tables
SHOW TABLES;

# Check sample data
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM activities;
SELECT COUNT(*) FROM peer_reviews;

EXIT;
```

**Expected Output:**
```
+---------------------------+
| Tables_in_trust_circle    |
+---------------------------+
| activities                |
| activity_participants     |
| peer_reviews              |
| safety_alerts             |
| users                     |
+---------------------------+

Total users: 6
Total activities: 6
Total peer_reviews: 4
```

---

## STEP 2: Update Application Configuration

### 2.1 Update Database Connection

Edit: `src/main/resources/application.properties`

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/trust_circle?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=[YOUR_PASSWORD]  # Leave blank if no password, or add your password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=validate  # Changed from 'update' to 'validate' after first run
spring.jpa.show-sql=true  # Set to true for debugging
spring.jpa.open-in-view=false

# Logging
logging.level.root=INFO
logging.level.TrustCircle.Project=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**Note:** 
- Use `ddl-auto=update` on first run (to auto-create tables)
- Use `ddl-auto=validate` on subsequent runs (for safety)

---

## STEP 3: Build the Project

### 3.1 Clean & Build

```bash
cd /path/to/Project

# Clean previous builds
mvn clean

# Build with Maven
mvn compile

# Package application
mvn package -DskipTests

# Expected: BUILD SUCCESS
```

### 3.2 Verify Build

```bash
# Check if JAR was created
ls -lah target/Project-0.0.1-SNAPSHOT.jar

# Should see a file like: Project-0.0.1-SNAPSHOT.jar (about 20-30 MB)
```

---

## STEP 4: Run the Backend Server

### 4.1 Start Spring Boot Application

```bash
# Option 1: Using Maven
cd /path/to/Project
mvn spring-boot:run

# Option 2: Using Java directly
java -jar target/Project-0.0.1-SNAPSHOT.jar

# Option 3: Run from IDE
# Right-click ProjectApplication.java → Run As → Java Application
```

### 4.2 Verify Server is Running

```bash
# In a new terminal
curl http://localhost:8085

# Should return HTML (the index page)

# Or test auth endpoint
curl -X GET http://localhost:8085/api/auth/me

# Should return JSON error (no token provided) - that's OK
```

**Expected Console Output:**
```
2026-09-22 15:30:45.123  INFO ... TrustCircleApplication : Starting TrustCircleApplication
2026-09-22 15:30:47.456  INFO ... HikariPool : HikariPool-1 - Starting...
2026-09-22 15:30:48.789  INFO ... SessionFactoryImpl : HHH000412: Hibernate SessionFactory created
2026-09-22 15:30:49.012  INFO ... TomcatWebServer : Tomcat started on port(s): 8085
2026-09-22 15:30:49.234  INFO ... TrustCircleApplication : Started TrustCircleApplication in X seconds
```

---

## STEP 5: Test API Endpoints

### 5.1 Manual Testing with Postman/curl

#### Test Authentication Endpoints

```bash
# 1. Login (get JWT token)
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "abhijeet@trustcircle.com",
    "password": "hashed_password_1_bcrypt_here"
  }'

# Expected Response:
# {
#   "id": 1,
#   "email": "abhijeet@trustcircle.com",
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "role": "ROLE_ADMIN"
# }

# Save the token for subsequent requests
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

#### Test Activity Endpoints

```bash
# 2. List all activities
curl -X GET http://localhost:8085/api/activities

# 3. Get activity by ID
curl -X GET http://localhost:8085/api/activities/1

# 4. Create new activity (requires authentication)
curl -X POST http://localhost:8085/api/activities \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Activity",
    "description": "Testing the API",
    "activityType": "Sports",
    "latitude": 28.6139,
    "longitude": 77.0249,
    "location": "Test Location",
    "scheduledDate": "2026-09-25",
    "scheduledTime": "18:00:00",
    "durationMinutes": 90,
    "minParticipants": 2,
    "maxParticipants": 10,
    "estimatedBudget": 500
  }'

# 5. Join an activity
curl -X POST http://localhost:8085/api/activities/1/join \
  -H "Authorization: Bearer $TOKEN"

# 6. Get activity participants
curl -X GET http://localhost:8085/api/activities/1/participants
```

#### Test Trust & Review Endpoints

```bash
# 7. Get user's trust scorecard
curl -X GET http://localhost:8085/api/trust/scorecard \
  -H "Authorization: Bearer $TOKEN"

# 8. Submit peer review
curl -X POST http://localhost:8085/api/trust/review \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "activityId": 6,
    "reviewedUserId": 2,
    "rating": 5,
    "comment": "Great participant!",
    "punctuality": 5,
    "hygiene": 5,
    "communication": 5,
    "respectfulness": 5
  }'

# 9. Get user reviews
curl -X GET http://localhost:8085/api/trust/reviews/1
```

#### Test Safety Endpoints

```bash
# 10. Get safety alerts
curl -X GET http://localhost:8085/api/safety/alerts

# 11. Report suspicious user
curl -X POST http://localhost:8085/api/safety/report \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "suspectedUserId": 6,
    "reportReason": "Suspicious behavior",
    "severityLevel": "MEDIUM"
  }'

# 12. Resolve safety alert
curl -X POST http://localhost:8085/api/safety/alerts/1/resolve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "resolutionNotes": "False alarm, resolved"
  }'
```

#### Test Matching Endpoints

```bash
# 13. Get smart matched companions
curl -X GET http://localhost:8085/api/matching/companions \
  -H "Authorization: Bearer $TOKEN"
```

### 5.2 Automated Testing

Make the test script executable and run it:

```bash
# Make script executable
chmod +x 02_test_all_apis.sh

# Run automated tests
./02_test_all_apis.sh

# This will:
# ✅ Test all 23 endpoints
# ✅ Verify JSON responses
# ✅ Check for errors
# ✅ Generate test report (api_test_results.log)
```

---

## STEP 6: Database Verification Checklist

### 6.1 Verify Database Schema

```bash
mysql -u root -p
USE trust_circle;

# 1. Check all tables exist
SHOW TABLES;
# Should show: activities, activity_participants, peer_reviews, safety_alerts, users

# 2. Check users table
DESC users;
SELECT * FROM users LIMIT 5;

# 3. Check activities
SELECT COUNT(*) as total_activities FROM activities;
SELECT * FROM activities LIMIT 3;

# 4. Check relationships
SELECT a.id, a.title, u.full_name 
FROM activities a 
JOIN users u ON a.creator_id = u.id;

# 5. Check peer reviews
SELECT COUNT(*) as total_reviews FROM peer_reviews;

# 6. Check safety alerts
SELECT COUNT(*) as total_alerts FROM safety_alerts 
WHERE status = 'OPEN';

EXIT;
```

---

## STEP 7: Common Issues & Troubleshooting

### Issue 1: "Connection Refused"
**Problem:** `java.net.ConnectException: Connection refused`

**Solution:**
```bash
# Check if MySQL is running
mysql -u root -p

# If fails, start MySQL
# Mac: brew services start mysql
# Linux: sudo service mysql start

# Check database exists
mysql -u root -p
SHOW DATABASES;
```

### Issue 2: "Access Denied for user 'root'"
**Problem:** `Access denied for user 'root'@'localhost' (using password: NO)`

**Solution:**
- Update `application.properties` with correct password
- Make sure MySQL password matches in config

```properties
spring.datasource.password=your_actual_password
```

### Issue 3: "Unknown database 'trust_circle'"
**Problem:** Database doesn't exist

**Solution:**
```bash
# Recreate database
mysql -u root -p
CREATE DATABASE trust_circle;
EXIT;

# Then run the SQL script
mysql -u root -p trust_circle < 01_setup_database.sql
```

### Issue 4: "Illegal State Exception"
**Problem:** Hibernate DDL mode conflict

**Solution:**
```properties
# In application.properties
spring.jpa.hibernate.ddl-auto=update  # First run
spring.jpa.hibernate.ddl-auto=validate  # After first run
```

### Issue 5: Port 8085 Already in Use
**Problem:** `Port 8085 is already in use`

**Solution:**
```bash
# Find process using port 8085
lsof -i :8085  # Mac/Linux
netstat -ano | findstr :8085  # Windows

# Kill the process or use different port
# In application.properties:
server.port=8086  # Use different port
```

### Issue 6: Maven Build Fails
**Problem:** `BUILD FAILURE`

**Solution:**
```bash
# Clean and rebuild
mvn clean
mvn compile
mvn package -DskipTests

# If still fails:
# 1. Check Java version (should be 21)
java -version

# 2. Delete .m2 cache
rm -rf ~/.m2/repository
mvn clean install
```

---

## STEP 8: Performance Tuning

### 8.1 Enable Query Logging

Update `application.properties`:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
```

### 8.2 Add Connection Pool Settings

```properties
# Hikari Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
```

### 8.3 Database Indexing

The SQL script already includes indexes on:
- `users.email` (unique)
- `users.trust_score`
- `users.is_flagged`
- `activities.creator_id`
- `activity_participants.user_id`
- `peer_reviews.user_id`
- `safety_alerts.status`

---

## STEP 9: Security Considerations

### 9.1 Change Default Credentials

```bash
# MySQL - change root password
mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED BY 'strong_password';
FLUSH PRIVILEGES;
EXIT;

# Create application user (not root)
mysql -u root -p
CREATE USER 'trustcircle'@'localhost' IDENTIFIED BY 'app_password';
GRANT ALL PRIVILEGES ON trust_circle.* TO 'trustcircle'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

Then update `application.properties`:
```properties
spring.datasource.username=trustcircle
spring.datasource.password=app_password
```

### 9.2 Update JWT Secret

Edit `application.properties`:
```properties
# Use a strong, unique key (min 256 bits)
app.jwt.secret=YourLongSecureKeyThatIsAtLeast32CharactersLongAndUnique1234567890!@#$%^&*
app.jwt.expiration=86400000  # 24 hours in milliseconds
```

### 9.3 Disable SQL Debugging in Production

```properties
# Production
spring.jpa.show-sql=false
logging.level.root=WARN
logging.level.TrustCircle.Project=INFO
```

---

## STEP 10: API Endpoint Reference

| # | Method | Endpoint | Authentication | Purpose |
|---|--------|----------|-----------------|---------|
| 1 | POST | `/api/auth/register` | ❌ | Register new user |
| 2 | POST | `/api/auth/login` | ❌ | Login & get JWT token |
| 3 | GET | `/api/auth/me` | ✅ | Get current user profile |
| 4 | POST | `/api/auth/verify-form` | ✅ | Submit verification form |
| 5 | GET | `/api/activities` | ❌ | List all activities |
| 6 | GET | `/api/activities/{id}` | ❌ | Get activity details |
| 7 | POST | `/api/activities` | ✅ | Create activity |
| 8 | POST | `/api/activities/{id}/join` | ✅ | Join activity |
| 9 | GET | `/api/activities/{id}/participants` | ❌ | Get participants |
| 10 | POST | `/api/activities/{id}/complete` | ✅ | Complete activity |
| 11 | GET | `/api/matching/companions` | ✅ | Get smart matches |
| 12 | GET | `/api/trust/scorecard` | ✅ | Get trust score |
| 13 | POST | `/api/trust/review` | ✅ | Submit peer review |
| 14 | GET | `/api/trust/reviews/{userId}` | ❌ | Get user reviews |
| 15 | GET | `/api/safety/alerts` | ❌ | List safety alerts |
| 16 | POST | `/api/safety/report` | ✅ | Report suspicious user |
| 17 | POST | `/api/safety/alerts/{id}/resolve` | ✅ | Resolve alert |

---

## STEP 11: Quick Start Checklist

```
[ ] MySQL installed and running
[ ] Database created (trust_circle)
[ ] SQL seed data loaded
[ ] application.properties updated with DB credentials
[ ] Project built successfully (mvn clean package)
[ ] Spring Boot server running (mvn spring-boot:run)
[ ] Can access http://localhost:8085
[ ] Can login and get JWT token
[ ] All 17 API endpoints tested
[ ] Test script passed (./02_test_all_apis.sh)
[ ] No ERROR logs in console
[ ] Database connection confirmed
[ ] Ready for Phase 2 (Frontend Integration)
```

---

## Next: Phase 2 - Frontend Integration

Once backend is verified:
1. Ensure frontend is loading at http://localhost:8085
2. Check browser console for errors
3. Test form submissions against live API
4. Verify CORS headers are correct
5. Update API endpoints in frontend JS files

See document: `04_Frontend_Integration_Guide.md`
