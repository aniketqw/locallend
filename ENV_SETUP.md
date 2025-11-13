# Environment Variables Setup Guide

This project uses environment variables to store sensitive configuration like API keys, database credentials, and secrets.

## 🔐 Why Environment Variables?

- **Security**: Keeps sensitive data out of version control
- **Flexibility**: Easy to change settings per environment (dev, staging, production)
- **Best Practice**: Industry standard for configuration management

## 🚀 Quick Setup

### 1. Copy the Template

```bash
cp .env.example .env
```

### 2. Edit the `.env` File

Open `.env` in a text editor and replace the placeholder values with your actual credentials:

```bash
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/locallend

# JWT Security Configuration
JWT_SECRET=your-generated-secret-key-here
JWT_EXPIRATION_MS=86400000

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_actual_cloud_name
CLOUDINARY_API_KEY=your_actual_api_key
CLOUDINARY_API_SECRET=your_actual_api_secret

# Server Configuration
SERVER_PORT=8080

# Application Name
APP_NAME=locallend
```

### 3. Generate a Secure JWT Secret

**Using OpenSSL (Recommended):**
```bash
openssl rand -base64 64
```

**Or use an online generator:**
- https://www.grc.com/passwords.htm
- https://randomkeygen.com/

Copy the generated string and paste it as the `JWT_SECRET` value.

### 4. Get Cloudinary Credentials

1. Go to https://cloudinary.com/ and sign up (free account)
2. Navigate to your Dashboard: https://cloudinary.com/console
3. Copy the following values:
   - **Cloud Name**: Found at the top of the dashboard
   - **API Key**: In the "Account Details" section
   - **API Secret**: Click "Reveal" next to API Secret

4. Paste these values into your `.env` file

### 5. Start the Application

```bash
mvn spring-boot:run
```

The application will automatically load variables from `.env` file!

## 📋 Available Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/locallend` | Yes |
| `JWT_SECRET` | Secret key for JWT token signing | (see template) | Yes |
| `JWT_EXPIRATION_MS` | JWT token expiration time in milliseconds | `86400000` (24 hours) | Yes |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | `your_cloud_name` | Yes* |
| `CLOUDINARY_API_KEY` | Cloudinary API key | `your_api_key` | Yes* |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | `your_api_secret` | Yes* |
| `SERVER_PORT` | Port for the backend server | `8080` | No |
| `APP_NAME` | Application name | `locallend` | No |

\* Required for image upload functionality

## 🌍 Environment-Specific Configuration

### Development

Use the `.env` file in the project root (already ignored by Git).

### Production

**Option 1: Set System Environment Variables**

**Linux/Mac:**
```bash
export MONGODB_URI="mongodb://your-production-db:27017/locallend"
export JWT_SECRET="your-secure-production-secret"
export CLOUDINARY_CLOUD_NAME="your_cloud"
# ... etc
```

**Windows:**
```powershell
$env:MONGODB_URI="mongodb://your-production-db:27017/locallend"
$env:JWT_SECRET="your-secure-production-secret"
$env:CLOUDINARY_CLOUD_NAME="your_cloud"
# ... etc
```

**Option 2: Docker/Container**

Pass environment variables when running the container:

```bash
docker run -e MONGODB_URI="..." -e JWT_SECRET="..." -e CLOUDINARY_CLOUD_NAME="..." locallend
```

Or use a `.env` file with Docker Compose:

```yaml
# docker-compose.yml
services:
  backend:
    image: locallend
    env_file:
      - .env
```

**Option 3: Cloud Platform**

Most cloud platforms (Heroku, AWS, Azure, etc.) provide ways to set environment variables through their dashboard or CLI.

## 🔒 Security Best Practices

### ✅ DO:
- Keep `.env` in `.gitignore` (already configured)
- Use different values for dev and production
- Generate strong random secrets
- Use HTTPS in production
- Rotate secrets regularly
- Limit access to production environment variables

### ❌ DON'T:
- Commit `.env` to version control
- Share your `.env` file
- Use weak or default secrets in production
- Hardcode secrets in the application code
- Share secrets via unsecured channels (email, Slack, etc.)

## 🧪 Verify Configuration

Check if all environment variables are loaded correctly:

```bash
# Test MongoDB connection
curl http://localhost:8080/api/categories

# Test Cloudinary configuration
curl http://localhost:8080/api/images/health

# Expected response:
# {"configured": true, "message": "Cloudinary is properly configured"}
```

## 🐛 Troubleshooting

### Problem: Application can't find `.env` file

**Solution**: Ensure `.env` is in the project root directory (same level as `pom.xml`)

### Problem: Environment variables not loading

**Solution**: 
1. Verify `.env` file exists and has correct syntax
2. Restart the application
3. Check for syntax errors (no spaces around `=`)

### Problem: "Cloudinary not configured" error

**Solution**:
1. Verify Cloudinary credentials in `.env`
2. Ensure no extra quotes or spaces
3. Restart the application

### Problem: JWT authentication failing

**Solution**:
1. Generate a new `JWT_SECRET` (at least 256 bits)
2. Update `.env` file
3. Restart the application
4. Re-login to get a new token

## 📝 File Structure

```
locallend/
├── .env                    # Your actual secrets (NOT in Git)
├── .env.example            # Template file (in Git)
├── .gitignore              # Ignores .env files
├── pom.xml                 # Added spring-dotenv dependency
└── src/main/resources/
    └── application.properties  # References environment variables
```

## 🔄 Updating `.env.example`

When you add a new environment variable:

1. Add it to your `.env` file with the actual value
2. Add it to `.env.example` with a placeholder value
3. Update this README with documentation
4. Commit `.env.example` (but never `.env`)

## 📚 Additional Resources

- [The Twelve-Factor App - Config](https://12factor.net/config)
- [Spring Boot External Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Environment Variables Security Best Practices](https://blog.gitguardian.com/secrets-api-management/)

## ✅ Checklist

Before deploying to production, ensure:

- [ ] `.env` file is NOT committed to Git
- [ ] Strong JWT secret generated (minimum 256 bits)
- [ ] Production database credentials configured
- [ ] Cloudinary production account credentials set
- [ ] All secrets are different from development
- [ ] Environment variables are set in production environment
- [ ] `.env.example` is up to date with all required variables

---

**Note**: The `.env` file is automatically ignored by Git. Your secrets are safe! 🔐
