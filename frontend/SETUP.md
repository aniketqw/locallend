# LocalLend Frontend - Installation & Setup Guide

## 🚀 Quick Start

This guide will help you set up the LocalLend frontend application.

### Prerequisites

1. **Node.js 18+** - Download from [nodejs.org](https://nodejs.org/)
2. **Backend API** - Ensure the LocalLend backend is running on `http://localhost:8080`
3. **Git** (optional) - For version control

### Step 1: Clone or Navigate to Project

```bash
# If you're working with the existing project
cd "d:\Bits\OOAD\Project OOAD\locallend_front\locallend-frontend"
```

### Step 2: Install Dependencies

The project may have npm installation issues due to esbuild conflicts. Try these approaches:

#### Method 1: Clean Install
```bash
# Remove existing node_modules if present
rm -rf node_modules package-lock.json

# Clear npm cache
npm cache clean --force

# Install dependencies
npm install
```

#### Method 2: Force Install (if Method 1 fails)
```bash
npm install --force
```

#### Method 3: Alternative Package Manager
```bash
# Using Yarn (install yarn first: npm install -g yarn)
yarn install

# Or using pnpm (install pnpm first: npm install -g pnpm)
pnpm install
```

### Step 3: Environment Configuration

```bash
# Copy environment template
cp .env.example .env

# Edit .env file with your settings (optional, defaults should work)
# VITE_API_BASE_URL=http://localhost:8080
```

### Step 4: Start Development Server

```bash
npm run dev
```

The application should open at `http://localhost:5173`

## 🛠 Troubleshooting

### Common Issues

#### 1. NPM Install Fails with esbuild Error
```
Error: EBUSY: resource busy or locked, rmdir 'node_modules\esbuild'
```

**Solution:**
- Close VS Code and any other processes that might be using the files
- Delete `node_modules` folder manually
- Try `npm install --force`

#### 2. Development Server Won't Start
```
npm error code ENOENT
npm error syscall open
npm error path package.json
```

**Solution:**
- Ensure you're in the correct directory (`locallend-frontend/`)
- Verify `package.json` exists
- Use `pwd` (PowerShell) to check current directory

#### 3. TypeScript/JSX Errors
```
JSX element implicitly has type 'any'
Cannot find module 'react'
```

**Solution:**
- These errors are expected until React dependencies are properly installed
- Complete the installation process first, then restart the dev server

#### 4. API Connection Issues
```
Network Error / CORS Error
```

**Solution:**
- Ensure backend API is running on `http://localhost:8080`
- Check backend CORS configuration allows `http://localhost:5173`
- Verify API endpoints are accessible via browser

### Development Notes

1. **Current Status:** The project structure is complete with all TypeScript types, services, and component structures ready
2. **Dependencies:** Some dependencies may need to be installed/resolved
3. **Components:** Most components are created as placeholders and will render properly once React is loaded
4. **API Integration:** All API services are implemented and ready to connect to the backend

## 📦 Project Structure Overview

```
locallend-frontend/
├── src/
│   ├── components/        # UI components (Navbar, etc.)
│   ├── pages/            # Page components (Home, Login, etc.) 
│   ├── services/         # API service layer
│   ├── context/          # React contexts (Auth)
│   ├── utils/            # Helper functions
│   ├── types/            # TypeScript definitions
│   └── assets/           # Static files
├── public/               # Public assets
├── package.json          # Dependencies and scripts
├── vite.config.ts       # Vite configuration
├── tsconfig.json        # TypeScript configuration
└── README-LOCALLEND.md  # Detailed documentation
```

## 🔗 Next Steps After Installation

1. **Verify Backend Connection:** Open browser dev tools and check for successful API calls
2. **Test Authentication:** Try to register/login a user
3. **Browse Items:** Navigate through the item listing functionality  
4. **Create Test Data:** Use the backend to create some test categories and items
5. **UI Development:** Continue implementing the Material-UI components

## 📚 Documentation

- **Complete API Guide:** `../FRONTEND_INTEGRATION_GUIDE.txt`
- **Project Documentation:** `README-LOCALLEND.md`
- **Backend Setup:** Refer to backend documentation for API setup

## 🆘 Support

If you encounter issues:

1. Check this troubleshooting guide first
2. Review the error messages carefully
3. Ensure backend API is running and accessible
4. Check network/CORS configuration
5. Try alternative installation methods above

---

**Note:** This project follows the exact specifications from the Frontend Integration Guide to ensure seamless backend compatibility.