import React from 'react';

interface MyBookingsPageProps {
  onBack: () => void;
}

export const MyBookingsPage: React.FC<MyBookingsPageProps> = ({ onBack }) => (
  <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
    <h1>My Bookings</h1>
    <p>Here you can see all the items you've booked from other users.</p>
    <button onClick={onBack} style={{ padding: '10px 20px', backgroundColor: '#ff9800', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
      Back to Home
    </button>
  </div>
);
