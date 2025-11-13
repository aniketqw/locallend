# LocalLend Frontend

A React + TypeScript + Vite application for the LocalLend peer-to-peer item sharing platform.

## 📋 Project Overview

LocalLend is a community platform where users can:
- List items they own for others to borrow
- Browse and search items available in their community  
- Request to borrow items from other users
- Manage bookings (approve, track, complete)
- Rate users and items after completed transactions
- Build trust scores through positive interactions

## 🛠 Technology Stack

- **Frontend Framework:** React 19+ with TypeScript
- **Build Tool:** Vite 7+
- **UI Framework:** Material-UI (MUI) v6
- **Routing:** React Router DOM v6
- **HTTP Client:** Axios (currently using fetch as fallback)
- **Form Handling:** React Hook Form with Yup validation
- **Date Handling:** Day.js
- **State Management:** React Context API
- **Authentication:** JWT tokens with localStorage

## 📁 Project Structure

```
src/
├── components/          # Reusable UI components
├── pages/              # Page-level components
├── services/           # API service layer
├── context/            # React context providers
├── hooks/              # Custom React hooks
├── utils/              # Helper functions and constants
├── types/              # TypeScript type definitions
└── assets/             # Static assets
```

## 🚀 Getting Started

### Prerequisites

- Node.js 18+ and npm/yarn
- Backend API running on http://localhost:8080

### Installation

1. **Install Dependencies** (if npm install fails due to esbuild issues):
   ```bash
   # Remove problematic node_modules if they exist
   rm -rf node_modules package-lock.json
   
   # Install dependencies
   npm install
   
   # If still failing, try with force flag
   npm install --force
   ```

2. **Environment Setup**
   ```bash
   # Create .env file in root directory
   echo "VITE_API_BASE_URL=http://localhost:8080" > .env
   ```

3. **Start Development Server**
   ```bash
   npm run dev
   ```

## 📦 Dependencies Status

The project dependencies are configured in package.json but may need to be installed:

### Core Dependencies (Added to package.json)
- `react` & `react-dom` - React framework
- `react-router-dom` - Client-side routing
- `axios` - HTTP client for API calls
- `@mui/material` - Material-UI component library
- `@emotion/react` & `@emotion/styled` - CSS-in-JS for MUI
- `@mui/icons-material` - Material Design icons

### Form & Validation
- `react-hook-form` - Form state management
- `@hookform/resolvers` - Form validation resolvers
- `yup` - Schema validation

### Date Handling
- `dayjs` - Date manipulation library
- `@mui/x-date-pickers` - MUI date picker components

### Notifications
- `react-hot-toast` - Toast notifications

## 🏗 API Integration

The frontend integrates with the LocalLend backend API following these patterns:

### Authentication
- JWT tokens stored in localStorage
- Automatic token refresh on API calls
- Redirect to login on 401 responses

### API Services (Implemented)
- `authService` - User authentication
- `itemService` - Item CRUD operations
- `bookingService` - Booking lifecycle management
- `categoryService` - Category management
- `ratingService` - User and item ratings
- `userService` - User profile operations

### Required Headers
```javascript
// For authenticated requests
Authorization: Bearer <jwt_token>

// For owner/borrower specific operations
X-User-Id: <user_id>
```

## 📱 Key Features (Designed)

### Authentication Flow
1. User registration/login
2. JWT token storage
3. Protected route navigation
4. Automatic session management

### Item Management
1. Browse items with pagination/filters
2. Create/edit item listings
3. Upload item images
4. Category-based organization

### Booking System
1. Request to borrow items
2. Owner approval workflow
3. Booking status tracking
4. Start/complete booking actions

### Rating System
1. Rate users after completed bookings
2. Rate items based on experience
3. Trust score calculation
4. Community feedback display

## 🎨 UI Components Structure (Planned)

### Pages (Major Views)
- `HomePage` - Item browsing and search
- `LoginPage` - User authentication ✅ (Structure created)
- `RegisterPage` - Account creation ✅ (Structure created)
- `DashboardPage` - User dashboard
- `ItemDetailPage` - Individual item view
- `BookingPage` - Booking management
- `ProfilePage` - User profile settings

