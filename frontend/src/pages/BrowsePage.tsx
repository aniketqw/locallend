// Browse Items Page Component
import React, { useState } from 'react';

export interface BrowsePageProps {
  onBack?: () => void;
}

export const BrowsePage: React.FC<BrowsePageProps> = ({ onBack }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');

  // Mock data for items
  const mockItems = [
    {
      id: 1,
      title: 'Electric Drill',
      category: 'Tools',
      description: 'Professional grade electric drill, perfect for home projects',
      owner: 'John Smith',
      pricePerDay: 15,
      location: 'Downtown',
      image: '🔧'
    },
    {
      id: 2,
      title: 'Canon Camera',
      category: 'Electronics',
      description: 'DSLR camera with multiple lenses for photography enthusiasts',
      owner: 'Sarah Johnson',
      pricePerDay: 25,
      location: 'Uptown',
      image: '📷'
    },
    {
      id: 3,
      title: 'Mountain Bike',
      category: 'Sports',
      description: 'High-quality mountain bike for trail adventures',
      owner: 'Mike Wilson',
      pricePerDay: 20,
      location: 'Suburbs',
      image: '🚵‍♂️'
    },
    {
      id: 4,
      title: 'Programming Books Set',
      category: 'Books',
      description: 'Complete set of modern programming language books',
      owner: 'Lisa Chen',
      pricePerDay: 5,
      location: 'University Area',
      image: '📚'
    }
  ];

  const filteredItems = mockItems.filter(item => {
    const matchesSearch = item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         item.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === '' || item.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f5f5f5',
      padding: '20px'
    }}>
      <div style={{
        maxWidth: '1200px',
        margin: '0 auto'
      }}>
        {/* Header */}
        <div style={{
          backgroundColor: 'white',
          padding: '30px',
          borderRadius: '8px',
          marginBottom: '30px',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
        }}>
          <h1 style={{ color: '#1976d2', marginBottom: '20px' }}>
            Browse Available Items
          </h1>
          
          {/* Search and Filter */}
          <div style={{
            display: 'flex',
            gap: '15px',
            marginBottom: '20px',
            flexWrap: 'wrap'
          }}>
            <input
              type="search"
              placeholder="Search items..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                flex: 1,
                minWidth: '250px',
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '16px'
              }}
            />
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              style={{
                padding: '12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '16px',
                minWidth: '150px'
              }}
            >
              <option value="">All Categories</option>
              <option value="Tools">Tools</option>
              <option value="Electronics">Electronics</option>
              <option value="Sports">Sports</option>
              <option value="Books">Books</option>
            </select>
          </div>
          
          {onBack && (
            <button 
              onClick={onBack}
              style={{
                padding: '8px 16px',
                backgroundColor: '#666',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              ← Back to Home
            </button>
          )}
        </div>

        {/* Items Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
          gap: '20px'
        }}>
          {filteredItems.map(item => (
            <div
              key={item.id}
              style={{
                backgroundColor: 'white',
                borderRadius: '8px',
                padding: '20px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                transition: 'transform 0.2s',
                cursor: 'pointer'
              }}
              onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
              onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
            >
              <div style={{ fontSize: '3rem', textAlign: 'center', marginBottom: '15px' }}>
                {item.image}
              </div>
              
              <h3 style={{ color: '#1976d2', marginBottom: '10px' }}>
                {item.title}
              </h3>
              
              <div style={{
                display: 'inline-block',
                backgroundColor: '#e3f2fd',
                color: '#1976d2',
                padding: '4px 8px',
                borderRadius: '4px',
                fontSize: '12px',
                marginBottom: '10px'
              }}>
                {item.category}
              </div>
              
              <p style={{ color: '#666', marginBottom: '15px', lineHeight: '1.5' }}>
                {item.description}
              </p>
              
              <div style={{ marginBottom: '15px' }}>
                <div style={{ fontSize: '14px', color: '#666' }}>
                  Owner: {item.owner}
                </div>
                <div style={{ fontSize: '14px', color: '#666' }}>
                  Location: {item.location}
                </div>
              </div>
              
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <div style={{ fontWeight: 'bold', color: '#4caf50', fontSize: '18px' }}>
                  ₹${item.pricePerDay}/day
                </div>
                <button
                  style={{
                    padding: '8px 16px',
                    backgroundColor: '#1976d2',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '14px'
                  }}
                  onClick={() => alert(`Request to borrow "${item.title}" - Feature coming soon!`)}
                >
                  Request to Borrow
                </button>
              </div>
            </div>
          ))}
        </div>

        {filteredItems.length === 0 && (
          <div style={{
            backgroundColor: 'white',
            padding: '40px',
            borderRadius: '8px',
            textAlign: 'center',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <p style={{ color: '#666', fontSize: '18px' }}>
              No items found matching your search criteria.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};