// API Configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Mock API interface for development (will be replaced with axios)
export interface ApiClient {
  get: (url: string, config?: any) => Promise<any>;
  post: (url: string, data?: any, config?: any) => Promise<any>;
  put: (url: string, data?: any, config?: any) => Promise<any>;
  patch: (url: string, data?: any, config?: any) => Promise<any>;
  delete: (url: string, config?: any) => Promise<any>;
}

// Fetch-based API client (temporary until axios is installed)
const createFetchClient = (): ApiClient => {
  const makeRequest = async (url: string, options: RequestInit = {}) => {
    const token = localStorage.getItem('token');
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };
    
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const fullUrl = `${API_BASE_URL}${url}`;
    console.log(`🌐 API Request: ${options.method || 'GET'} ${fullUrl}`);
    console.log('📤 Request headers:', headers);
    if (options.body) {
      console.log('📦 Request body:', options.body);
    }

    let response: Response;
    try {
      response = await fetch(fullUrl, {
        ...options,
        headers,
        mode: 'cors', // Explicitly set CORS mode
      });
      
      console.log(`📨 Response: ${response.status} ${response.statusText}`);
    } catch (error: any) {
      console.error('❌ Network Error:', error);
      // Handle network errors (CORS, connection refused, etc.)
      if (error.name === 'TypeError' || error.message === 'Failed to fetch') {
        throw new Error('Failed to fetch');
      }
      throw error;
    }

    if (response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
      throw new Error('Unauthorized');
    }

    if (!response.ok) {
      let errorData: any = {};
      try {
        errorData = await response.json();
      } catch (e) {
        // If response is not JSON, create error based on status
        errorData = { 
          message: `HTTP ${response.status}: ${response.statusText}` 
        };
      }
      
      const error = new Error(errorData.message || errorData.error || `Request failed with status ${response.status}`);
      (error as any).response = { 
        status: response.status, 
        data: errorData 
      };
      throw error;
    }

    return response.json();
  };

  return {
    get: (url: string, config?: any) => makeRequest(url, { method: 'GET', ...config }),
    post: (url: string, data?: any, config?: any) => 
      makeRequest(url, { method: 'POST', body: JSON.stringify(data), ...config }),
    put: (url: string, data?: any, config?: any) => 
      makeRequest(url, { method: 'PUT', body: JSON.stringify(data), ...config }),
    patch: (url: string, data?: any, config?: any) => 
      makeRequest(url, { method: 'PATCH', body: JSON.stringify(data), ...config }),
    delete: (url: string, config?: any) => makeRequest(url, { method: 'DELETE', ...config }),
  };
};

const api = createFetchClient();

// Helper function to handle API errors
export const handleApiError = (error: any): string => {
  if (error.message) {
    return error.message;
  }
  return 'An unexpected error occurred';
};

// Helper function to add X-User-Id header when needed
export const addUserIdHeader = (userId: string) => {
  return { 'X-User-Id': userId };
};

export default api;