### Components (Reusable)
- `Navbar` - Navigation with search
- `ItemCard` - Item display component
- `BookingCard` - Booking status display
- `RatingForm` - Rating submission
- `LoadingSpinner` - Loading states
- `ErrorBoundary` - Error handling

## 🔧 Development Commands

```bash
# Development server
npm run dev

# Production build
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

## 🌐 API Endpoints Integration

The frontend connects to these key backend endpoints:

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### Items
- `GET /api/items` - Browse items (public)
- `POST /api/items` - Create item (authenticated)
- `GET /api/items/{id}` - Item details
- `PUT /api/items/{id}` - Update item
- `PATCH /api/items/{id}/status` - Update status

### Bookings
- `POST /api/bookings` - Create booking request
- `GET /api/bookings` - My bookings
- `PATCH /api/bookings/{id}/approve` - Approve booking
- `PATCH /api/bookings/{id}/start` - Start booking
- `PATCH /api/bookings/{id}/complete` - Complete booking

### Ratings
- `GET /api/ratings/can-rate/{bookingId}` - Check if can rate
- `POST /api/ratings` - Submit rating

## 🚨 Current Status

✅ **Completed:**
- Project scaffolding and structure
- TypeScript type definitions (complete API coverage)
- API service layer with fetch-based client
- Authentication context setup
- Basic component structure (placeholder)
- Form validation utilities
- Constants and helper functions
- Project documentation

⏳ **Pending (requires dependency installation):**
- Material-UI component integration
- React Router implementation
- Form handling with React Hook Form
- Complete page implementations
- Image upload functionality
- Real-time notifications
- Error boundary implementation

## 🛠 Implementation Roadmap

### Phase 1: Dependencies & Core Setup
1. Resolve npm installation issues
2. Install all required dependencies
3. Set up Material-UI theme provider
4. Implement React Router with protected routes

### Phase 2: Authentication & Navigation
1. Complete Login/Register pages with MUI components
2. Implement navigation bar with search
3. Set up authentication guards
4. Add toast notification system

### Phase 3: Core Features
1. Home page with item listings
2. Item detail and creation pages
3. User dashboard and profile pages
4. Booking request and management system

### Phase 4: Advanced Features
1. Rating and review system
2. Real-time notifications
3. Image upload handling
4. Search and filter functionality

### Phase 5: Polish & Optimization
1. Loading states and error handling
2. Responsive design improvements
3. Performance optimizations
4. Testing implementation

## 🔗 Backend Integration

This frontend is designed to work with the LocalLend backend API. Ensure the backend is running on `http://localhost:8080` before starting the frontend development server.

### Backend Requirements
- Spring Boot 3.5.6 application
- MongoDB database
- JWT authentication enabled
- CORS configured for `http://localhost:5173` (Vite dev server)

## 📚 Documentation Reference

- [Frontend Integration Guide](../FRONTEND_INTEGRATION_GUIDE.txt) - Complete API reference
- [React Documentation](https://react.dev/)
- [Material-UI Documentation](https://mui.com/)
- [Vite Documentation](https://vitejs.dev/)

## 🤝 Contributing

1. Follow the existing code structure and patterns
2. Implement proper TypeScript typing
3. Add proper error handling for all API calls
4. Follow the authentication patterns established
5. Test with the backend API endpoints

## ⚠️ Known Issues

1. **NPM Installation**: There are currently issues with installing dependencies due to esbuild conflicts
2. **React Types**: JSX types are not available until React dependencies are properly installed
3. **Import Meta**: Environment variables access needs proper Vite typing

## 🔧 Troubleshooting

### NPM Installation Issues
If you encounter esbuild-related errors during `npm install`:

```bash
# Method 1: Clean install
rm -rf node_modules package-lock.json
npm cache clean --force
npm install

# Method 2: Force install
npm install --force

# Method 3: Use different package manager
yarn install
# or
pnpm install
```

### Development Server Issues
If the dev server fails to start:

1. Ensure you're in the correct directory (`locallend-frontend/`)
2. Verify package.json exists in the current directory
3. Try running with verbose output: `npm run dev --verbose`

---

**Note:** This project is currently in development phase with complete architectural planning done. All core functionality is designed and ready for implementation once dependency installation is resolved.