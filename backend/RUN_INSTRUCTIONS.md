# LocalLend Backend - Running Instructions

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Detailed Setup](#detailed-setup)
- [Running Commands](#running-commands)
- [Testing the API](#testing-the-api)
- [New Architecture Patterns](#new-architecture-patterns)
- [Troubleshooting](#troubleshooting)

---

## 🔧 Prerequisites

### 1. Check Java Installation
```bash
java -version
# Required: Java 17 or higher
```

**If not installed:**
- macOS: `brew install openjdk@17`
- Linux: `sudo apt install openjdk-17-jdk`
- Windows: Download from [Oracle](https://www.oracle.com/java/technologies/downloads/)

### 2. Check Maven Installation
```bash
mvn -version
# Required: Maven 3.6 or higher
```

**If not installed:**
- macOS: `brew install maven`
- Linux: `sudo apt install maven`
- Windows: Download from [Apache Maven](https://maven.apache.org/download.cgi)

### 3. Check MongoDB Installation
```bash
mongod --version
# Required: MongoDB 4.4 or higher
```

**If not installed:**
- macOS: `brew install mongodb-community`
- Linux: `sudo apt install mongodb`
- Windows: Download from [MongoDB](https://www.mongodb.com/try/download/community)

---

## ⚡ Quick Start

### Option 1: Using the Automated Script
```bash
cd backend
chmod +x QUICK_START.sh
./QUICK_START.sh
```

### Option 2: Manual Setup
```bash
# 1. Navigate to backend directory
cd backend

# 2. Start MongoDB (in a separate terminal)
mongod

# 3. Build the project
mvn clean install -DskipTests

# 4. Run the application
mvn spring-boot:run
```

**Server will start at:** `http://localhost:8080`

---

## 🛠️ Detailed Setup

### Step 1: MongoDB Setup

#### Start MongoDB
```bash
# Option A: Using Homebrew (macOS)
brew services start mongodb-community

# Option B: Direct command
mongod --dbpath /usr/local/var/mongodb

# Option C: Custom data directory
mongod --dbpath ~/data/db
```

#### Verify MongoDB is Running
```bash
# Connect to MongoDB shell
mongosh

# Or check the process
ps aux | grep mongod
```

#### Create Database (Optional - auto-created on first run)
```bash
mongosh
use locallend
db.createCollection("users")
db.createCollection("items")
db.createCollection("bookings")
exit
```

### Step 2: Environment Configuration

Create a `.env` file in the `backend` directory:

```bash
# Create .env file
touch .env
```

Add the following variables:

```env
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/locallend

# JWT Configuration (IMPORTANT: Use a strong secret in production)
JWT_SECRET=your-very-long-secret-key-at-least-256-bits-make-it-secure-and-random
JWT_EXPIRATION_MS=86400000

# Cloudinary Configuration (Optional - for image uploads)
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Server Configuration
SERVER_PORT=8080

# Feature Flags (New Architecture)
USE_COMMAND_PATTERN=false
USE_STATE_PATTERN=false
USE_EVENT_DRIVEN=false
```

**OR** use export commands:
```bash
export MONGODB_URI="mongodb://localhost:27017/locallend"
export JWT_SECRET="your-very-long-secret-key-at-least-256-bits"
export JWT_EXPIRATION_MS=86400000
```

### Step 3: Build the Project

```bash
# Navigate to backend directory
cd backend

# Clean build (removes old artifacts)
mvn clean

# Compile and package
mvn package -DskipTests

# Or install to local Maven repository
mvn clean install -DskipTests
```

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 15.234 s
```

---

## 🚀 Running Commands

### Development Mode (Default)

```bash
# Standard run
mvn spring-boot:run

# With specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# With custom port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### With New Architecture Patterns Enabled

```bash
# Enable Command Pattern only
mvn spring-boot:run -Dspring-boot.run.arguments="--feature.use-command-pattern=true"

# Enable State Pattern only
mvn spring-boot:run -Dspring-boot.run.arguments="--feature.use-state-pattern=true"

# Enable all new patterns
mvn spring-boot:run -Dspring-boot.run.arguments="--feature.use-command-pattern=true --feature.use-state-pattern=true --feature.use-event-driven=true"

# Or use environment variable
export ENABLE_ALL_PATTERNS=true
mvn spring-boot:run
```

### Debug Mode

```bash
# Run with debug enabled (port 5005)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Production Mode

```bash
# Build JAR file
mvn clean package -DskipTests

# Run the JAR
java -jar target/locallend-0.0.1-SNAPSHOT.jar

# With environment variables
java -jar target/locallend-0.0.1-SNAPSHOT.jar \
  --spring.data.mongodb.uri=mongodb://localhost:27017/locallend \
  --feature.use-command-pattern=true
```

### Background Execution

```bash
# Run in background (macOS/Linux)
nohup mvn spring-boot:run > app.log 2>&1 &

# Check if running
ps aux | grep spring-boot

# Kill the process
kill -9 <PID>
```

---

## 🧪 Testing the API

### Health Check

```bash
# Check if server is running
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### Test Endpoints with cURL

#### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User",
    "phoneNumber": "1234567890"
  }'
```

#### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }'

# Save the token from response:
# {"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...","userId":"..."}
```

#### 3. Get User Profile
```bash
# Replace <TOKEN> with your JWT token
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer <TOKEN>"
```

#### 4. Create an Item
```bash
# Replace <USER_ID> and <TOKEN>
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <USER_ID>" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "name": "Power Drill",
    "description": "DeWalt 20V power drill",
    "condition": "EXCELLENT",
    "deposit": 50.00,
    "categoryId": "categoryId"
  }'
```

#### 5. Create a Booking (Command Pattern)
```bash
# With command pattern enabled
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <BORROWER_ID>" \
  -d '{
    "itemId": "<ITEM_ID>",
    "startDate": "2024-12-01T10:00:00",
    "endDate": "2024-12-05T10:00:00",
    "bookingNotes": "Need for home project"
  }'
```

### Using Postman

1. **Import Collection**: Import `LocalLend_Postman_Collection.json`
2. **Import Environment**: Import `postman_environment.json`
3. **Set Variables**: Update base_url if needed
4. **Run Requests**: Start with Authentication folder

---

## 🎨 New Architecture Patterns

The refactored backend supports new design patterns that can be toggled via feature flags.

### Feature Flags

```properties
# Enable Command Pattern
feature.use-command-pattern=true

# Enable State Pattern
feature.use-state-pattern=true

# Enable Event-Driven Architecture
feature.use-event-driven=true

# Enable all patterns at once
feature.enable-all-patterns=true
```

### Testing Command Pattern

```bash
# 1. Enable command pattern
export USE_COMMAND_PATTERN=true
mvn spring-boot:run

# 2. Create booking (will use new command pattern)
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <USER_ID>" \
  -d '{...}'

# 3. Check logs for command execution
# You should see: "Executing command: CreateBookingCommand | User: ..."
```

### Testing State Pattern

```bash
# 1. Enable state pattern
export USE_STATE_PATTERN=true
mvn spring-boot:run

# 2. The booking lifecycle will use state machine
# Valid transitions:
# PENDING → CONFIRMED → ACTIVE → COMPLETED
# PENDING → CANCELLED/REJECTED
# ACTIVE → OVERDUE

# 3. Try invalid transition (will fail)
curl -X PATCH http://localhost:8080/api/bookings/<ID>/activate \
  -H "X-User-Id: <USER_ID>"
# Error if booking is not CONFIRMED first
```

### Comparing Legacy vs New Patterns

```bash
# Terminal 1: Run with legacy code
mvn spring-boot:run

# Terminal 2: Run with new patterns
export ENABLE_ALL_PATTERNS=true
mvn spring-boot:run -Dserver.port=8081

# Compare performance and behavior
```

---

## 🐛 Troubleshooting

### Issue 1: MongoDB Connection Failed

**Error:**
```
com.mongodb.MongoSocketOpenException: Exception opening socket
```

**Solution:**
```bash
# Check if MongoDB is running
ps aux | grep mongod

# Start MongoDB
brew services start mongodb-community
# OR
mongod --dbpath /usr/local/var/mongodb

# Verify connection
mongosh
```

### Issue 2: Port Already in Use

**Error:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solution:**
```bash
# Option 1: Kill the process using port 8080
lsof -ti:8080 | xargs kill -9

# Option 2: Use a different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Issue 3: Build Failures

**Error:**
```
[ERROR] Failed to execute goal ... compilation failure
```

**Solution:**
```bash
# Clean and rebuild
mvn clean install -DskipTests

# If still failing, check Java version
java -version
# Should be 17 or higher

# Update JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Issue 4: JWT Token Invalid

**Error:**
```
401 Unauthorized - Invalid JWT token
```

**Solution:**
```bash
# Ensure JWT_SECRET is set and consistent
echo $JWT_SECRET

# If empty, set it:
export JWT_SECRET="your-very-long-secret-key-at-least-256-bits"

# Restart the application
mvn spring-boot:run
```

### Issue 5: Command Pattern Not Working

**Error:**
```
Command pattern is disabled
```

**Solution:**
```bash
# Check feature flag
cat backend/src/main/resources/application.properties | grep use-command-pattern

# Enable via environment variable
export USE_COMMAND_PATTERN=true
mvn spring-boot:run

# OR edit application.properties
feature.use-command-pattern=true
```

### Issue 6: Missing Dependencies

**Error:**
```
ClassNotFoundException: ...
```

**Solution:**
```bash
# Update dependencies
mvn dependency:resolve

# Force update
mvn clean install -U
```

---

## 📊 Monitoring & Logs

### View Application Logs

```bash
# Real-time logs
mvn spring-boot:run

# Logs to file
mvn spring-boot:run > app.log 2>&1

# Filter logs
tail -f app.log | grep "ERROR"
tail -f app.log | grep "BookingCreated"
```

### Check Active Endpoints

```bash
# List all endpoints
curl http://localhost:8080/actuator/mappings | jq

# Check specific endpoint
curl -I http://localhost:8080/api/bookings
```

### Database Inspection

```bash
# Connect to MongoDB
mongosh

# Switch to database
use locallend

# View collections
show collections

# Count documents
db.bookings.countDocuments()
db.users.countDocuments()

# View recent bookings
db.bookings.find().sort({createdDate: -1}).limit(5)

# Exit
exit
```

---

## 🎯 Next Steps

1. ✅ **Import Postman Collection** for easy API testing
2. ✅ **Create test users** via `/api/auth/register`
3. ✅ **Create items** for borrowing
4. ✅ **Test booking flow** (create → confirm → activate → complete)
5. ✅ **Enable new patterns** and compare behavior
6. ✅ **Review logs** to understand command execution

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Design Patterns Refactoring Guide](./DESIGN_PATTERNS_REFACTORING.md)
- [Postman Documentation](https://learning.postman.com/)

---

## 🆘 Getting Help

If you encounter issues:

1. Check this troubleshooting guide
2. Review application logs
3. Verify environment variables
4. Check MongoDB connection
5. Ensure all prerequisites are installed

---

**Happy Coding! 🚀**
