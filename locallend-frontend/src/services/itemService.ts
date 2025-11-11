import api, { addUserIdHeader } from './api';
import type { 
  Item, 
  CreateItemRequest, 
  ApiResponse, 
  SearchParams,
  ItemCondition,
  ItemStatus 
} from '../types';

export const itemService = {
  // Get all items with pagination and filters
  getItems: async (params: SearchParams = {}): Promise<ApiResponse<Item[]>> => {
    const queryParams = new URLSearchParams();
    
    if (params.page !== undefined) queryParams.append('page', params.page.toString());
    if (params.size !== undefined) queryParams.append('size', params.size.toString());
    if (params.sort) queryParams.append('sort', params.sort);
    if (params.categoryId) queryParams.append('categoryId', params.categoryId);
    if (params.ownerId) queryParams.append('ownerId', params.ownerId);
    if (params.condition) queryParams.append('condition', params.condition);
    if (params.status) queryParams.append('status', params.status);
    
    const url = `/api/items${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
    const response = await api.get(url);
    return response;
  },

  // Get single item by ID
  getItem: async (itemId: string): Promise<Item> => {
    const response = await api.get(`/api/items/${itemId}`);
    return response;
  },

  // Create new item
  createItem: async (itemData: CreateItemRequest, userId: string): Promise<Item> => {
    const response = await api.post('/api/items', itemData, {
      headers: addUserIdHeader(userId)
    });
    return response;
  },

  // Update item
  updateItem: async (itemId: string, itemData: Partial<CreateItemRequest>, userId: string): Promise<Item> => {
    const response = await api.put(`/api/items/${itemId}`, itemData, {
      headers: addUserIdHeader(userId)
    });
    return response;
  },

  // Delete item
  deleteItem: async (itemId: string, userId: string): Promise<void> => {
    await api.delete(`/api/items/${itemId}`, {
      headers: addUserIdHeader(userId)
    });
  },

  // Update item status
  updateItemStatus: async (itemId: string, status: ItemStatus, userId: string): Promise<Item> => {
    const response = await api.patch(`/api/items/${itemId}/status`, { status }, {
      headers: addUserIdHeader(userId)
    });
    return response;
  },

  // Search items
  searchItems: async (query: string, categoryId?: string, condition?: ItemCondition): Promise<ApiResponse<Item[]>> => {
    const queryParams = new URLSearchParams();
    queryParams.append('query', query);
    if (categoryId) queryParams.append('categoryId', categoryId);
    if (condition) queryParams.append('condition', condition);
    
    const response = await api.get(`/api/items/search?${queryParams.toString()}`);
    return response;
  }
};