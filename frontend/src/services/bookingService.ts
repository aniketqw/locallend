import api, { addUserIdHeader } from './api';
import type { 
  Booking, 
  CreateBookingRequest, 
  ApiResponse, 
  BookingStatus 
} from '../types';

export const bookingService = {
  // Create new booking request
  createBooking: async (bookingData: CreateBookingRequest, borrowerId: string): Promise<Booking> => {
    const response = await api.post('/api/bookings', bookingData, {
      headers: addUserIdHeader(borrowerId)
    });
    return response;
  },

  // Get all bookings (as borrower)
  getMyBookings: async (status?: BookingStatus): Promise<ApiResponse<Booking[]>> => {
    const url = status ? `/api/bookings?status=${status}` : '/api/bookings';
    const response = await api.get(url);
    return response;
  },

  // Get bookings for items I own (as owner)
  getBookingsReceived: async (): Promise<ApiResponse<Booking[]>> => {
    const response = await api.get('/api/bookings/received');
    return response;
  },

  // Get pending approvals (for owners)
  getPendingApprovals: async (): Promise<ApiResponse<Booking[]>> => {
    const response = await api.get('/api/bookings/pending-approvals');
    return response;
  },

  // Get single booking details
  getBooking: async (bookingId: string): Promise<Booking> => {
    const response = await api.get(`/api/bookings/${bookingId}`);
    return response;
  },

  // Approve booking (owner)
  approveBooking: async (bookingId: string, ownerId: string): Promise<Booking> => {
    const response = await api.patch(`/api/bookings/${bookingId}/approve`, null, {
      headers: addUserIdHeader(ownerId)
    });
    return response;
  },

  // Reject booking (owner)
  rejectBooking: async (bookingId: string, reason: string, ownerId: string): Promise<Booking> => {
    const response = await api.patch(`/api/bookings/${bookingId}/reject`, { reason }, {
      headers: addUserIdHeader(ownerId)
    });
    return response;
  },

  // Start booking (borrower)
  startBooking: async (bookingId: string, borrowerId: string): Promise<Booking> => {
    const response = await api.patch(`/api/bookings/${bookingId}/start`, null, {
      headers: addUserIdHeader(borrowerId)
    });
    return response;
  },

  // Complete booking (borrower or owner)
  completeBooking: async (bookingId: string, userId: string): Promise<Booking> => {
    const response = await api.patch(`/api/bookings/${bookingId}/complete`, null, {
      headers: addUserIdHeader(userId)
    });
    return response;
  },

  // Cancel booking
  cancelBooking: async (bookingId: string, reason: string, userId: string): Promise<Booking> => {
    const response = await api.patch(`/api/bookings/${bookingId}/cancel`, { reason }, {
      headers: addUserIdHeader(userId)
    });
    return response;
  }
};