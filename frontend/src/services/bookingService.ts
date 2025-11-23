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

  // Confirm booking (owner approves)
  confirmBooking: async (bookingId: string, ownerId: string, ownerNotes?: string): Promise<Booking> => {
    const body = ownerNotes ? { ownerNotes } : {};
    const response = await api.patch(`/api/bookings/${bookingId}/confirm`, body, {
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

  // Activate booking (borrower picks up item)
  activateBooking: async (
    bookingId: string,
    borrowerId: string,
    options?: {
      actualStartDate?: string;
      pickupNotes?: string;
      depositPaid?: boolean;
    }
  ): Promise<Booking> => {
    const response = await api.patch(`/api/bookings/${bookingId}/activate`, options || {}, {
      headers: addUserIdHeader(borrowerId)
    });
    return response;
  },

  // Complete booking (borrower returns item)
  completeBooking: async (
    bookingId: string,
    userId: string,
    options?: {
      actualEndDate?: string;
      returnNotes?: string;
      returnCondition?: string;
    }
  ): Promise<Booking> => {
    const response = await api.patch(`/api/bookings/${bookingId}/complete`, options || {}, {
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
  },

  // Get bookings for items I own (as owner) - using new endpoint
  getMyOwnedBookings: async (ownerId: string): Promise<ApiResponse<Booking[]>> => {
    const response = await api.get('/api/bookings/my-owned', {
      headers: addUserIdHeader(ownerId)
    });
    return response;
  }
};