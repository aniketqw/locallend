# LocalLend

A peer-to-peer lending platform for sharing items within local communities.

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose installed
- Ports 80, 8080, and 27017 available

### Production Mode (Recommended)
```bash
# Start all services (MongoDB, Backend, Frontend)
docker compose up --build -d

# Access the application
# Frontend: http://localhost
# Backend API: http://localhost:8080
```

### Development Mode
```bash
# Start all services with hot reload
docker compose -f docker-compose.dev.yml up --build -d

# Access the application
# Frontend: http://localhost:5173 (Vite dev server)
# Backend API: http://localhost:8080
# MongoDB: localhost:27017
```

### Stop Services
```bash
# Production
docker compose down

# Development
docker compose -f docker-compose.dev.yml down

# Remove volumes (clean database)
docker compose down -v
```

## 📁 Project Structure

```
locallend/
├── backend/                          # Spring Boot Backend (Java 17)
│   ├── src/                         # Java source code
│   │   ├── main/java/               # Application code
│   │   └── main/resources/          # Config files (application.properties)
│   ├── pom.xml                      # Maven dependencies
│   ├── Dockerfile                   # Backend Docker configuration
│   └── .env                         # Backend environment variables
├── frontend/                         # React Frontend (TypeScript + Vite)
│   ├── src/                         # React source code
│   ├── public/                      # Static assets
│   ├── package.json                 # NPM dependencies
│   ├── Dockerfile                   # Production build
│   ├── Dockerfile.dev               # Development with hot reload
│   └── .env                         # Frontend environment variables
├── docker-compose.yml               # Production orchestration
├── docker-compose.dev.yml           # Development orchestration
└── README.md                        # This file
```

## 🔧 Technology Stack

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.6
- **Database**: MongoDB 7
- **Authentication**: JWT (JSON Web Tokens)
- **Build Tool**: Maven
- **Image Storage**: Cloudinary

### Frontend
- **Language**: TypeScript
- **Framework**: React 19.1.1
- **Build Tool**: Vite
- **UI Library**: Material-UI (MUI)
- **Routing**: React Router
- **HTTP Client**: Fetch API
- **Validation**: React Hook Form + Yup

## 🧪 Test Credentials

Use these credentials to test the application:

```
Email: u12
Password: 12345678

Email: usr2@usr2.com
Password: user21234

Email: fasd@gmail.com
Password: 12345678

Email:afadsaaftury@gmail.com
Password:afadsaaftury@gmail.com 
```

## 🛠️ Manual Development Setup

### Backend (without Docker)
```bash
cd backend
./mvnw spring-boot:run
# API available at http://localhost:8080
```

### Frontend (without Docker)
```bash
cd frontend
npm install
npm run dev
# UI available at http://localhost:5173
```

### MongoDB (without Docker)
```bash
# Install MongoDB 7 locally
# Ensure it's running on localhost:27017
```

## 📝 Environment Variables

### Backend (.env)
```env
MONGODB_URI=mongodb://mongodb:27017/locallend
JWT_SECRET=your-secure-jwt-secret-here
JWT_EXPIRATION_MS=86400000
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
SERVER_PORT=8080
APP_NAME=locallend
```

### Frontend (.env)
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=LocalLend
VITE_NODE_ENV=development
```

## 📊 Useful Docker Commands

```bash
# View logs
docker compose logs -f                    # All services
docker compose logs -f backend            # Backend only
docker compose logs -f frontend           # Frontend only

# Restart a service
docker compose restart backend
docker compose restart frontend

# Rebuild a specific service
docker compose up --build backend -d

# View running containers
docker ps

# Access container shell
docker exec -it locallend-backend /bin/sh
docker exec -it locallend-frontend /bin/sh

# Access MongoDB shell
docker exec -it locallend-mongodb mongosh

# Clean everything (caution: removes data!)
docker compose down -v
docker system prune -a
```

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -i :8080    # Backend
lsof -i :80      # Frontend production
lsof -i :5173    # Frontend development

# Kill process
kill -9 <PID>
```

### MongoDB Connection Issues
```bash
# Check if MongoDB container is running
docker ps | grep mongodb

# Check MongoDB logs
docker compose logs mongodb

# Restart MongoDB
docker compose restart mongodb
```

### Backend Not Starting
```bash
# Check backend logs
docker compose logs backend

# Verify .env file exists in backend/
ls -la backend/.env

# Rebuild backend
docker compose up --build backend -d
```

### Frontend Not Loading
```bash
# Check frontend logs
docker compose logs frontend

# Verify frontend built successfully
docker compose up --build frontend -d

# Check nginx config (production)
docker exec -it locallend-frontend cat /etc/nginx/conf.d/default.conf
```

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Test thoroughly with Docker
4. Submit a pull request

## 📄 License

[Your License Here]

## 📧 Contact

[Your Contact Information]
