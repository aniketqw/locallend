import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
// bookingService and CreateBookingRequest are not required in this component yet; removed unused imports to avoid build errors

interface BookItemPageProps {
  onBack: () => void;
  item: any;
}

export const BookItemPage: React.FC<BookItemPageProps> = ({ onBack, item }) => {
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    startDate: '',
    endDate: '',
    bookingNotes: '',
    depositAmount: 0,
    acceptTerms: false
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.acceptTerms) {
      alert('Please accept the terms and conditions');
      return;
    }

    // More robust date validation
    const now = new Date();
    const startDate = new Date(formData.startDate);
    const endDate = new Date(formData.endDate);
    
    // Start date must be at least tomorrow
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    
    if (startDate < tomorrow) {
      alert('Start date must be at least tomorrow');
      return;
    }

    if (endDate <= startDate) {
      alert('End date must be after start date');
      return;
    }
    
    // Additional validation checks
    if (!formData.acceptTerms) {
      alert('You must accept the terms and conditions');
      return;
    }
    
    if (!item?.id) {
      alert('Invalid item selected');
      return;
    }

    setIsLoading(true);

    try {
      console.log('🚀 Creating booking for item:', item.id, 'user:', user?.id);
      
      // Create date objects with proper time
      const startDateTime = new Date(formData.startDate + 'T10:00:00');
      const endDateTime = new Date(formData.endDate + 'T18:00:00');
      
      // Calculate duration in days
      const durationDays = Math.max(1, Math.ceil((endDateTime.getTime() - startDateTime.getTime()) / (1000 * 60 * 60 * 24)));
      
      // Format according to integration guide requirements
      // The guide shows format: "2025-11-10T10:00:00" (without Z and milliseconds)
      const formatDateTime = (date: Date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
      };

      const bookingData = {
        itemId: item.id,
        startDate: formatDateTime(startDateTime),  // "2025-11-10T10:00:00"
        endDate: formatDateTime(endDateTime),      // "2025-11-13T18:00:00"
        bookingNotes: (formData.bookingNotes || '').slice(0, 500),
        depositAmount: Number(formData.depositAmount) || 0,
        requestedDurationDays: durationDays,
        acceptTerms: formData.acceptTerms
      };
      
      console.log('📝 Booking data to send:', bookingData);
      console.log('🆔 User ID for X-User-Id header:', user?.id);
      
      if (!user?.id) {
        alert('❌ User not found. Please log in again.');
        return;
      }
      
      // Test backend connectivity first
      try {
        const healthCheck = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/categories`, {
          method: 'GET'
        });
        console.log('🏥 Backend health check:', healthCheck.status);
        if (!healthCheck.ok) {
          alert(`❌ Backend connectivity issue: ${healthCheck.status}. Please check if backend is running on port 8080.`);
          return;
        }
      } catch (connectError) {
        console.error('🏥 Backend connection failed:', connectError);
        alert('❌ Cannot connect to backend. Please ensure backend is running on port 8080.');
        return;
      }
      
      try {
        // First try direct fetch to see exact error
        const token = localStorage.getItem('token');
        console.log('🔐 Using token:', token ? token.substring(0, 20) + '...' : 'NO TOKEN');
        
        const directResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            'X-User-Id': user.id
          },
          body: JSON.stringify(bookingData)
        });
        
        console.log('📡 Direct response status:', directResponse.status);
        console.log('📡 Direct response headers:', Object.fromEntries(directResponse.headers.entries()));
        
        if (!directResponse.ok) {
          const errorText = await directResponse.text();
          console.error('📡 Direct response error body:', errorText);
          
          let errorJson = null;
          try {
            errorJson = JSON.parse(errorText);
          } catch (e) {
            // Error text is not JSON
          }
          
          throw new Error(`HTTP ${directResponse.status}: ${errorJson?.message || errorText || 'Unknown error'}`);
        }
        
        const bookingResponse = await directResponse.json();
        console.log('✅ Direct booking created successfully:', bookingResponse);
        
        // Also test the service layer for comparison
        // const bookingResponse = await bookingService.createBooking(bookingData, user.id);
        console.log('✅ Booking created successfully:', bookingResponse);
        alert(`✅ Booking request sent for "${item.name}"! You can track it in your dashboard.`);
        onBack();
      } catch (serviceError: any) {
        console.error('❌ Booking service failed:', serviceError);
        console.error('Full error object:', JSON.stringify(serviceError, null, 2));
        
        // Try to extract detailed error information
        let detailedError = 'Unknown error occurred';
        if (serviceError?.response) {
          const { status, data } = serviceError.response;
          console.error(`HTTP ${status}:`, data);
          
          if (status === 400) {
            detailedError = `Bad Request (400): ${data?.message || data?.details || JSON.stringify(data)}`;
          } else if (status === 401) {
            detailedError = 'Unauthorized (401): Please log in again';
          } else if (status === 403) {
            detailedError = 'Forbidden (403): Not authorized to create booking';
          } else if (status === 404) {
            detailedError = 'Not Found (404): Booking endpoint not available';
          } else {
            detailedError = `HTTP ${status}: ${data?.message || 'Server error'}`;
          }
        } else if (serviceError?.message) {
          detailedError = serviceError.message;
        }
        
        alert(`❌ Failed to create booking:\n${detailedError}\n\nCheck browser console for detailed logs.`);
      }
    } catch (error) {
      console.error('💥 Error creating booking:', error);
      alert(`❌ Network error creating booking: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  if (!item) {
    return (
      <div style={{ padding: '20px', textAlign: 'center' }}>
        <h2>Item not found</h2>
        <button onClick={onBack} style={{ padding: '10px 20px', backgroundColor: '#1976d2', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
          Back
        </button>
      </div>
    );
  }

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
      <div style={{ marginBottom: '20px' }}>
        <button onClick={onBack} style={{ padding: '8px 16px', backgroundColor: '#666', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
          ← Back
        </button>
      </div>

      <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', marginBottom: '20px' }}>
        <h2 style={{ color: '#1976d2', marginBottom: '10px' }}>Request to Book Item</h2>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '20px' }}>
          {item.images?.[0] && (
            <img src={item.images[0]} alt={item.name} style={{ width: '60px', height: '60px', objectFit: 'cover', borderRadius: '4px', marginRight: '15px' }} />
          )}
          <div>
            <h3 style={{ margin: '0 0 5px 0', color: '#333' }}>{item.name}</h3>
            <p style={{ margin: '0', color: '#666', fontSize: '14px' }}>{item.description}</p>
            <p style={{ margin: '5px 0 0 0', color: '#1976d2', fontSize: '14px', fontWeight: 'bold' }}>Owner: {item.ownerName}</p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit} style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
            Start Date *
          </label>
          <input
            type="date"
            name="startDate"
            value={formData.startDate}
            onChange={handleChange}
            required
            min={new Date().toISOString().split('T')[0]}
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '16px' }}
          />
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
            End Date *
          </label>
          <input
            type="date"
            name="endDate"
            value={formData.endDate}
            onChange={handleChange}
            required
            min={formData.startDate || new Date().toISOString().split('T')[0]}
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '16px' }}
          />
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
            Deposit Amount in ₹ (Optional)
          </label>
          <input
            type="number"
            name="depositAmount"
            value={formData.depositAmount}
            onChange={handleChange}
            min="0"
            step="0.01"
            placeholder="0.00"
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '16px' }}
          />
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
            Notes to Owner (Optional)
          </label>
          <textarea
            name="bookingNotes"
            value={formData.bookingNotes}
            onChange={handleChange}
            rows={4}
            placeholder="Any special requests or information for the owner..."
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '16px', resize: 'vertical' }}
          />
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
            <input
              type="checkbox"
              name="acceptTerms"
              checked={formData.acceptTerms}
              onChange={handleChange}
              required
              style={{ marginRight: '8px' }}
            />
            <span style={{ color: '#333' }}>I accept the terms and conditions for booking this item *</span>
          </label>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            type="button"
            onClick={onBack}
            style={{ 
              flex: 1,
              padding: '12px',
              backgroundColor: '#666',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '16px'
            }}
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isLoading || !formData.acceptTerms}
            style={{
              flex: 2,
              padding: '12px',
              backgroundColor: isLoading || !formData.acceptTerms ? '#ccc' : '#1976d2',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: isLoading || !formData.acceptTerms ? 'not-allowed' : 'pointer',
              fontSize: '16px',
              fontWeight: 'bold'
            }}
          >
            {isLoading ? 'Creating Booking...' : 'Send Booking Request'}
          </button>
        </div>
      </form>
    </div>
  );
};
