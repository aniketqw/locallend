# LocalLend Frontend - Docker Setup Guide

## 🐳 Docker Containerization

This guide explains how to run the LocalLend frontend application using Docker.

## 📋 Prerequisites

- **Docker Desktop** installed and running
- **Docker Compose** (included with Docker Desktop)
- **Backend API** running (either locally or in Docker)

## 🚀 Quick Start with Docker

### Option 1: Using Docker Compose (Recommended)

```bash
# Build and start the container
npm run docker:compose:build

# Or use docker-compose directly
docker-compose up --build -d
```

The frontend will be available at: `http://localhost:3000`

### Option 2: Using Docker directly

```bash
# Build the Docker image
npm run docker:build

# Run the container
npm run docker:run

# Or use docker commands directly
docker build -t locallend-frontend .
docker run -p 3000:80 locallend-frontend
```

## 🔧 Docker Configuration Files

### Dockerfile (Multi-stage build)
- **Stage 1 (Builder):** Installs dependencies and builds the React app
- **Stage 2 (Production):** Serves the built app using Nginx

### docker-compose.yml
- Orchestrates the frontend container
- Includes optional backend and MongoDB services (commented out)
- Sets up networking between services

### nginx.conf
- Handles client-side routing for React Router
- Serves static assets with proper caching
- Includes security headers and gzip compression

## 🌐 Environment Configuration

### Production Environment (.env.production)
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_IMAGE_BASE_URL=http://localhost:8080/uploads
VITE_APP_TITLE=LocalLend
```

### Docker Compose Environment
If using Docker Compose with backend services, update the API URL:
```env
VITE_API_BASE_URL=http://locallend-backend:8080
```

## 📦 Container Details

- **Base Image:** node:18-alpine (for building), nginx:alpine (for serving)
- **Port:** 80 inside container, mapped to 3000 on host
- **Size:** Optimized multi-stage build (~50MB final image)
- **Restart Policy:** unless-stopped

## 🛠 Docker Commands

```bash
# Build and run with compose
docker-compose up --build -d

# View logs
docker-compose logs -f locallend-frontend

# Stop containers
docker-compose down

# Rebuild only frontend
docker-compose build locallend-frontend

# Access container shell (for debugging)
docker exec -it locallend-frontend_locallend-frontend_1 sh

# View Nginx config inside container
docker exec locallend-frontend_locallend-frontend_1 cat /etc/nginx/conf.d/default.conf
```

## 🔗 Backend Integration

### Option 1: Backend running locally
```env
# In .env.production
VITE_API_BASE_URL=http://localhost:8080
```

### Option 2: Backend in Docker (same network)
```env
# In .env.production  
VITE_API_BASE_URL=http://locallend-backend:8080
```

### Option 3: Nginx proxy (recommended for production)
Uncomment the API proxy section in `nginx.conf`:
```nginx
location /api/ {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

## 🏗 Full Stack Docker Setup

To run the entire LocalLend application with Docker:

1. **Uncomment backend services** in `docker-compose.yml`
2. **Build/obtain backend Docker image**
3. **Update environment variables** for service communication
4. **Run the full stack:**
   ```bash
   docker-compose up --build -d
   ```

Services will be available at:
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- MongoDB: `localhost:27017`

## 🔍 Troubleshooting

### Build Issues
```bash
# Clear Docker cache
docker system prune -a

# Build without cache
docker-compose build --no-cache

# Check build logs
docker-compose logs locallend-frontend
```

### Network Issues
```bash
# Check container networks
docker network ls

# Inspect network
docker network inspect locallend-frontend_locallend-network
```

### Container Access
```bash
# Check running containers
docker ps

# Access container logs
docker logs <container-id>

# Shell into container
docker exec -it <container-id> sh
```

## 📊 Performance Optimization

The Docker setup includes:
- **Multi-stage builds** to minimize image size
- **Nginx gzip compression** for faster loading
- **Static asset caching** with proper headers
- **Production build optimization** via Vite

## 🔒 Security Features

- Non-root user in container
- Security headers (X-Frame-Options, X-Content-Type-Options, etc.)
- Minimal attack surface with Alpine Linux
- No sensitive data in container layers

---

## 🚀 Production Deployment

For production deployment:

1. **Update environment variables** for production URLs
2. **Configure HTTPS** (add SSL certificates to nginx.conf)
3. **Set up proper logging** and monitoring
4. **Use Docker secrets** for sensitive data
5. **Implement health checks** in docker-compose.yml

Example production docker-compose:
```yaml
services:
  locallend-frontend:
    image: locallend-frontend:latest
    restart: always
    environment:
      - VITE_API_BASE_URL=https://api.yourdomain.com
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:80/"]
      interval: 30s
      timeout: 10s
      retries: 3
```