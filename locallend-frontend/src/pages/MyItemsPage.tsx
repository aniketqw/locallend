import React from 'react';

interface MyItemsPageProps {
  onBack: () => void;
}

export const MyItemsPage: React.FC<MyItemsPageProps> = ({ onBack }) => (
  <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
    <h1>My Items</h1>
    <p>Here you can manage the items you're lending out to others.</p>
    <button onClick={onBack} style={{ padding: '10px 20px', backgroundColor: '#ff9800', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
      Back to Home
    </button>
  </div>
);
