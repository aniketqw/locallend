// API Constants
export const API_BASE_URL = 'http://localhost:8080';

// Status constants
export const ITEM_CONDITIONS = {
  NEW: 'NEW',
  EXCELLENT: 'EXCELLENT',
  GOOD: 'GOOD',
  FAIR: 'FAIR',
  POOR: 'POOR'
} as const;

export const ITEM_STATUSES = {
  AVAILABLE: 'AVAILABLE',
  UNAVAILABLE: 'UNAVAILABLE',
  BORROWED: 'BORROWED'
} as const;

export const BOOKING_STATUSES = {
  PENDING: 'PENDING',
  CONFIRMED: 'CONFIRMED',
  ACTIVE: 'ACTIVE',
  COMPLETED: 'COMPLETED',
  REJECTED: 'REJECTED',
  CANCELLED: 'CANCELLED'
} as const;

export const USER_ROLES = {
  USER: 'USER',
  ADMIN: 'ADMIN'
} as const;

export const RATING_TYPES = {
  BORROWER_TO_OWNER: 'BORROWER_TO_OWNER',
  OWNER_TO_BORROWER: 'OWNER_TO_BORROWER',
  ITEM_RATING: 'ITEM_RATING'
} as const;

// Status color mappings for UI
export const STATUS_COLORS = {
  PENDING: '#FFA726', // Orange
  CONFIRMED: '#42A5F5', // Blue
  ACTIVE: '#66BB6A', // Green
  COMPLETED: '#9E9E9E', // Gray
  REJECTED: '#EF5350', // Red
  CANCELLED: '#EF5350', // Red
  AVAILABLE: '#66BB6A', // Green
  UNAVAILABLE: '#FFA726', // Orange
  BORROWED: '#42A5F5' // Blue
};

// Pagination defaults
export const DEFAULT_PAGE_SIZE = 10;
export const DEFAULT_PAGE_NUMBER = 0;

// Form validation constants
export const MIN_USERNAME_LENGTH = 3;
export const MAX_USERNAME_LENGTH = 50;
export const MIN_NAME_LENGTH = 2;
export const MAX_NAME_LENGTH = 100;
export const MIN_PASSWORD_LENGTH = 8;
export const MAX_ITEM_NAME_LENGTH = 200;
export const MAX_ITEM_DESCRIPTION_LENGTH = 2000;
export const MAX_BOOKING_NOTES_LENGTH = 500;
export const MAX_RATING_COMMENT_LENGTH = 1000;

// Trust score constants
export const DEFAULT_TRUST_SCORE = 5.0;
export const MIN_TRUST_SCORE = 0;
export const MAX_TRUST_SCORE = 5;

// Rating constants
export const MIN_RATING = 1;
export const MAX_RATING = 5;

// Local storage keys
export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER: 'user',
  THEME: 'theme',
  RECENT_SEARCHES: 'recent_searches'
} as const;

// Route paths
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  ITEMS: '/items',
  ITEM_DETAIL: '/items/:id',
  ITEM_CREATE: '/my-items/new',
  ITEM_EDIT: '/my-items/:id/edit',
  MY_ITEMS: '/my-items',
  MY_BOOKINGS: '/my-bookings',
  BOOKINGS_RECEIVED: '/bookings-received',
  BOOKING_DETAIL: '/bookings/:id',
  BOOKING_CREATE: '/items/:itemId/book',
  PROFILE: '/profile',
  PROFILE_RATINGS: '/profile/ratings',
  CATEGORIES: '/categories'
} as const;

// Error messages
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  UNAUTHORIZED: 'Your session has expired. Please login again.',
  FORBIDDEN: 'You do not have permission to perform this action.',
  NOT_FOUND: 'The requested resource was not found.',
  VALIDATION_ERROR: 'Please check your input and try again.',
  GENERIC_ERROR: 'An unexpected error occurred. Please try again.'
} as const;