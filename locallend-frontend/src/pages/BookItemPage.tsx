import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

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

    if (new Date(formData.startDate) <= new Date()) {
      alert('Start date must be in the future');
      return;
    }

    if (new Date(formData.endDate) <= new Date(formData.startDate)) {
      alert('End date must be after start date');
      return;
    }

    setIsLoading(true);

    try {
      console.log('🚀 Creating booking for item:', item.id, 'user:', user?.id);
      
      // Helper to produce canonical ISO (trim milliseconds) and a local no-Z variant
      const isoNoMillis = (d: Date) => d.toISOString().replace(/\..+Z$/, 'Z'); // drop milliseconds for cleaner backend parsing
      const localNoZone = (d: Date) => {
        const pad = (n: number) => n.toString().padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
      };

      const startDateTime = new Date(formData.startDate + 'T10:00:00');
      const endDateTime = new Date(formData.endDate + 'T18:00:00');
      
      // Primary payload uses snake_case to match Mongo keys (start_date/end_date, etc.)
      const bookingDataSnake = {
        item_id: item.id,
        start_date: localNoZone(startDateTime),
        end_date: localNoZone(endDateTime),
        booking_notes: (formData.bookingNotes || '').slice(0, 500),
        deposit_amount: Number(formData.depositAmount) || 0,
        duration_days: Math.max(1, Math.ceil((endDateTime.getTime() - startDateTime.getTime()) / (1000 * 60 * 60 * 24))),
        accept_terms: formData.acceptTerms === true
      } as const;

      // Fallback camelCase (if API expects DTO mapping per guide)
      const bookingDataCamel = {
        itemId: item.id,
        startDate: isoNoMillis(startDateTime),
        endDate: isoNoMillis(endDateTime),
        bookingNotes: (formData.bookingNotes || '').slice(0, 500),
        depositAmount: Number(formData.depositAmount) || 0,
        requestedDurationDays: Math.max(1, Math.ceil((endDateTime.getTime() - startDateTime.getTime()) / (1000 * 60 * 60 * 24))),
        acceptTerms: formData.acceptTerms === true
      } as const;
      
  console.log('📝 Booking data to send (snake_case):', bookingDataSnake);
      
        const token = localStorage.getItem('token');
        if (!token) {
          alert('❌ Authentication token not found. Please log in again.');
          return;
        }
      
        // Try snake_case first (matches Mongo keys you've shared)
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            'X-User-Id': user?.id?.toString() || ''
          },
          body: JSON.stringify(bookingDataSnake)
        });
      
      console.log('📡 Booking response status:', response.status);
      
      if (response.ok) {
        const bookingResponse = await response.json();
        console.log('✅ Booking created successfully:', bookingResponse);
        alert(`✅ Booking request sent for "${item.name}"! You can track it in your dashboard.`);
        onBack();
      } else {
        let backendMessage = '';
        try {
          const maybeJson = await response.json();
          backendMessage = maybeJson.message || maybeJson.details || JSON.stringify(maybeJson);
        } catch {
          backendMessage = await response.text();
        }
        console.error('❌ Booking failed:', response.status, backendMessage);
        const statusMap: Record<number, string> = {
          400: 'Invalid booking data. Please verify dates (future & end after start), accept terms, and required fields.',
          401: 'Not authenticated. Please log in again.',
          403: 'Not authorized to create booking for this item.',
          404: 'Booking endpoint not found. Backend may not be running.',
          500: 'Server error. Please try again later.'
        };
        const errorMessage = statusMap[response.status] || `Unexpected error ${response.status}`;
        // Attempt fallback with camelCase if validation suggests unknown fields
        if (response.status === 400 && /unknown|invalid|start|end|date/i.test(backendMessage)) {
          console.warn('🔄 Retrying booking with camelCase payload fallback');
          const retry = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${token}`,
              'X-User-Id': user?.id?.toString() || ''
            },
            body: JSON.stringify(bookingDataCamel)
          });
          if (retry.ok) {
            const bookingResponse = await retry.json();
            console.log('✅ Booking created successfully on fallback:', bookingResponse);
            alert(`✅ Booking request (fallback) sent for "${item.name}"!`);
            onBack();
            return;
          } else {
            let retryMsg = await retry.text();
            alert(`❌ Failed booking (camelCase + fallback): ${errorMessage}\nOriginal: ${backendMessage}\nFallback: ${retryMsg}`);
            return;
          }
        }
        alert(`❌ Failed to create booking: ${errorMessage}\nDetails: ${backendMessage}`);
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
            Deposit Amount (Optional)
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
