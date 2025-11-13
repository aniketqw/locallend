import api from './api';
import type { 
  Category, 
  ApiResponse 
} from '../types';

export const categoryService = {
  // Get all categories
  getCategories: async (sort: string = 'name'): Promise<ApiResponse<Category[]>> => {
    const response = await api.get(`/api/categories?sort=${sort}`);
    return response;
  },

  // Get single category
  getCategory: async (categoryId: string): Promise<Category> => {
    const response = await api.get(`/api/categories/${categoryId}`);
    return response;
  },

  // Create category (authenticated)
  createCategory: async (categoryData: { name: string; description?: string; parentCategoryId?: string }): Promise<Category> => {
    const response = await api.post('/api/categories', categoryData);
    return response;
  },

  // Search categories
  searchCategories: async (term: string): Promise<ApiResponse<Category[]>> => {
    const response = await api.get(`/api/categories/search?term=${encodeURIComponent(term)}`);
    return response;
  }
};