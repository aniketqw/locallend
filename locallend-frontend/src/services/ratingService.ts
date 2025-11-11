import api from './api';
import type { 
  Rating, 
  CreateRatingRequest, 
  CanRateResponse,
  ApiResponse 
} from '../types';

export const ratingService = {
  // Check if user can rate a booking
  canRateBooking: async (bookingId: string): Promise<CanRateResponse> => {
    const response = await api.get(`/api/ratings/can-rate/${bookingId}`);
    return response;
  },

  // Create a rating
  createRating: async (ratingData: CreateRatingRequest): Promise<Rating> => {
    const response = await api.post('/api/ratings', ratingData);
    return response;
  },

  // Get ratings for a user
  getUserRatings: async (userId: string, type?: 'received' | 'given'): Promise<ApiResponse<Rating[]>> => {
    const url = type ? `/api/ratings/user/${userId}?type=${type}` : `/api/ratings/user/${userId}`;
    const response = await api.get(url);
    return response;
  },

  // Get ratings for an item
  getItemRatings: async (itemId: string): Promise<ApiResponse<Rating[]>> => {
    const response = await api.get(`/api/ratings/item/${itemId}`);
    return response;
  }
};