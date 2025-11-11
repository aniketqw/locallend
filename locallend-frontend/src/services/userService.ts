import api from './api';
import type { 
  PublicUser, 
  ApiResponse 
} from '../types';

export const userService = {
  // Get public user profile
  getPublicUser: async (userId: string): Promise<PublicUser> => {
    const response = await api.get(`/api/users/${userId}/public`);
    return response;
  },

  // Search users
  searchUsers: async (query: string): Promise<ApiResponse<PublicUser[]>> => {
    const response = await api.get(`/api/users/search?query=${encodeURIComponent(query)}`);
    return response;
  }
};