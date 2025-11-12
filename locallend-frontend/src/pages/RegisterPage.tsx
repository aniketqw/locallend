// Register Page Component
import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import type { RegisterRequest } from '../types';

export interface RegisterPageProps {
  onBack?: () => void;
}

export const RegisterPage: React.FC<RegisterPageProps> = ({ onBack }) => {
  const { register } = useAuth();
  const [formData, setFormData] = useState({
    username: '',
    name: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: ''
  });
  const [isLoading, setIsLoading] = useState(false);

  // Test backend connectivity
  const testBackendConnection = async () => {
    const backendUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    console.log('🔍 Testing connection to:', backendUrl);
    
    try {
      // Test multiple endpoints to find what works
      const testEndpoints = [
        '/api/categories',
        '/api/auth/login', // This should exist according to the guide
        '/actuator/health', // Common Spring Boot health endpoint
        '/', // Basic root endpoint
      ];
      
      let successFound = false;
      let results = [];
      
      for (const endpoint of testEndpoints) {
        try {
          console.log(`Testing ${backendUrl}${endpoint}`);
          const response = await fetch(`${backendUrl}${endpoint}`, {
            method: 'GET',
            mode: 'cors',
            headers: {
              'Content-Type': 'application/json',
            },
          });
          
          console.log(`Response for ${endpoint}:`, response.status, response.statusText);
          const responseText = await response.text();
          console.log(`Response body:`, responseText);
          
          results.push(`${endpoint}: ${response.status} ${response.statusText} - ${responseText.substring(0, 100)}`);
          
          if (response.ok) {
            successFound = true;
            alert(`✅ Backend connection successful!\nURL: ${backendUrl}${endpoint}\nStatus: ${response.status}\nResponse: ${responseText.substring(0, 200)}...\n\nBackend is reachable!`);
            break;
          }
        } catch (err: any) {
          console.error(`Error testing ${endpoint}:`, err);
          results.push(`${endpoint}: ${err.message}`);
        }
      }
      
      if (!successFound) {
        alert(`❌ Cannot connect to backend:\nURL: ${backendUrl}\n\nTest Results:\n${results.join('\n')}\n\nPossible issues:\n1. Backend not running on port 8080\n2. Backend running on different port\n3. CORS not configured\n4. Backend bound to localhost only`);
      }
      
    } catch (error: any) {
      console.error('Connection test failed:', error);
      alert(`❌ Network error:\nURL: ${backendUrl}\nError: ${error.message}\n\nCheck:\n1. Is backend running?\n2. What port is it on?\n3. Is Docker networking working?`);
    }
  };

  // Test alternative ports
  const testAlternativePort = async (port: number) => {
    const testUrl = `http://localhost:${port}`;
    console.log(`🔍 Testing alternative port: ${testUrl}`);
    
    try {
      const response = await fetch(`${testUrl}/api/categories`, {
        method: 'GET',
        mode: 'cors',
      });
      
      if (response.ok) {
        alert(`✅ Found backend on port ${port}!\nURL: ${testUrl}\nUpdate your .env file:\nVITE_API_BASE_URL=http://localhost:${port}`);
      } else {
        alert(`⚠️ Port ${port} responded with: ${response.status} ${response.statusText}`);
      }
    } catch (error: any) {
      console.log(`Port ${port}: ${error.message}`);
      alert(`❌ Port ${port}: ${error.message}`);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Client-side validation to match backend requirements
    const validationErrors: string[] = [];
    
    if (formData.username.length < 3 || formData.username.length > 50) {
      validationErrors.push('Username must be between 3 and 50 characters');
    }
    
    if (formData.name.length < 2 || formData.name.length > 100) {
      validationErrors.push('Full name must be between 2 and 100 characters');
    }
    
    if (formData.password.length < 8) {
      validationErrors.push('Password must be at least 8 characters long');
    }
    
    if (formData.password !== formData.confirmPassword) {
      validationErrors.push('Passwords do not match');
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      validationErrors.push('Please enter a valid email address');
    }
    
    if (validationErrors.length > 0) {
      alert('Validation errors:\n' + validationErrors.join('\n'));
      return;
    }

    setIsLoading(true);
    try {
      // Prepare data for backend (excluding confirmPassword)
      const registerData: RegisterRequest = {
        username: formData.username,
        name: formData.name,
        email: formData.email,
        password: formData.password,
        phoneNumber: formData.phoneNumber || undefined
      };

      console.log('Registration attempt:', registerData);
      console.log('Attempting to connect to backend at:', import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080');
      
      await register(registerData);
      
      alert(`Registration successful! Welcome ${formData.name}!`);
      
      // Redirect back to home after successful registration
      if (onBack) {
        onBack();
      }
    } catch (error: any) {
      console.error('Registration failed:', error);
      
      // Check for network/CORS errors
      if (error.message === 'Failed to fetch') {
        alert('Network error: Cannot connect to backend at ' + (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080') + '. Please check:\n1. Backend is running on port 8080\n2. CORS is properly configured\n3. Backend container is accessible from browser');
      } else if (error.message === 'Unauthorized') {
        alert('Authentication error: Please check your credentials');
      } else {
        // Extract error message from various response formats
        const errorMessage = error?.response?.data?.message || 
                            error?.response?.data?.error || 
                            error?.data?.message || 
                            error?.message || 
                            'Registration failed. Please try again.';
        alert(`Registration failed: ${errorMessage}`);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ 
      minHeight: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center',
      backgroundColor: '#f5f5f5',
      padding: '20px'
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '40px',
        borderRadius: '8px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        width: '100%',
        maxWidth: '500px'
      }}>
        <h1 style={{ textAlign: 'center', marginBottom: '30px', color: '#1976d2' }}>
          Join LocalLend
        </h1>
        
        {/* Backend Connection Test Buttons */}
        <div style={{ marginBottom: '20px' }}>
          <button
            type="button"
            onClick={testBackendConnection}
            style={{
              width: '100%',
              padding: '8px',
              backgroundColor: '#f0f0f0',
              color: '#666',
              border: '1px solid #ccc',
              borderRadius: '4px',
              fontSize: '14px',
              cursor: 'pointer',
              marginBottom: '8px'
            }}
          >
            🔍 Test Backend Connection (Port 8080)
          </button>
          
          <div style={{ display: 'flex', gap: '5px' }}>
            {[8081, 8082, 9090, 3000].map(port => (
              <button
                key={port}
                type="button"
                onClick={() => testAlternativePort(port)}
                style={{
                  flex: 1,
                  padding: '6px',
                  backgroundColor: '#e8f4fd',
                  color: '#1976d2',
                  border: '1px solid #1976d2',
                  borderRadius: '3px',
                  fontSize: '12px',
                  cursor: 'pointer'
                }}
              >
                :{port}
              </button>
            ))}
          </div>
        </div>
        
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#000' }}>
              Username
            </label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
              minLength={3}
              maxLength={50}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '16px',
                boxSizing: 'border-box'
              }}
            />
          </div>
          
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#000' }}>
              Full Name
            </label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              minLength={2}
              maxLength={100}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '16px',
                boxSizing: 'border-box'
              }}
            />
          </div>
          
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#000' }}>
              Email
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '16px',
                boxSizing: 'border-box'
              }}
            />
          </div>
          
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#000' }}>
              Phone Number (Optional)
            </label>
            <input
              type="tel"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '16px',
                boxSizing: 'border-box'
              }}
            />
          </div>
          
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#000' }}>
              Password (Minimum 8 characters)
            </label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              minLength={8}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '16px',
                boxSizing: 'border-box'
              }}
            />
          </div>
          
          <div style={{ marginBottom: '30px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#000' }}>
              Confirm Password
            </label>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              minLength={8}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '16px',
                boxSizing: 'border-box'
              }}
            />
          </div>
          
          <button
            type="submit"
            disabled={isLoading}
            style={{
              width: '100%',
              padding: '12px',
              backgroundColor: isLoading ? '#ccc' : '#1976d2',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '16px',
              fontWeight: 'bold',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              marginBottom: '10px'
            }}
          >
            {isLoading ? 'Signing Up...' : 'Sign Up'}
          </button>
          
          {onBack && (
            <button
              type="button"
              onClick={onBack}
              style={{
                width: '100%',
                padding: '12px',
                backgroundColor: '#f5f5f5',
                color: '#333',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '16px',
                cursor: 'pointer'
              }}
            >
              Back to Home
            </button>
          )}
        </form>
      </div>
    </div>
  );
};
