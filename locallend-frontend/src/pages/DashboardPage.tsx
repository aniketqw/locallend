import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

interface DashboardPageProps {
  onBack: () => void;
  onAddItem?: () => void;
}

export const DashboardPage: React.FC<DashboardPageProps> = ({ onBack, onAddItem }) => {
  const { user } = useAuth();
  const [myItems, setMyItems] = useState<any[]>([]);
  const [myBookings, setMyBookings] = useState<any[]>([]);
  const [receivedBookings, setReceivedBookings] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setIsLoading(true);
        console.log('=== DASHBOARD DATA LOADING START ===');
        console.log('Current user:', user);
        console.log('Current user ID:', user?.id, 'Type:', typeof user?.id);
        console.log('JWT Token exists:', !!localStorage.getItem('token'));
        
        // Load items I'm lending (owned by me)
        const itemsResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/items?ownerId=${user?.id}`, {
          headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'X-User-Id': user?.id?.toString() || ''
          }
        });
        if (itemsResponse.ok) {
          const itemsData = await itemsResponse.json();
          console.log('Items API response:', itemsData);
          // Backend returns paginated data with 'content' array, not 'data' array
          const allItems = itemsData.content || itemsData.data || [];
          console.log('All items from API:', allItems);
          console.log('Query URL was:', `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/items?ownerId=${user?.id}`);
          
          // Filter items to only show items owned by current user (frontend filtering as backup)
          const myOwnedItems = allItems.filter((item: any) => {
            console.log(`Comparing item.ownerId (${item.ownerId}) === user.id (${user?.id}):`, item.ownerId === user?.id);
            return item.ownerId === user?.id || item.owner_id === user?.id;
          });
          console.log('Filtered myItems to:', myOwnedItems);
          setMyItems(myOwnedItems);
        } else {
          console.log('Items API failed:', itemsResponse.status);
        }

        // Try to load my bookings (items I've borrowed) - gracefully handle server errors
        try {
          console.log('Fetching my bookings for user:', user?.id);
          
          // Try multiple possible endpoints for my bookings
          let bookingsResponse;
          let endpointUsed = '';
          
          // Try endpoint 1: /api/bookings/my-bookings (confirmed working by backend)
          try {
            endpointUsed = '/api/bookings/my-bookings';
            bookingsResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/my-bookings`, {
              headers: { 
                'Content-Type': 'application/json',
                'X-User-Id': user?.id?.toString() || ''
              }
            });
            
            if (!bookingsResponse.ok && bookingsResponse.status === 404) {
              // Try endpoint 2: /api/bookings/user/{userId} (alternative pattern)
              endpointUsed = `/api/bookings/user/${user?.id}`;
              bookingsResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/user/${user?.id}`, {
                headers: { 
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${localStorage.getItem('token')}`,
                  'X-User-Id': user?.id?.toString() || ''
                }
              });
            }
            
            if (!bookingsResponse.ok && bookingsResponse.status === 404) {
              // Try endpoint 3: /api/bookings (with query param)
              endpointUsed = `/api/bookings?borrowerId=${user?.id}`;
              bookingsResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings?borrowerId=${user?.id}`, {
                headers: { 
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${localStorage.getItem('token')}`,
                  'X-User-Id': user?.id?.toString() || ''
                }
              });
            }
          } catch (fetchError) {
            console.log('Error trying booking endpoints:', fetchError);
            setMyBookings([]);
            return;
          }
          
          console.log('Bookings response status:', bookingsResponse.status, 'from endpoint:', endpointUsed);
          
          if (bookingsResponse.ok) {
            const bookingsData = await bookingsResponse.json();
            console.log('✅ BOOKINGS SUCCESS - API response:', bookingsData);
            console.log('Bookings response type:', typeof bookingsData, 'is array:', Array.isArray(bookingsData));
            // Handle both paginated (content) and direct array responses
            const bookings = bookingsData.content || bookingsData.data || bookingsData || [];
            console.log('Final bookings array:', bookings, 'length:', bookings.length);
            
            // Let's also try to fetch ALL bookings to see what exists in the system
            try {
              const allBookingsResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings`, {
                headers: { 
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${localStorage.getItem('token')}`,
                  'X-User-Id': user?.id?.toString() || ''
                }
              });
              if (allBookingsResponse.ok) {
                const allBookingsData = await allBookingsResponse.json();
                console.log('🔍 ALL BOOKINGS in system:', allBookingsData);
              }
            } catch (e) {
              console.log('Could not fetch all bookings for debugging');
            }
            
            setMyBookings(bookings);
          } else {
            const errorText = await bookingsResponse.text();
            console.log('All booking endpoints failed. Last error:', bookingsResponse.status, 'response:', errorText);
            setMyBookings([]);
          }
        } catch (error) {
          console.log('Bookings endpoint failed:', error);
          setMyBookings([]);
        }

        // Try to load bookings received (people wanting to borrow my items) - gracefully handle server errors
        try {
          console.log('Fetching received bookings for owner:', user?.id);
          
          // Try multiple possible endpoints for received bookings
          let receivedResponse;
          let ownerEndpointUsed = '';
          
          // Try endpoint 1: /api/bookings/my-owned (confirmed working by backend)
          try {
            ownerEndpointUsed = '/api/bookings/my-owned';
            receivedResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/my-owned`, {
              headers: { 
                'Content-Type': 'application/json',
                'X-User-Id': user?.id?.toString() || ''
              }
            });
            
            if (!receivedResponse.ok && receivedResponse.status === 404) {
              // Try endpoint 2: /api/bookings/my-owned (alternative from earlier conversation)
              ownerEndpointUsed = '/api/bookings/my-owned';
              receivedResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/my-owned`, {
                headers: { 
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${localStorage.getItem('token')}`,
                  'X-User-Id': user?.id?.toString() || ''
                }
              });
            }
            
            if (!receivedResponse.ok && receivedResponse.status === 404) {
              // Try endpoint 3: /api/bookings/owner/{userId}
              ownerEndpointUsed = `/api/bookings/owner/${user?.id}`;
              receivedResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/owner/${user?.id}`, {
                headers: { 
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${localStorage.getItem('token')}`,
                  'X-User-Id': user?.id?.toString() || ''
                }
              });
            }
            
            if (!receivedResponse.ok && receivedResponse.status === 404) {
              // Try endpoint 4: /api/bookings (with query param)
              ownerEndpointUsed = `/api/bookings?ownerId=${user?.id}`;
              receivedResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings?ownerId=${user?.id}`, {
                headers: { 
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${localStorage.getItem('token')}`,
                  'X-User-Id': user?.id?.toString() || ''
                }
              });
            }
          } catch (fetchError) {
            console.log('Error trying owner booking endpoints:', fetchError);
            setReceivedBookings([]);
            return;
          }
          
          console.log('Received bookings response status:', receivedResponse.status, 'from endpoint:', ownerEndpointUsed);
          
          if (receivedResponse.ok) {
            const receivedData = await receivedResponse.json();
            console.log('Received bookings API response:', receivedData);
            console.log('Received bookings response type:', typeof receivedData, 'is array:', Array.isArray(receivedData));
            // Handle both paginated (content) and direct array responses
            const bookings = receivedData.content || receivedData.data || receivedData || [];
            console.log('Setting receivedBookings to:', bookings, 'length:', bookings.length);
            setReceivedBookings(bookings);
          } else {
            const errorText = await receivedResponse.text();
            console.log('All owner booking endpoints failed. Last error:', receivedResponse.status, 'response:', errorText);
            setReceivedBookings([]);
          }
        } catch (error) {
          console.log('Received bookings endpoint failed:', error);
          setReceivedBookings([]);
        }
      } catch (error) {
        console.error('Error loading dashboard data:', error);
        // Set empty arrays if there's a general error
        setMyItems([]);
        setMyBookings([]);
        setReceivedBookings([]);
      } finally {
        setIsLoading(false);
      }
    };

    if (user) {
      loadDashboardData();
    }
  }, [user]);

  // Debug: Log current state values
  console.log('Dashboard render - myItems:', myItems, 'myBookings:', myBookings, 'receivedBookings:', receivedBookings);

  return (
    <div style={{ 
      fontFamily: 'Roboto, Arial, sans-serif', 
      margin: '0',
      padding: '0',
      backgroundColor: '#f5f5f5',
      color: '#213547',
      minHeight: '100vh'
    }}>
      {/* Navigation Bar */}
      <nav style={{
        backgroundColor: '#1976d2',
        color: 'white',
        padding: '12px 0',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        marginBottom: '20px'
      }}>
        <div style={{
          maxWidth: '1200px',
          margin: '0 auto',
          padding: '0 20px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>
            <button
              onClick={onBack}
              style={{ 
                color: 'white', 
                textDecoration: 'none', 
                background: 'none', 
                border: 'none', 
                cursor: 'pointer',
                fontSize: '1.5rem',
                fontWeight: 'bold'
              }}
            >
              LocalLend
            </button>
          </div>
          
          <span style={{ 
            color: 'white', 
            fontSize: '14px',
            padding: '8px 12px',
            backgroundColor: 'rgba(255, 255, 255, 0.1)',
            borderRadius: '4px'
          }}>
            Welcome, {user?.name}!
          </span>
        </div>
      </nav>

      {/* Dashboard Content */}
      <div style={{ padding: '0 20px', maxWidth: '1200px', margin: '0 auto' }}>
        <h1 style={{ marginBottom: '30px', color: '#1976d2' }}>Dashboard</h1>
        
        {isLoading ? (
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <p>Loading your dashboard...</p>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '20px' }}>
            
            {/* My Items Section */}
            <div style={{ 
              backgroundColor: 'white', 
              padding: '20px', 
              borderRadius: '8px', 
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)' 
            }}>
              <h2 style={{ color: '#1976d2', marginBottom: '15px' }}>My Items ({myItems.length})</h2>
              <p style={{ color: '#666', marginBottom: '15px', fontSize: '14px' }}>Items you're lending to others</p>
              
              {myItems.length === 0 ? (
                <p style={{ color: '#888', fontStyle: 'italic' }}>No items listed yet. Start lending!</p>
              ) : (
                <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                  {myItems.map((item) => (
                    <div key={item.id} style={{ 
                      border: '1px solid #e0e0e0', 
                      borderRadius: '4px', 
                      padding: '12px', 
                      marginBottom: '10px',
                      backgroundColor: '#fafafa'
                    }}>
                      <h4 style={{ margin: '0 0 5px 0', color: '#333' }}>{item.name || item.title}</h4>
                      <p style={{ margin: '0 0 8px 0', color: '#666', fontSize: '14px' }}>{item.description}</p>
                      <div style={{ display: 'flex', gap: '10px', fontSize: '12px' }}>
                        <span style={{ 
                          padding: '2px 8px', 
                          borderRadius: '12px', 
                          backgroundColor: item.status === 'AVAILABLE' ? '#4caf50' : '#ff9800',
                          color: 'white'
                        }}>
                          {item.status}
                        </span>
                        <span style={{ 
                          padding: '2px 8px', 
                          borderRadius: '12px', 
                          backgroundColor: '#2196f3',
                          color: 'white'
                        }}>
                          {item.condition}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* My Bookings Section */}
            <div style={{ 
              backgroundColor: 'white', 
              padding: '20px', 
              borderRadius: '8px', 
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)' 
            }}>
              <h2 style={{ color: '#1976d2', marginBottom: '15px' }}>My Bookings ({myBookings.length})</h2>
              <p style={{ color: '#666', marginBottom: '15px', fontSize: '14px' }}>Items you've borrowed from others</p>
              
              {myBookings.length === 0 ? (
                <p style={{ color: '#888', fontStyle: 'italic' }}>No active bookings. Browse items to borrow!</p>
              ) : (
                <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                  {myBookings.map((booking) => (
                    <div key={booking.id} style={{ 
                      border: '1px solid #e0e0e0', 
                      borderRadius: '4px', 
                      padding: '12px', 
                      marginBottom: '10px',
                      backgroundColor: '#fafafa'
                    }}>
                      <h4 style={{ margin: '0 0 5px 0', color: '#333' }}>{booking.itemTitle}</h4>
                      <p style={{ margin: '0 0 8px 0', color: '#666', fontSize: '14px' }}>
                        {booking.startDate} to {booking.endDate}
                      </p>
                      <span style={{ 
                        padding: '2px 8px', 
                        borderRadius: '12px', 
                        backgroundColor: booking.status === 'APPROVED' ? '#4caf50' : '#ff9800',
                        color: 'white',
                        fontSize: '12px'
                      }}>
                        {booking.status}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Booking Requests Section */}
            <div style={{ 
              backgroundColor: 'white', 
              padding: '20px', 
              borderRadius: '8px', 
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
              gridColumn: 'span 2'
            }}>
              <h2 style={{ color: '#1976d2', marginBottom: '15px' }}>Booking Requests ({receivedBookings.length})</h2>
              <p style={{ color: '#666', marginBottom: '15px', fontSize: '14px' }}>People wanting to borrow your items</p>
              
              {receivedBookings.length === 0 ? (
                <p style={{ color: '#888', fontStyle: 'italic' }}>No pending booking requests.</p>
              ) : (
                <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                  {receivedBookings.map((booking) => (
                    <div key={booking.id} style={{ 
                      border: '1px solid #e0e0e0', 
                      borderRadius: '4px', 
                      padding: '15px', 
                      marginBottom: '10px',
                      backgroundColor: '#fafafa',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                      <div>
                        <h4 style={{ margin: '0 0 5px 0', color: '#333' }}>{booking.itemTitle}</h4>
                        <p style={{ margin: '0 0 5px 0', color: '#666', fontSize: '14px' }}>
                          Requested by: {booking.borrowerName}
                        </p>
                        <p style={{ margin: '0', color: '#666', fontSize: '14px' }}>
                          From: {booking.startDate}
                        </p>
                      </div>
                      <div style={{ display: 'flex', gap: '10px' }}>
                        <button style={{
                          padding: '6px 12px',
                          backgroundColor: '#4caf50',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontSize: '14px'
                        }}>
                          Approve
                        </button>
                        <button style={{
                          padding: '6px 12px',
                          backgroundColor: '#f44336',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontSize: '14px'
                        }}>
                          Decline
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

          </div>
        )}
        
        {/* Quick Actions */}
        <div style={{ 
          marginTop: '30px',
          display: 'flex',
          gap: '15px',
          flexWrap: 'wrap'
        }}>
          <button 
            onClick={onAddItem}
            style={{
            padding: '12px 24px',
            backgroundColor: '#4caf50',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '16px',
            fontWeight: 'bold'
          }}>
            + Add New Item
          </button>
          
          <button style={{
            padding: '12px 24px',
            backgroundColor: '#2196f3',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '16px'
          }}>
            Browse Items
          </button>
          
          <button onClick={onBack} style={{
            padding: '12px 24px',
            backgroundColor: '#757575',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '16px'
          }}>
            Back to Home
          </button>
        </div>
      </div>
    </div>
  );
};
