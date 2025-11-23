# LocalLend 2.0 - Docker Deployment Guide

## =Ë Table of Contents
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Docker Build Options](#docker-build-options)
- [Docker Run Options](#docker-run-options)
- [Docker Compose](#docker-compose)
- [Environment Variables](#environment-variables)
- [Troubleshooting](#troubleshooting)
- [Production Deployment](#production-deployment)

---

## =' Prerequisites

### Required Software
- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher (optional, for multi-container setup)

### Check Installation
```bash
docker --version
docker-compose --version
```

### Install Docker
- **macOS**: Install [Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/)
- **Linux**: Follow [Docker Engine installation guide](https://docs.docker.com/engine/install/)
- **Windows**: Install [Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/)

---

## ¡ Quick Start

### Option 1: Using Automated Scripts

#### Build and Run (Easiest Method)
```bash
cd backend

# Build the Docker image
./docker-build.sh

# Run the container
./docker-run.sh
```

The application will be available at `http://localhost:8080`

### Option 2: Using Docker Compose (Full Stack)

```bash
# From project root directory
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

This starts:
- MongoDB database
- LocalLend 2.0 backend (Spring Boot)
- Frontend (Nginx)

---

## <× Docker Build Options

### Basic Build

```bash
cd backend
docker build -t locallend2.0:latest .
```

### Build with Version Tag

```bash
docker build -t locallend2.0:2.0.0 -t locallend2.0:latest .
```

### Build with No Cache (Clean Build)

```bash
docker build --no-cache -t locallend2.0:latest .
```

### View Build Progress

```bash
docker build --progress=plain -t locallend2.0:latest .
```

### Check Image Size

```bash
docker images locallend2.0
```

Expected size: ~300-400 MB (multi-stage build with Alpine Linux)

---

## =€ Docker Run Options

### Basic Run

```bash
docker run -d \
  --name locallend2.0 \
  -p 8080:8080 \
  locallend2.0:latest
```

### Run with Environment Variables

```bash
docker run -d \
  --name locallend2.0 \
  -p 8080:8080 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/locallend \
  -e JWT_SECRET=your-secure-secret-here \
  -e JWT_EXPIRATION_MS=86400000 \
  locallend2.0:latest
```

### Run with .env File

```bash
docker run -d \
  --name locallend2.0 \
  -p 8080:8080 \
  --env-file .env \
  locallend2.0:latest
```

### Run with Custom Memory Limits

```bash
docker run -d \
  --name locallend2.0 \
  -p 8080:8080 \
  -m 1g \
  --memory-swap 1g \
  -e JAVA_OPTS="-Xms512m -Xmx1024m" \
  locallend2.0:latest
```

### Run with Volume Mount (for logs)

```bash
docker run -d \
  --name locallend2.0 \
  -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  locallend2.0:latest
```

---

## =3 Docker Compose

### File Structure

The project includes two Docker Compose files:

- `docker-compose.yml` - Production configuration
- `docker-compose.dev.yml` - Development configuration (if exists)

### Start All Services

```bash
# Start in detached mode
docker-compose up -d

# Start with build (rebuild images)
docker-compose up -d --build

# Start specific service
docker-compose up -d backend
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f mongodb

# Last 100 lines
docker-compose logs --tail=100 backend
```

### Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose down -v

# Stop specific service
docker-compose stop backend
```

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart backend
```

### Check Status

```bash
# List running containers
docker-compose ps

# View resource usage
docker stats
```

---

## = Environment Variables

### Required Variables

```bash
# MongoDB Configuration
MONGODB_URI=mongodb://mongodb:27017/locallend

# JWT Configuration (CRITICAL: Change in production!)
JWT_SECRET=your-super-secret-jwt-key-minimum-256-bits
JWT_EXPIRATION_MS=86400000
```

### Optional Variables

```bash
# Server Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Feature Flags (LocalLend 2.0)
USE_COMMAND_PATTERN=true
USE_STATE_PATTERN=true
USE_EVENT_DRIVEN=true
ENABLE_ALL_PATTERNS=true

# JVM Options
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC

# Cloudinary (for image uploads)
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### Create .env File

```bash
cd backend
cat > .env << EOF
MONGODB_URI=mongodb://mongodb:27017/locallend
JWT_SECRET=$(openssl rand -base64 32)
JWT_EXPIRATION_MS=86400000
USE_COMMAND_PATTERN=true
USE_STATE_PATTERN=true
USE_EVENT_DRIVEN=true
EOF
```

---

## =à Common Commands

### Container Management

```bash
# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# Stop container
docker stop locallend2.0

# Start stopped container
docker start locallend2.0

# Restart container
docker restart locallend2.0

# Remove container
docker rm locallend2.0

# Remove container forcefully
docker rm -f locallend2.0
```

### Logs and Debugging

```bash
# View logs
docker logs locallend2.0

# Follow logs in real-time
docker logs -f locallend2.0

# View last 100 lines
docker logs --tail=100 locallend2.0

# Execute command inside container
docker exec -it locallend2.0 bash

# Check container health
docker inspect --format='{{.State.Health.Status}}' locallend2.0
```

### Image Management

```bash
# List images
docker images

# Remove image
docker rmi locallend2.0:latest

# Remove all unused images
docker image prune -a

# View image details
docker inspect locallend2.0:latest
```

---

## = Health Checks

### Check Application Health

```bash
# Using curl
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}

# Check from inside container
docker exec locallend2.0 curl -f http://localhost:8080/actuator/health
```

### Check Container Health

```bash
docker inspect --format='{{json .State.Health}}' locallend2.0 | jq
```

---

## = Troubleshooting

### Issue 1: Container Won't Start

**Symptoms:**
```
Error: Container exits immediately after starting
```

**Solution:**
```bash
# Check logs for errors
docker logs locallend2.0

# Common issues:
# - MongoDB not accessible
# - Invalid JWT_SECRET
# - Port 8080 already in use

# Fix port conflict
lsof -ti:8080 | xargs kill -9

# Or use different port
docker run -d --name locallend2.0 -p 8081:8080 locallend2.0:latest
```

### Issue 2: Cannot Connect to MongoDB

**Symptoms:**
```
MongoSocketOpenException: Exception opening socket
```

**Solution:**
```bash
# If running MongoDB on host machine:
docker run -d \
  --name locallend2.0 \
  -p 8080:8080 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/locallend \
  locallend2.0:latest

# If using docker-compose (MongoDB container):
# Use service name: mongodb://mongodb:27017/locallend
```

### Issue 3: Out of Memory

**Symptoms:**
```
Java heap space error
Container killed
```

**Solution:**
```bash
# Increase container memory
docker run -d \
  --name locallend2.0 \
  -p 8080:8080 \
  -m 2g \
  -e JAVA_OPTS="-Xms512m -Xmx1536m" \
  locallend2.0:latest
```

### Issue 4: Image Build Fails

**Symptoms:**
```
Build fails during Maven package
```

**Solution:**
```bash
# Clean build with no cache
docker build --no-cache -t locallend2.0:latest .

# If Maven dependency issues:
# Remove .m2 cache and rebuild
rm -rf ~/.m2/repository
docker build --no-cache -t locallend2.0:latest .
```

### Issue 5: Health Check Failing

**Symptoms:**
```
Health check returns unhealthy status
```

**Solution:**
```bash
# Check application logs
docker logs -f locallend2.0

# Access container shell
docker exec -it locallend2.0 bash

# Test health endpoint from inside
curl http://localhost:8080/actuator/health

# Common fixes:
# - Wait longer (application may still be starting)
# - Check if Spring Boot started successfully
# - Verify MongoDB connection
```

---

## =€ Production Deployment

### Best Practices

1. **Use Specific Version Tags**
```bash
docker build -t locallend2.0:2.0.0 .
docker run -d --name locallend2.0 locallend2.0:2.0.0
```

2. **Set Strong JWT Secret**
```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

3. **Use Docker Secrets (Swarm/Kubernetes)**
```bash
echo "super-secret-jwt-key" | docker secret create jwt_secret -
```

4. **Enable Resource Limits**
```bash
docker run -d \
  --name locallend2.0 \
  -m 1g \
  --cpus=1.5 \
  locallend2.0:latest
```

5. **Use Health Checks**
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

6. **Set Up Monitoring**
```bash
# Using Prometheus + Grafana
# Expose actuator metrics
# Configure alerts
```

7. **Use Non-Root User**
```dockerfile
USER locallend  # Already configured in Dockerfile
```

8. **Enable Logging**
```bash
docker run -d \
  --name locallend2.0 \
  --log-driver json-file \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  locallend2.0:latest
```

### Deployment Checklist

- [ ] Change JWT_SECRET to strong random value
- [ ] Configure production MongoDB connection
- [ ] Set appropriate memory limits
- [ ] Enable health checks
- [ ] Configure logging and monitoring
- [ ] Set up automated backups
- [ ] Configure SSL/TLS certificates
- [ ] Set up reverse proxy (Nginx/Traefik)
- [ ] Enable firewall rules
- [ ] Test disaster recovery procedures

---

## =Ê Monitoring

### View Container Stats

```bash
# Real-time resource usage
docker stats locallend2.0

# One-time stats
docker stats --no-stream locallend2.0
```

### Check Application Metrics

```bash
# Spring Boot Actuator endpoints
curl http://localhost:8080/actuator
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

---

## = Updating the Application

### Rolling Update

```bash
# Build new version
docker build -t locallend2.0:2.0.1 .

# Stop old container
docker stop locallend2.0

# Remove old container
docker rm locallend2.0

# Start new container
docker run -d --name locallend2.0 locallend2.0:2.0.1
```

### Zero-Downtime Update (Docker Compose)

```bash
# Update code
# Build new image
docker-compose build backend

# Scale up with new version
docker-compose up -d --scale backend=2

# Verify new container is healthy
docker ps

# Scale down old version
docker-compose up -d --scale backend=1
```

---

## =Ú Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [LocalLend Backend Documentation](./RUN_INSTRUCTIONS.md)
- [Design Patterns Refactoring](./DESIGN_PATTERNS_REFACTORING.md)

---

## <˜ Getting Help

If you encounter issues:

1. Check this troubleshooting guide
2. Review Docker logs: `docker logs locallend2.0`
3. Check container status: `docker ps -a`
4. Verify environment variables: `docker inspect locallend2.0`
5. Test health endpoint: `curl http://localhost:8080/actuator/health`

---

**LocalLend 2.0** - Refactored with Design Patterns =€
