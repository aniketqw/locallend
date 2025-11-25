import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { itemService } from '../services/itemService';

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
  const [deletingItemId, setDeletingItemId] = useState<string | null>(null);

  // Function to delete an item
  const handleDeleteItem = async (itemId: string, itemName: string) => {
    if (!user?.id) {
      alert('❌ User not found. Please log in again.');
      return;
    }

    const confirmDelete = window.confirm(
      `Are you sure you want to delete "${itemName}"?\n\nThis action cannot be undone. The item will be permanently removed from your listings.`
    );

    if (!confirmDelete) {
      return;
    }

    setDeletingItemId(itemId);
    try {
      console.log('🗑️ Deleting item:', itemId, 'with userId:', user.id);
      console.log('🗑️ DELETE request URL:', `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/items/${itemId}`);
      
      const deleteResult = await itemService.deleteItem(itemId, user.id);
      console.log('🗑️ Delete API response:', deleteResult);
      
      // Remove the item from the local state immediately
      setMyItems(prevItems => prevItems.filter(item => item.id !== itemId));
      
      alert(`✅ "${itemName}" has been successfully deleted.`);
      console.log('✅ Item deleted successfully');
    } catch (error: any) {
      console.error('❌ Delete item failed:', error);
      
      let errorMessage = 'Failed to delete item. Please try again.';
      if (error?.response?.status === 400) {
        errorMessage = 'Cannot delete item: It may have active bookings or be in use.';
      } else if (error?.response?.status === 403) {
        errorMessage = 'You are not authorized to delete this item.';
      } else if (error?.response?.status === 404) {
        errorMessage = 'Item not found. It may have already been deleted.';
      } else if (error?.response?.data?.message) {
        errorMessage = error.response.data.message;
      }
      
      alert(`❌ ${errorMessage}`);
    } finally {
      setDeletingItemId(null);
    }
  };

  // Function to toggle item status
  const handleToggleStatus = async (itemId: string, currentStatus: string) => {
    if (!user?.id) {
      alert('❌ User not found. Please log in again.');
      return;
    }

    // Validate current status and determine new status
    if (!currentStatus || !['AVAILABLE', 'UNAVAILABLE', 'BORROWED'].includes(currentStatus)) {
      alert('❌ Invalid current item status. Please refresh the page and try again.');
      return;
    }
    
    // Don't allow changing status if item is borrowed
    if (currentStatus === 'BORROWED') {
      alert('❌ Cannot change status of borrowed items. Wait for the item to be returned.');
      return;
    }
    
    const newStatus = currentStatus === 'AVAILABLE' ? 'UNAVAILABLE' : 'AVAILABLE';
    console.log('📝 Status change:', `${currentStatus} → ${newStatus}`);
    
    try {
      console.log('🔄 Updating item status:', {
        itemId,
        currentStatus,
        newStatus,
        userId: user.id
      });
      
      // First test backend connectivity and API routing
      try {
        const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
        console.log('🔍 Testing backend at:', baseUrl);
        
        // Test 1: Basic connectivity
        const healthCheck = await fetch(`${baseUrl}/api/items`, {
          method: 'GET'
        });
        console.log('🏥 Backend health check for /api/items:', healthCheck.status);
        
        // Test 2: Check if we're getting static resource errors
        if (healthCheck.status === 500) {
          const errorText = await healthCheck.text();
          if (errorText.includes('No static resource')) {
            alert(`❌ Backend API Routing Issue Detected!

The backend is treating API calls as static file requests.

This means:
1. The backend API server is not running properly
2. You may be connecting to a frontend/static server instead of the API server
3. The backend routes are not configured correctly

Current URL: ${baseUrl}
Expected: Spring Boot API server with /api/* endpoints

Please:
1. Check if your backend API server is running on port 8080
2. Verify you're not connecting to a frontend server
3. Check backend logs for proper API startup`);
            return;
          }
        }
        
        // Test 3: Try a different endpoint to confirm API availability
        if (!healthCheck.ok && healthCheck.status !== 401 && healthCheck.status !== 403) {
          // Try categories endpoint (public)
          const categoriesTest = await fetch(`${baseUrl}/api/categories`);
          console.log('🏥 Categories endpoint test:', categoriesTest.status);
          
          if (categoriesTest.status === 500) {
            const catErrorText = await categoriesTest.text();
            if (catErrorText.includes('No static resource')) {
              alert('❌ Backend API server is not running. Static resource errors detected on multiple endpoints.');
              return;
            }
          }
        }
        
      } catch (connectError) {
        console.error('🏥 Backend connection failed:', connectError);
        alert(`❌ Cannot connect to backend at ${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}.

Please ensure:
1. Backend is running
2. Correct port (8080)
3. No firewall blocking
4. CORS is configured

Error: ${(connectError as any)?.message || connectError}`);
        return;
      }
      
      // Test direct API call first to see exact error
      const token = localStorage.getItem('token');
      console.log('🔐 Token exists:', !!token);
      console.log('🔐 Token preview:', token ? token.substring(0, 20) + '...' : 'NO TOKEN');
      
      // Use toggle endpoint only (simpler and works)
      let directResponse;
      let endpointUsed = `/api/items/${itemId}/toggle-availability`;
      
      try {
        directResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}${endpointUsed}`, {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            'X-User-Id': user.id
          }
          // No body needed - toggle endpoint just flips current status
        });
      } catch (error) {
        throw new Error(`Failed to call toggle endpoint: ${error}`);
      }
      
      console.log('📡 Used endpoint:', endpointUsed);
      
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
        
        let errorMessage = 'Failed to update item status';
        
        // Special handling for static resource error (backend routing issue)
        if (directResponse.status === 500 && errorText.includes('No static resource')) {
          errorMessage = `❌ Backend API Error: The backend is treating API calls as static resources.
          
This indicates:
1. Backend API server is not properly running
2. Wrong backend URL or port
3. API endpoints not properly configured
4. Request being routed to static file server instead of API

Current backend URL: ${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}

Please check:
- Is the backend API server running on port 8080?
- Are you connecting to the right backend service?
- Is the backend properly configured for API routes?`;

        } else if (directResponse.status === 400) {
          errorMessage = 'Bad Request: ' + (errorJson?.message || errorText || 'Invalid status value or request format');
        } else if (directResponse.status === 401) {
          errorMessage = 'Unauthorized: Please log in again';
        } else if (directResponse.status === 403) {
          errorMessage = 'Forbidden: You are not the owner of this item';
        } else if (directResponse.status === 404) {
          errorMessage = 'Not Found: Item not found or endpoint not available';
        } else {
          errorMessage = `HTTP ${directResponse.status}: ${errorJson?.message || errorText || 'Server error'}`;
        }
        
        throw new Error(errorMessage);
      }
      
      const updatedItem = await directResponse.json();
      console.log('✅ Direct API success, updated item:', updatedItem);
      
      // Update the item in the local state
      setMyItems(prevItems => 
        prevItems.map(item => 
          item.id === itemId ? { ...item, status: updatedItem.status || newStatus } : item
        )
      );
      
      console.log('✅ Item status updated successfully to:', updatedItem.status || newStatus);
      alert(`✅ Item status changed to ${updatedItem.status || newStatus}`);
      
    } catch (error: any) {
      console.error('❌ Update status failed:', error);
      console.error('Full error object:', JSON.stringify(error, null, 2));
      
      let userFriendlyMessage = 'Failed to update item status. Please try again.';
      if (error.message) {
        userFriendlyMessage = error.message;
      }
      
      alert(`❌ ${userFriendlyMessage}\n\nCheck browser console for detailed logs.`);
    }
  };

  // Function to cancel a booking
  const handleCancelBooking = async (bookingId: string, itemName: string) => {
    if (!user?.id) {
      alert('❌ User not found. Please log in again.');
      return;
    }

    const confirmCancel = window.confirm(
      `Are you sure you want to cancel your booking for "${itemName}"?\n\nThis action cannot be undone.`
    );
    
    if (!confirmCancel) return;

    try {
      const token = localStorage.getItem('token');
      
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/${bookingId}/cancel`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'X-User-Id': user.id
        },
        body: JSON.stringify({
          reason: 'Cancelled by borrower'
        })
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Cancel booking error:', errorText);
        
        let errorMessage = 'Failed to cancel booking';
        if (response.status === 403) {
          errorMessage = 'You are not authorized to cancel this booking';
        } else if (response.status === 404) {
          errorMessage = 'Booking not found';
        } else if (response.status === 400) {
          errorMessage = 'Cannot cancel booking (may already be active or completed)';
        }
        
        throw new Error(errorMessage);
      }

      // Refresh bookings data
      await response.json();
      setMyBookings(prevBookings => 
        prevBookings.map(booking => 
          booking.id === bookingId ? { ...booking, status: 'CANCELLED' } : booking
        )
      );
      
      alert(`✅ Booking for "${itemName}" has been cancelled successfully`);
      
    } catch (error: any) {
      console.error('❌ Cancel booking failed:', error);
      alert(`❌ ${error.message || 'Failed to cancel booking. Please try again.'}`);
    }
  };

  // Function to view booking details
  const handleViewBookingDetails = (booking: any) => {
    const formatDate = (dateString: string) => {
      if (!dateString) return 'Not specified';
      try {
        return new Date(dateString).toLocaleDateString('en-US', {
          weekday: 'long',
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        });
      } catch {
        return dateString;
      }
    };

    const details = `
📦 BOOKING DETAILS

🏷️ Item: ${booking.itemName || booking.itemTitle || 'Unknown Item'}
👤 Owner: ${booking.ownerName || 'Unknown Owner'}
📋 Status: ${booking.status || 'Unknown'}

📅 RENTAL PERIOD
Start: ${formatDate(booking.startDate)}
End: ${formatDate(booking.endDate)}

💰 FINANCIAL
Deposit: ₹${booking.deposit || booking.itemDeposit || '0'}

📝 TIMELINE
Requested: ${formatDate(booking.createdAt)}
Booking ID: ${booking.id}

💬 NOTES
${booking.notes ? `Your Notes: "${booking.notes}"` : 'No notes from you'}
${booking.ownerNotes ? `Owner Notes: "${booking.ownerNotes}"` : 'No notes from owner'}

${booking.borrowerEmail ? `📧 Owner Contact: ${booking.ownerEmail || 'Not available'}` : ''}
    `.trim();

    alert(details);
  };

  // Function to start a booking
  const handleStartBooking = async (bookingId: string, itemName: string) => {
    if (!user?.id) {
      alert('❌ User not found. Please log in again.');
      return;
    }

    const confirmStart = window.confirm(
      `Start your booking for "${itemName}"?\n\nThis will mark the item as actively borrowed.`
    );
    
    if (!confirmStart) return;

    try {
      const token = localStorage.getItem('token');
      
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/${bookingId}/start`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'X-User-Id': user.id
        }
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Start booking error:', errorText);
        
        let errorMessage = 'Failed to start booking';
        if (response.status === 403) {
          errorMessage = 'You are not authorized to start this booking';
        } else if (response.status === 404) {
          errorMessage = 'Booking not found';
        } else if (response.status === 400) {
          errorMessage = 'Cannot start booking (may not be confirmed yet)';
        }
        
        throw new Error(errorMessage);
      }

      // Update booking status
      await response.json();
      setMyBookings(prevBookings => 
        prevBookings.map(booking => 
          booking.id === bookingId ? { ...booking, status: 'ACTIVE' } : booking
        )
      );
      
      alert(`✅ Booking for "${itemName}" is now active. Enjoy your rental!`);
      
    } catch (error: any) {
      console.error('❌ Start booking failed:', error);
      alert(`❌ ${error.message || 'Failed to start booking. Please try again.'}`);
    }
  };

  // Function to complete a booking
  const handleCompleteBooking = async (bookingId: string, itemName: string) => {
    if (!user?.id) {
      alert('❌ User not found. Please log in again.');
      return;
    }

    const confirmComplete = window.confirm(
      `Mark your booking for "${itemName}" as completed?\n\nThis indicates you have returned the item to the owner.`
    );
    
    if (!confirmComplete) return;

    try {
      const token = localStorage.getItem('token');
      
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/${bookingId}/complete`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'X-User-Id': user.id
        }
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Complete booking error:', errorText);
        
        let errorMessage = 'Failed to complete booking';
        if (response.status === 403) {
          errorMessage = 'You are not authorized to complete this booking';
        } else if (response.status === 404) {
          errorMessage = 'Booking not found';
        } else if (response.status === 400) {
          errorMessage = 'Cannot complete booking (may not be active)';
        }
        
        throw new Error(errorMessage);
      }

      // Update booking status
      await response.json();
      setMyBookings(prevBookings => 
        prevBookings.map(booking => 
          booking.id === bookingId ? { ...booking, status: 'COMPLETED' } : booking
        )
      );
      
      alert(`✅ Booking for "${itemName}" has been marked as completed. Thank you for using LocalLend!`);
      
    } catch (error: any) {
      console.error('❌ Complete booking failed:', error);
      alert(`❌ ${error.message || 'Failed to complete booking. Please try again.'}`);
    }
  };

  // Function to approve a booking request
  const handleApproveBookingRequest = async (bookingId: string, itemName: string, borrowerName: string) => {
    if (!user?.id) {
      alert('❌ User not found. Please log in again.');
      return;
    }

    const ownerNotes = prompt(
      `Approve booking request for "${itemName}" by ${borrowerName}?\n\nAdd any notes for the borrower (optional):`,
      "Please take good care of the item. Contact me if you have any questions."
    );
    
    if (ownerNotes === null) return; // User cancelled

    try {
      const token = localStorage.getItem('token');
      
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/${bookingId}/approve`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'X-User-Id': user.id
        },
        body: JSON.stringify({
          ownerNotes: ownerNotes.trim() || undefined
        })
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Approve booking error:', errorText);
        
        let errorMessage = 'Failed to approve booking request';
        if (response.status === 403) {
          errorMessage = 'You are not authorized to approve this booking';
        } else if (response.status === 404) {
          errorMessage = 'Booking request not found';
        } else if (response.status === 400) {
          errorMessage = 'Cannot approve this booking (may already be processed)';
        }
        
        throw new Error(errorMessage);
      }

      // Update booking status in received bookings
      await response.json();
      setReceivedBookings(prevBookings => 
        prevBookings.map(booking => 
          booking.id === bookingId ? { ...booking, status: 'CONFIRMED', ownerNotes: ownerNotes.trim() } : booking
        )
      );
      
      alert(`✅ Booking request for "${itemName}" by ${borrowerName} has been approved!`);
      
    } catch (error: any) {
      console.error('❌ Approve booking failed:', error);
      alert(`❌ ${error.message || 'Failed to approve booking request. Please try again.'}`);
    }
  };

  // Function to decline a booking request
  const handleDeclineBookingRequest = async (bookingId: string, itemName: string, borrowerName: string) => {
    if (!user?.id) {
      alert('❌ User not found. Please log in again.');
      return;
    }

    const reason = prompt(
      `Decline booking request for "${itemName}" by ${borrowerName}?\n\nReason for declining (optional):`,
      "Sorry, the item is not available for the requested dates."
    );
    
    if (reason === null) return; // User cancelled

    const confirmDecline = window.confirm(
      `Are you sure you want to decline the booking request for "${itemName}" by ${borrowerName}?\n\nThis action cannot be undone.`
    );
    
    if (!confirmDecline) return;

    try {
      const token = localStorage.getItem('token');
      
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/${bookingId}/reject`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'X-User-Id': user.id
        },
        body: JSON.stringify({
          reason: reason?.trim() || undefined
        })
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Decline booking error:', errorText);
        
        let errorMessage = 'Failed to decline booking request';
        if (response.status === 403) {
          errorMessage = 'You are not authorized to decline this booking';
        } else if (response.status === 404) {
          errorMessage = 'Booking request not found';
        } else if (response.status === 400) {
          errorMessage = 'Cannot decline this booking (may already be processed)';
        }
        
        throw new Error(errorMessage);
      }

      // Update booking status in received bookings
      await response.json();
      setReceivedBookings(prevBookings => 
        prevBookings.map(booking => 
          booking.id === bookingId ? { ...booking, status: 'REJECTED', rejectionReason: reason?.trim() } : booking
        )
      );
      
      alert(`✅ Booking request for "${itemName}" by ${borrowerName} has been declined.`);
      
    } catch (error: any) {
      console.error('❌ Decline booking failed:', error);
      alert(`❌ ${error.message || 'Failed to decline booking request. Please try again.'}`);
    }
  };

  // Function to view full booking request details
  const handleViewBookingRequestDetails = (booking: any) => {
    const formatDate = (dateString: string) => {
      if (!dateString) return 'Not specified';
      try {
        return new Date(dateString).toLocaleDateString('en-US', {
          weekday: 'long',
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        });
      } catch {
        return dateString;
      }
    };

    const calculateDuration = () => {
      if (!booking.startDate || !booking.endDate) return 'Unknown duration';
      try {
        const start = new Date(booking.startDate);
        const end = new Date(booking.endDate);
        const diffTime = Math.abs(end.getTime() - start.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return `${diffDays} day${diffDays !== 1 ? 's' : ''}`;
      } catch {
        return 'Unknown duration';
      }
    };

    const details = `
📦 BOOKING REQUEST DETAILS

🏷️ Item: ${booking.itemName || booking.itemTitle || 'Unknown Item'}
👤 Borrower: ${booking.borrowerName || 'Unknown Borrower'}
📧 Contact: ${booking.borrowerEmail || 'Not provided'}
📋 Status: ${booking.status || 'Unknown'}

📅 RENTAL PERIOD
Start: ${formatDate(booking.startDate)}
End: ${formatDate(booking.endDate)}
Duration: ${calculateDuration()}

💰 FINANCIAL DETAILS
Deposit Required: ₹${booking.deposit || booking.itemDeposit || '0'}
${booking.itemValue ? `Item Value: $${booking.itemValue}` : ''}

📝 TIMELINE
Request Date: ${formatDate(booking.createdAt)}
Booking ID: ${booking.id}

⭐ BORROWER INFORMATION
${booking.borrowerRating ? `Rating: ${booking.borrowerRating.toFixed(1)}/5 stars` : 'Rating: Not available'}
${booking.borrowerCompletedBookings ? `Completed Bookings: ${booking.borrowerCompletedBookings}` : ''}

💬 BORROWER'S MESSAGE
${booking.notes || booking.borrowerNotes || 'No message provided'}

${booking.ownerNotes ? `📋 YOUR NOTES\n"${booking.ownerNotes}"` : ''}
${booking.rejectionReason ? `❌ REJECTION REASON\n"${booking.rejectionReason}"` : ''}
    `.trim();

    alert(details);
  };

  // Function to contact borrower
  const handleContactBorrower = (booking: any) => {
    if (!booking.borrowerEmail) {
      alert('❌ No contact information available for this borrower.');
      return;
    }

    const subject = encodeURIComponent(`LocalLend: Regarding your booking for ${booking.itemName || booking.itemTitle || 'item'}`);
    const body = encodeURIComponent(`Hi ${booking.borrowerName || 'there'},

I'm contacting you regarding your booking request for "${booking.itemName || booking.itemTitle || 'item'}".

Booking Details:
- Start Date: ${booking.startDate}
- End Date: ${booking.endDate}
- Status: ${booking.status}

Best regards,
${user?.name || 'Item Owner'}

---
This message was sent through LocalLend platform.`);

    window.open(`mailto:${booking.borrowerEmail}?subject=${subject}&body=${body}`);
  };

  // Function to send message to borrower
  const handleSendMessage = (booking: any) => {
    if (!booking.borrowerEmail) {
      alert('❌ No contact information available for this borrower.');
      return;
    }

    const message = prompt(
      `Send a message to ${booking.borrowerName || 'borrower'} about "${booking.itemName || booking.itemTitle}":\n\n(This will open your email client)`,
      `Hi ${booking.borrowerName || 'there'},\n\nRegarding your booking for "${booking.itemName || booking.itemTitle}"...\n\nBest regards,\n${user?.name || 'Item Owner'}`
    );

    if (message === null) return; // User cancelled

    const subject = encodeURIComponent(`LocalLend: ${booking.itemName || booking.itemTitle || 'Booking'}`);
    const body = encodeURIComponent(message);

    window.open(`mailto:${booking.borrowerEmail}?subject=${subject}&body=${body}`);
  };

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
          
          // Use only confirmed working endpoint from integration guide
          let bookingsResponse;
          let endpointUsed = '/api/bookings/my-bookings';
          
          try {
            bookingsResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/my-bookings`, {
              headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'X-User-Id': user?.id?.toString() || ''
              }
            });
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
            
            // Note: Removed debugging call to /api/bookings (405 Method Not Allowed)
            // The /api/bookings endpoint only supports POST for creating bookings, not GET
            console.log('� Successfully loaded user bookings:', bookings.length, 'items');
            
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
          
          // Use only confirmed working endpoint from integration guide
          let receivedResponse;
          let ownerEndpointUsed = '/api/bookings/my-owned';
          
          try {
            receivedResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/bookings/my-owned`, {
              headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'X-User-Id': user?.id?.toString() || ''
              }
            });
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
        backgroundColor: '#ff9800',
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
        <h1 style={{ marginBottom: '30px', color: '#ff9800' }}>Dashboard</h1>
        
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
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                <h2 style={{ color: '#ff9800', margin: 0 }}>My Items ({myItems.length})</h2>
                {onAddItem && (
                  <button
                    onClick={onAddItem}
                    style={{
                      padding: '8px 16px',
                      backgroundColor: '#ff9800',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontSize: '14px',
                      fontWeight: 'bold'
                    }}
                  >
                    + Add Item
                  </button>
                )}
              </div>
              <p style={{ color: '#666', marginBottom: '15px', fontSize: '14px' }}>Items you're lending to others</p>
              
              {myItems.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px 20px' }}>
                  <p style={{ color: '#888', fontStyle: 'italic', marginBottom: '15px' }}>No items listed yet. Start lending!</p>
                  {onAddItem && (
                    <button
                      onClick={onAddItem}
                      style={{
                        padding: '12px 24px',
                        backgroundColor: '#ff9800',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        fontSize: '16px',
                        fontWeight: 'bold'
                      }}
                    >
                      List Your First Item
                    </button>
                  )}
                </div>
              ) : (
                <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                  {myItems.map((item) => (
                    <div key={item.id} style={{ 
                      border: '1px solid #e0e0e0', 
                      borderRadius: '8px', 
                      padding: '16px', 
                      marginBottom: '12px',
                      backgroundColor: '#fafafa',
                      position: 'relative'
                    }}>
                      <div style={{ display: 'flex', gap: '12px' }}>
                        {/* Item Image */}
                        {item.images && item.images[0] && (
                          <img
                            src={item.images[0]}
                            alt={item.name}
                            style={{
                              width: '80px',
                              height: '80px',
                              objectFit: 'cover',
                              borderRadius: '6px',
                              flexShrink: 0
                            }}
                          />
                        )}
                        
                        {/* Item Details */}
                        <div style={{ flex: 1, minWidth: 0 }}>
                          <h4 style={{ margin: '0 0 6px 0', color: '#333', fontSize: '16px', fontWeight: 'bold' }}>
                            {item.name || item.title}
                          </h4>
                          
                          <p style={{ 
                            margin: '0 0 8px 0', 
                            color: '#666', 
                            fontSize: '14px', 
                            lineHeight: '1.4',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            display: '-webkit-box',
                            WebkitLineClamp: 2,
                            WebkitBoxOrient: 'vertical'
                          }}>
                            {item.description}
                          </p>
                          
                          {/* Item Metadata */}
                          <div style={{ marginBottom: '10px' }}>
                            {item.categoryName && (
                              <div style={{ fontSize: '12px', color: '#888', marginBottom: '4px' }}>
                                📂 Category: <strong>{item.categoryName}</strong>
                              </div>
                            )}
                            
                            {item.deposit > 0 && (
                              <div style={{ fontSize: '12px', color: '#888', marginBottom: '4px' }}>
                                💰 Deposit: <strong>₹${item.deposit}</strong>
                              </div>
                            )}
                            
                            {item.averageRating > 0 && (
                              <div style={{ fontSize: '12px', color: '#888', marginBottom: '4px' }}>
                                ⭐ Rating: <strong>{item.averageRating.toFixed(1)}/5</strong>
                              </div>
                            )}
                            
                            {item.createdAt && (
                              <div style={{ fontSize: '12px', color: '#888' }}>
                                📅 Listed: <strong>{new Date(item.createdAt).toLocaleDateString()}</strong>
                              </div>
                            )}
                          </div>
                          
                          {/* Status and Condition Badges */}
                          <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
                            <span style={{ 
                              padding: '4px 10px', 
                              borderRadius: '12px', 
                              fontSize: '12px',
                              fontWeight: 'bold',
                              backgroundColor: item.status === 'AVAILABLE' ? '#4caf50' : item.status === 'BORROWED' ? '#ff5722' : '#ff9800',
                              color: 'white'
                            }}>
                              {item.status || 'UNKNOWN'}
                            </span>
                            <span style={{ 
                              padding: '4px 10px', 
                              borderRadius: '12px', 
                              fontSize: '12px',
                              fontWeight: 'bold',
                              backgroundColor: '#2196f3',
                              color: 'white'
                            }}>
                              {item.condition || 'N/A'}
                            </span>
                            {item.canBeBorrowed === false && (
                              <span style={{ 
                                padding: '4px 10px', 
                                borderRadius: '12px', 
                                fontSize: '12px',
                                fontWeight: 'bold',
                                backgroundColor: '#9e9e9e',
                                color: 'white'
                              }}>
                                Not Borrowable
                              </span>
                            )}
                          </div>
                          
                          {/* Action Buttons */}
                          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                            <button
                              onClick={() => handleToggleStatus(item.id, item.status)}
                              style={{
                                padding: '6px 12px',
                                fontSize: '12px',
                                border: '1px solid #ff9800',
                                backgroundColor: 'transparent',
                                color: '#ff9800',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontWeight: 'bold'
                              }}
                              title={`Change status to ${item.status === 'AVAILABLE' ? 'UNAVAILABLE' : 'AVAILABLE'}`}
                            >
                              {item.status === 'AVAILABLE' ? '⏸️ Make Unavailable' : '▶️ Make Available'}
                            </button>
                            
                            <button
                              onClick={() => handleDeleteItem(item.id, item.name)}
                              disabled={deletingItemId === item.id}
                              style={{
                                padding: '6px 12px',
                                fontSize: '12px',
                                border: '1px solid #f44336',
                                backgroundColor: deletingItemId === item.id ? '#ffcdd2' : 'transparent',
                                color: deletingItemId === item.id ? '#666' : '#f44336',
                                borderRadius: '4px',
                                cursor: deletingItemId === item.id ? 'not-allowed' : 'pointer',
                                fontWeight: 'bold'
                              }}
                              title="Delete this item permanently"
                            >
                              {deletingItemId === item.id ? '🗑️ Deleting...' : '🗑️ Delete'}
                            </button>
                          </div>
                        </div>
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
              <h2 style={{ color: '#ff9800', marginBottom: '15px' }}>My Bookings ({myBookings.length})</h2>
              <p style={{ color: '#666', marginBottom: '15px', fontSize: '14px' }}>Items you've borrowed or are borrowing from others</p>
              
              {myBookings.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                  <p style={{ fontSize: '48px', margin: '0 0 10px 0' }}>📋</p>
                  <p style={{ fontStyle: 'italic', fontSize: '16px' }}>No bookings yet</p>
                  <p style={{ fontSize: '14px', margin: '10px 0 0 0' }}>Browse items to make your first booking!</p>
                </div>
              ) : (
                <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                  {myBookings.map((booking) => {
                    const getStatusColor = (status?: string) => {
                      switch(status?.toUpperCase()) {
                        case 'PENDING': return '#ff9800';
                        case 'CONFIRMED': case 'APPROVED': return '#4caf50';
                        case 'ACTIVE': return '#2196f3';
                        case 'COMPLETED': return '#9c27b0';
                        case 'CANCELLED': case 'REJECTED': return '#f44336';
                        default: return '#757575';
                      }
                    };

                    const getStatusIcon = (status?: string) => {
                      switch(status?.toUpperCase()) {
                        case 'PENDING': return '⏳';
                        case 'CONFIRMED': case 'APPROVED': return '✅';
                        case 'ACTIVE': return '📦';
                        case 'COMPLETED': return '✨';
                        case 'CANCELLED': case 'REJECTED': return '❌';
                        default: return '📋';
                      }
                    };

                    return (
                      <div key={booking.id} style={{ 
                        border: '1px solid #e0e0e0', 
                        borderRadius: '8px', 
                        padding: '16px', 
                        marginBottom: '12px',
                        backgroundColor: '#fafafa',
                        position: 'relative'
                      }}>
                        {/* Header with Item and Status */}
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                          <div style={{ flex: 1 }}>
                            <h4 style={{ margin: '0 0 6px 0', color: '#333', fontSize: '16px', fontWeight: 'bold' }}>
                              {booking.itemName || booking.itemTitle || 'Unknown Item'}
                            </h4>
                            <div style={{ fontSize: '13px', color: '#666', marginBottom: '4px' }}>
                              👤 Owner: <strong>{booking.ownerName || 'Unknown Owner'}</strong>
                            </div>
                          </div>
                          <span style={{ 
                            padding: '6px 12px', 
                            borderRadius: '16px', 
                            backgroundColor: getStatusColor(booking.status),
                            color: 'white',
                            fontSize: '12px',
                            fontWeight: 'bold',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px'
                          }}>
                            {getStatusIcon(booking.status)} {booking.status || 'UNKNOWN'}
                          </span>
                        </div>

                        {/* Booking Details */}
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px', marginBottom: '12px' }}>
                          {/* Dates */}
                          <div>
                            <div style={{ fontSize: '12px', color: '#888', marginBottom: '4px' }}>📅 Booking Period</div>
                            <div style={{ fontSize: '14px', color: '#333', fontWeight: '500' }}>
                              {booking.startDate && booking.endDate ? (
                                <>
                                  <div>From: {new Date(booking.startDate).toLocaleDateString()}</div>
                                  <div>To: {new Date(booking.endDate).toLocaleDateString()}</div>
                                </>
                              ) : (
                                <div>{booking.startDate} - {booking.endDate}</div>
                              )}
                            </div>
                          </div>

                          {/* Deposit Info */}
                          {(booking.deposit || booking.itemDeposit) && (
                            <div>
                              <div style={{ fontSize: '12px', color: '#888', marginBottom: '4px' }}>💰 Deposit</div>
                              <div style={{ fontSize: '14px', color: '#333', fontWeight: '500' }}>
                                ₹${booking.deposit || booking.itemDeposit || 0}
                              </div>
                            </div>
                          )}

                          {/* Booking Date */}
                          {booking.createdAt && (
                            <div>
                              <div style={{ fontSize: '12px', color: '#888', marginBottom: '4px' }}>📝 Requested</div>
                              <div style={{ fontSize: '14px', color: '#333', fontWeight: '500' }}>
                                {new Date(booking.createdAt).toLocaleDateString()}
                              </div>
                            </div>
                          )}
                        </div>

                        {/* Notes */}
                        {(booking.notes || booking.borrowerNotes || booking.ownerNotes) && (
                          <div style={{ marginBottom: '12px' }}>
                            {booking.notes && (
                              <div style={{ marginBottom: '6px' }}>
                                <div style={{ fontSize: '12px', color: '#888', marginBottom: '2px' }}>💬 Your Notes</div>
                                <div style={{ 
                                  fontSize: '13px', 
                                  color: '#555', 
                                  backgroundColor: '#f0f7ff', 
                                  padding: '8px', 
                                  borderRadius: '6px',
                                  border: '1px solid #e3f2fd'
                                }}>
                                  "{booking.notes}"
                                </div>
                              </div>
                            )}
                            {booking.ownerNotes && (
                              <div>
                                <div style={{ fontSize: '12px', color: '#888', marginBottom: '2px' }}>📋 Owner Notes</div>
                                <div style={{ 
                                  fontSize: '13px', 
                                  color: '#555', 
                                  backgroundColor: '#fff3e0', 
                                  padding: '8px', 
                                  borderRadius: '6px',
                                  border: '1px solid #ffe0b2'
                                }}>
                                  "{booking.ownerNotes}"
                                </div>
                              </div>
                            )}
                          </div>
                        )}

                        {/* Action Buttons Based on Status */}
                        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                          {booking.status === 'CONFIRMED' && (
                            <button 
                              onClick={() => handleStartBooking(booking.id, booking.itemName || booking.itemTitle || 'Unknown Item')}
                              style={{
                                padding: '8px 16px',
                                backgroundColor: '#2196f3',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '13px',
                                fontWeight: '500'
                              }}>
                              🚀 Start Booking
                            </button>
                          )}
                          {booking.status === 'ACTIVE' && (
                            <button 
                              onClick={() => handleCompleteBooking(booking.id, booking.itemName || booking.itemTitle || 'Unknown Item')}
                              style={{
                                padding: '8px 16px',
                                backgroundColor: '#9c27b0',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '13px',
                                fontWeight: '500'
                              }}>
                              ✅ Complete Booking
                            </button>
                          )}
                          {(booking.status === 'PENDING' || booking.status === 'CONFIRMED') && (
                            <button 
                              onClick={() => handleCancelBooking(booking.id, booking.itemName || booking.itemTitle || 'Unknown Item')}
                              style={{
                                padding: '8px 16px',
                                backgroundColor: '#f44336',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '13px',
                                fontWeight: '500'
                              }}>
                              ❌ Cancel
                            </button>
                          )}
                          <button 
                            onClick={() => handleViewBookingDetails(booking)}
                            style={{
                              padding: '8px 16px',
                              backgroundColor: 'white',
                              color: '#ff9800',
                              border: '1px solid #ff9800',
                              borderRadius: '6px',
                              cursor: 'pointer',
                              fontSize: '13px',
                              fontWeight: '500'
                            }}>
                            📄 View Details
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Received Booking Requests Section */}
            <div style={{ 
              backgroundColor: 'white', 
              padding: '20px', 
              borderRadius: '8px', 
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
              gridColumn: 'span 2'
            }}>
              <h2 style={{ color: '#ff9800', marginBottom: '15px' }}>Booking Requests ({receivedBookings.length})</h2>
              <p style={{ color: '#666', marginBottom: '15px', fontSize: '14px' }}>People wanting to borrow your items - Manage approvals and track rentals</p>
              
              {receivedBookings.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                  <p style={{ fontSize: '48px', margin: '0 0 10px 0' }}>📬</p>
                  <p style={{ fontStyle: 'italic', fontSize: '16px' }}>No booking requests yet</p>
                  <p style={{ fontSize: '14px', margin: '10px 0 0 0' }}>Your items will appear here when people want to borrow them!</p>
                </div>
              ) : (
                <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
                  {receivedBookings.map((booking) => {
                    const getStatusColor = (status?: string) => {
                      switch(status?.toUpperCase()) {
                        case 'PENDING': return '#ff9800';
                        case 'CONFIRMED': case 'APPROVED': return '#4caf50';
                        case 'ACTIVE': return '#2196f3';
                        case 'COMPLETED': return '#9c27b0';
                        case 'CANCELLED': case 'REJECTED': return '#f44336';
                        default: return '#757575';
                      }
                    };

                    const getStatusIcon = (status?: string) => {
                      switch(status?.toUpperCase()) {
                        case 'PENDING': return '⏳';
                        case 'CONFIRMED': case 'APPROVED': return '✅';
                        case 'ACTIVE': return '📦';
                        case 'COMPLETED': return '✨';
                        case 'CANCELLED': case 'REJECTED': return '❌';
                        default: return '📋';
                      }
                    };

                    const isPending = booking.status?.toUpperCase() === 'PENDING';
                    const isActive = booking.status?.toUpperCase() === 'ACTIVE';

                    return (
                      <div key={booking.id} style={{ 
                        border: isPending ? '2px solid #ff9800' : '1px solid #e0e0e0', 
                        borderRadius: '12px', 
                        padding: '20px', 
                        marginBottom: '16px',
                        backgroundColor: isPending ? '#fff8e1' : '#fafafa',
                        position: 'relative'
                      }}>
                        {/* Urgent Indicator for Pending */}
                        {isPending && (
                          <div style={{
                            position: 'absolute',
                            top: '12px',
                            right: '12px',
                            backgroundColor: '#ff9800',
                            color: 'white',
                            padding: '4px 8px',
                            borderRadius: '12px',
                            fontSize: '11px',
                            fontWeight: 'bold'
                          }}>
                            🚨 NEEDS APPROVAL
                          </div>
                        )}

                        {/* Header with Item and Borrower */}
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                          <div style={{ flex: 1, paddingRight: isPending ? '120px' : '0' }}>
                            <h4 style={{ margin: '0 0 8px 0', color: '#333', fontSize: '18px', fontWeight: 'bold' }}>
                              📦 {booking.itemName || booking.itemTitle || 'Unknown Item'}
                            </h4>
                            <div style={{ fontSize: '14px', color: '#666', marginBottom: '6px' }}>
                              👤 Requested by: <strong style={{ color: '#333' }}>{booking.borrowerName || 'Unknown Borrower'}</strong>
                            </div>
                            {booking.borrowerEmail && (
                              <div style={{ fontSize: '13px', color: '#666', marginBottom: '6px' }}>
                                ✉️ Contact: <span style={{ color: '#ff9800' }}>{booking.borrowerEmail}</span>
                              </div>
                            )}
                          </div>
                          <span style={{ 
                            padding: '8px 14px', 
                            borderRadius: '20px', 
                            backgroundColor: getStatusColor(booking.status),
                            color: 'white',
                            fontSize: '13px',
                            fontWeight: 'bold',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px'
                          }}>
                            {getStatusIcon(booking.status)} {booking.status || 'UNKNOWN'}
                          </span>
                        </div>

                        {/* Booking Details Grid */}
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px', marginBottom: '16px' }}>
                          {/* Rental Period */}
                          <div>
                            <div style={{ fontSize: '12px', color: '#888', marginBottom: '6px', fontWeight: '600' }}>📅 RENTAL PERIOD</div>
                            <div style={{ fontSize: '14px', color: '#333' }}>
                              {booking.startDate && booking.endDate ? (
                                <>
                                  <div style={{ fontWeight: '500' }}>Start: {new Date(booking.startDate).toLocaleDateString()}</div>
                                  <div style={{ fontWeight: '500' }}>End: {new Date(booking.endDate).toLocaleDateString()}</div>
                                  <div style={{ fontSize: '12px', color: '#666', marginTop: '2px' }}>
                                    ({Math.ceil((new Date(booking.endDate).getTime() - new Date(booking.startDate).getTime()) / (1000 * 3600 * 24))} days)
                                  </div>
                                </>
                              ) : (
                                <div>{booking.startDate} - {booking.endDate}</div>
                              )}
                            </div>
                          </div>

                          {/* Deposit & Financial */}
                          <div>
                            <div style={{ fontSize: '12px', color: '#888', marginBottom: '6px', fontWeight: '600' }}>💰 FINANCIAL</div>
                            <div style={{ fontSize: '14px', color: '#333' }}>
                              {(booking.deposit || booking.itemDeposit) ? (
                                <div style={{ fontWeight: '500', color: '#4caf50' }}>Deposit: ₹${booking.deposit || booking.itemDeposit}</div>
                              ) : (
                                <div style={{ color: '#666' }}>No deposit required</div>
                              )}
                            </div>
                          </div>

                          {/* Request Date */}
                          <div>
                            <div style={{ fontSize: '12px', color: '#888', marginBottom: '6px', fontWeight: '600' }}>📝 REQUESTED</div>
                            <div style={{ fontSize: '14px', color: '#333', fontWeight: '500' }}>
                              {booking.createdAt ? new Date(booking.createdAt).toLocaleDateString() : 'Unknown'}
                            </div>
                          </div>

                          {/* Borrower Rating */}
                          {booking.borrowerRating && (
                            <div>
                              <div style={{ fontSize: '12px', color: '#888', marginBottom: '6px', fontWeight: '600' }}>⭐ BORROWER RATING</div>
                              <div style={{ fontSize: '14px', color: '#333', fontWeight: '500' }}>
                                {booking.borrowerRating.toFixed(1)}/5 ⭐
                              </div>
                            </div>
                          )}
                        </div>

                        {/* Notes Section */}
                        {(booking.notes || booking.borrowerNotes) && (
                          <div style={{ marginBottom: '16px' }}>
                            <div style={{ fontSize: '12px', color: '#888', marginBottom: '6px', fontWeight: '600' }}>💬 BORROWER'S MESSAGE</div>
                            <div style={{ 
                              fontSize: '14px', 
                              color: '#555', 
                              backgroundColor: 'white', 
                              padding: '12px', 
                              borderRadius: '8px',
                              border: '1px solid #e0e0e0',
                              fontStyle: 'italic'
                            }}>
                              "{booking.notes || booking.borrowerNotes || 'No message provided.'}"
                            </div>
                          </div>
                        )}

                        {/* Action Buttons */}
                        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                          {isPending && (
                            <>
                              <button 
                                onClick={() => handleApproveBookingRequest(
                                  booking.id, 
                                  booking.itemName || booking.itemTitle || 'Unknown Item',
                                  booking.borrowerName || 'Unknown Borrower'
                                )}
                                style={{
                                  padding: '10px 20px',
                                  backgroundColor: '#4caf50',
                                  color: 'white',
                                  border: 'none',
                                  borderRadius: '8px',
                                  cursor: 'pointer',
                                  fontSize: '14px',
                                  fontWeight: '600',
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: '6px'
                                }}>
                                ✅ Approve Request
                              </button>
                              <button 
                                onClick={() => handleDeclineBookingRequest(
                                  booking.id,
                                  booking.itemName || booking.itemTitle || 'Unknown Item',
                                  booking.borrowerName || 'Unknown Borrower'
                                )}
                                style={{
                                  padding: '10px 20px',
                                  backgroundColor: '#f44336',
                                  color: 'white',
                                  border: 'none',
                                  borderRadius: '8px',
                                  cursor: 'pointer',
                                  fontSize: '14px',
                                  fontWeight: '600',
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: '6px'
                                }}>
                                ❌ Decline Request
                              </button>
                            </>
                          )}
                          
                          {isActive && (
                            <button 
                              onClick={() => handleContactBorrower(booking)}
                              style={{
                                padding: '10px 20px',
                                backgroundColor: '#2196f3',
                                color: 'white',
                                border: 'none',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '600',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '6px'
                              }}>
                              📞 Contact Borrower
                            </button>
                          )}

                          <button 
                            onClick={() => handleViewBookingRequestDetails(booking)}
                            style={{
                              padding: '10px 20px',
                              backgroundColor: 'white',
                              color: '#ff9800',
                              border: '2px solid #ff9800',
                              borderRadius: '8px',
                              cursor: 'pointer',
                              fontSize: '14px',
                              fontWeight: '600',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '6px'
                            }}>
                            📄 View Full Details
                          </button>

                          {booking.borrowerEmail && (
                            <button 
                              onClick={() => handleSendMessage(booking)}
                              style={{
                                padding: '10px 20px',
                                backgroundColor: '#ff9800',
                                color: 'white',
                                border: 'none',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '600',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '6px'
                              }}>
                              💬 Send Message
                            </button>
                          )}
                        </div>
                      </div>
                    );
                  })}
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
