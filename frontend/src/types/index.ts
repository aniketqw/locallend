// User Types
export interface User {
  id: string;
  username: string;
  name: string;
  email: string;
  phoneNumber?: string;
  profileImageUrl?: string;
  role: 'USER' | 'ADMIN';
  isActive: boolean;
  trustScore: number;
  location?: {
    type: 'Point';
    coordinates: [number, number]; // [longitude, latitude]
  };
  address?: {
    street: string;
    city: string;
    state: string;
    zipCode: string;
    country: string;
  };
  createdDate: string;
  lastLogin?: string;
  totalRatingsReceived: number;
  ratingCountAsBorrower: number;
  ratingCountAsLender: number;
  averageRatingAsBorrower: number;
  averageRatingAsLender: number;
  itemCount?: number;
  memberSince?: string;
  totalItemsShared?: number;
  totalBookings?: number;
}

export interface PublicUser {
  id: string;
  username: string;
  name: string;
  profileImageUrl?: string;
  trustScore: number;
  itemCount: number;
  memberSince?: string;
}

// Auth Types
export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  name: string;
  email: string;
  password: string;
  phoneNumber?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
}

// Item Types
export type ItemCondition = 'NEW' | 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR';
export type ItemStatus = 'AVAILABLE' | 'UNAVAILABLE' | 'BORROWED';

export interface Item {
  id: string;
  name: string;
  description: string;
  condition: ItemCondition;
  status: ItemStatus;
  deposit: number;
  images: string[];
  averageRating: number;
  ownerId: string;
  ownerName: string;
  categoryId: string;
  categoryName: string;
  canBeBorrowed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateItemRequest {
  name: string;
  description: string;
  categoryId: string;
  deposit?: number;
  images?: string[];
  condition: ItemCondition;
}

// Category Types
export interface Category {
  id: string;
  name: string;
  description?: string;
  parent_category_id?: string;
  parent_category_name?: string;
  is_active: boolean;
  item_count: number;
  has_subcategories: boolean;
  created_at: string;
  updated_at: string;
}

// Booking Types
export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'ACTIVE' | 'COMPLETED' | 'REJECTED' | 'CANCELLED';

export interface Booking {
  id: string;
  itemId: string;
  itemName: string;
  itemImageUrl?: string;
  borrowerId: string;
  borrowerName: string;
  ownerId: string;
  ownerName: string;
  status: BookingStatus;
  startDate: string;
  endDate: string;
  actualStartDate?: string;
  actualEndDate?: string;
  bookingNotes?: string;
  ownerNotes?: string;
  depositAmount: number;
  depositPaid: boolean;
  createdDate: string;
  updatedDate: string;
  confirmedDate?: string;
  cancelledDate?: string;
  cancellationReason?: string;
  isRated: boolean;
  durationDays: number;
  statusDescription: string;
  timeAgo: string;
  daysUntilStart: number;
  daysUntilEnd: number;
  canBeCancelled: boolean;
  canBeConfirmed: boolean;
  canBeActivated: boolean;
  canBeCompleted: boolean;
  isOverdue: boolean;
  requiresDeposit: boolean;
}

export interface CreateBookingRequest {
  itemId: string;
  startDate: string;
  endDate: string;
  bookingNotes?: string;
  depositAmount: number;
  acceptTerms: boolean;
}

// Rating Types
export type RatingType = 'BORROWER_TO_OWNER' | 'OWNER_TO_BORROWER' | 'ITEM_RATING';

export interface Rating {
  id: string;
  raterId: string;
  raterName: string;
  rateeId?: string;
  rateeName?: string;
  itemId?: string;
  itemName?: string;
  bookingId: string;
  ratingType: RatingType;
  ratingValue: number; // 1-5
  comment?: string;
  isAnonymous: boolean;
  createdDate: string;
}

export interface CreateRatingRequest {
  rateeId?: string;
  itemId?: string;
  bookingId: string;
  ratingType: RatingType;
  ratingValue: number;
  comment?: string;
  isAnonymous?: boolean;
}

export interface CanRateResponse {
  can_rate: boolean;
  message: string;
}

// API Response Types
export interface ApiResponse<T> {
  data?: T;
  content?: T; // for paginated responses
  success?: boolean;
  message?: string;
  count?: number;
  totalElements?: number;
  totalPages?: number;
  first?: boolean;
  last?: boolean;
  pageable?: any;
}

export interface ErrorResponse {
  status: number;
  message: string;
  timestamp: string;
  details?: string;
  error_code?: string;
}

// Search Types
export interface SearchParams {
  query?: string;
  page?: number;
  size?: number;
  sort?: string;
  categoryId?: string;
  ownerId?: string;
  condition?: ItemCondition;
  status?: ItemStatus;
}

// Form Types
export interface ItemFormData {
  name: string;
  description: string;
  categoryId: string;
  deposit: number;
  condition: ItemCondition;
  images: string[];
}

export interface BookingFormData {
  startDate: Date | null;
  endDate: Date | null;
  bookingNotes: string;
  acceptTerms: boolean;
}

export interface RatingFormData {
  rating: number;
  comment: string;
  isAnonymous: boolean;
}

export interface ProfileFormData {
  name: string;
  email: string;
  phoneNumber: string;
  address?: {
    street: string;
    city: string;
    state: string;
    zipCode: string;
    country: string;
  };
}