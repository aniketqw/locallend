// Login Page Component
import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import type { LoginRequest } from '../types';

export interface LoginPageProps {
  onBack?: () => void;
}

export const LoginPage: React.FC<LoginPageProps> = ({ onBack }) => {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    setIsLoading(true);
    try {
      const loginData: LoginRequest = {
        usernameOrEmail: email, // Backend expects usernameOrEmail, so we pass email
        password: password
      };

      console.log('Login attempt:', loginData);
      await login(loginData);
      
      alert(`Login successful! Welcome back!`);
      // Navigation is handled centrally in App via auth state; no manual redirect needed here
    } catch (error: any) {
      console.error('Login failed:', error);
      
      // Check for network/CORS errors
      if (error.message === 'Failed to fetch') {
        alert('Network error: Cannot connect to backend at http://localhost:8080. Please check:\n1. Backend is running on port 8080\n2. CORS is properly configured\n3. No firewall blocking the connection');
      } else if (error.message === 'Unauthorized') {
        alert('Invalid username/email or password');
      } else {
        // Extract error message from various response formats
        const errorMessage = error?.response?.data?.message || 
                            error?.response?.data?.error || 
                            error?.data?.message || 
                            error?.message || 
                            'Login failed. Please check your credentials.';
        alert(`Login failed: ${errorMessage}`);
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
      backgroundColor: '#f5f5f5'
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '40px',
        borderRadius: '8px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        width: '100%',
        maxWidth: '400px'
      }}>
        <h1 style={{ textAlign: 'center', marginBottom: '30px', color: '#1976d2' }}>
          Login to LocalLend
        </h1>
        
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '20px' }}>
            <label style={{ 
              display: 'block', 
              marginBottom: '5px', 
              fontWeight: 'bold', 
              color: '#000',
              fontSize: '14px',
              fontFamily: 'Arial, sans-serif'
            }}>
              Email
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
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
          
          <div style={{ marginBottom: '30px' }}>
            <label style={{ 
              display: 'block', 
              marginBottom: '5px', 
              fontWeight: 'bold', 
              color: '#000',
              fontSize: '14px',
              fontFamily: 'Arial, sans-serif'
            }}>
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
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
              cursor: isLoading ? 'not-allowed' : 'pointer',
              marginBottom: '20px'
            }}
          >
            {isLoading ? 'Logging In...' : 'Login'}
          </button>
        </form>
        
        <div style={{ textAlign: 'center' }}>
          <p style={{ margin: '10px 0' }}>
            Don't have an account?{' '}
            <button 
              style={{ color: '#1976d2', background: 'none', border: 'none', cursor: 'pointer', textDecoration: 'underline' }}
              onClick={() => console.log('Navigate to register')}
            >
              Register here
            </button>
          </p>
          {onBack && (
            <button 
              onClick={onBack}
              style={{ color: '#666', background: 'none', border: 'none', cursor: 'pointer', textDecoration: 'underline' }}
            >
              ← Back to Home
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

// When React is properly installed, this will become:
/*
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { handleApiError } from '../services/api';

export const LoginPage: React.FC = () => {
  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    
    try {
      await login(formData);
      navigate('/dashboard');
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Typography component="h1" variant="h4">
          Login to LocalLend
        </Typography>
        
        {error && (
          <Alert severity="error" sx={{ mt: 2, width: '100%' }}>
            {error}
          </Alert>
        )}
        
        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1, width: '100%' }}>
          <TextField
            margin="normal"
            required
            fullWidth
            label="Username or Email"
            value={formData.usernameOrEmail}
            onChange={(e) => setFormData(prev => ({ ...prev, usernameOrEmail: e.target.value }))}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            label="Password"
            type="password"
            value={formData.password}
            onChange={(e) => setFormData(prev => ({ ...prev, password: e.target.value }))}
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            disabled={isLoading}
            sx={{ mt: 3, mb: 2 }}
          >
            {isLoading ? 'Logging in...' : 'Login'}
          </Button>
          <Box textAlign="center">
            <Link to="/register">
              Don't have an account? Register here
            </Link>
          </Box>
        </Box>
      </Box>
    </Container>
  );
};
*/