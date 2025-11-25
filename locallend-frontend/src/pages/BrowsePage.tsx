// Browse Items Page Component
import React, { useState, useEffect } from 'react';
import type { Item, Category } from '../types';
import { itemService } from '../services/itemService';
import { categoryService } from '../services/categoryService';
import { handleApiError } from '../services/api';
import { DEFAULT_PAGE_SIZE } from '../utils/constants';

export interface BrowsePageProps {
  onBack?: () => void;
}

export const BrowsePage: React.FC<BrowsePageProps> = ({ onBack }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');

  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [categories, setCategories] = useState<Category[]>([]);
  const [totalItems, setTotalItems] = useState(0);

  // Treat selectedCategory as a categoryId (empty = all)
  const selectedCategoryId = selectedCategory || '';

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    // Fetch items whenever search or selected category changes
    fetchItems();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchQuery, selectedCategory]);

  const fetchCategories = async () => {
    try {
      const response = await categoryService.getCategories();
      setCategories(response.data || []);
    } catch (err) {
      // non-fatal - categories are just for the filter UI
      console.error('Failed to load categories for Browse page', err);
    }
  };

  const fetchItems = async () => {
    setLoading(true);
    setError('');

    try {
      let response;
      if (searchQuery) {
        response = await itemService.searchItems(searchQuery, selectedCategoryId || undefined);
      } else {
        response = await itemService.getItems({ page: 0, size: DEFAULT_PAGE_SIZE, ...(selectedCategoryId && { categoryId: selectedCategoryId }) });
      }

      setItems(response.content || response.data || []);
      setTotalItems(response.totalElements || response.count || (response.data || []).length || 0);
    } catch (err) {
      setError(handleApiError(err));
      setItems([]);
      setTotalItems(0);
    } finally {
      setLoading(false);
    }
  };

  const handleItemClick = (itemId: string) => {
    const target = `${window.location.origin}${window.location.pathname}#/items/${itemId}`;
    window.location.href = target;
  };

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
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 12 }}>
            <h1 style={{ color: '#ff9800', marginBottom: '20px' }}>
              Browse Available Items
            </h1>
            <div style={{ color: '#666', fontSize: 14 }}>{totalItems} items</div>
          </div>
          
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
              {categories.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
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
          {items.map(item => (
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
              onClick={() => handleItemClick(item.id)}
              onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
              onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
            >
                <div style={{ fontSize: '3rem', textAlign: 'center', marginBottom: '15px' }}>
                  {item.images && item.images.length > 0 ? (
                    <img src={item.images[0]} alt={item.name} style={{ width: 80, height: 80, objectFit: 'cover', borderRadius: 8 }} />
                  ) : (
                    '📦'
                  )}
                </div>
              
              <h3 style={{ color: '#ff9800', marginBottom: '10px' }}>
                {item.name}
              </h3>
              
              <div style={{
                display: 'inline-block',
                backgroundColor: '#e3f2fd',
                color: '#ff9800',
                padding: '4px 8px',
                borderRadius: '4px',
                fontSize: '12px',
                marginBottom: '10px'
              }}>
                {item.categoryName || 'General'}
              </div>
              
              <p style={{ color: '#666', marginBottom: '15px', lineHeight: '1.5' }}>
                {item.description}
              </p>
              
              <div style={{ marginBottom: '15px' }}>
                <div style={{ fontSize: '14px', color: '#666' }}>
                  Owner: {item.ownerName || 'Unknown'}
                </div>
                {/* Location is not part of the backend Item model - skip showing it here */}
              </div>
              
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <div style={{ fontWeight: 'bold', color: '#4caf50', fontSize: '18px' }}>
                  {item.deposit != null && item.deposit > 0 ? `₹${item.deposit} deposit` : 'No deposit'}
                </div>
                <button
                  style={{
                    padding: '8px 16px',
                    backgroundColor: '#ff9800',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '14px'
                  }}
                  onClick={(e) => {
                    // Prevent card click navigation when pressing this button
                    e.stopPropagation();
                    alert(`Request to borrow "${item.name}" - Feature coming soon!`);
                  }}
                >
                  Request to Borrow
                </button>
              </div>
            </div>
          ))}
        </div>

        {loading && items.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 30 }}>
            <div>Loading items…</div>
          </div>
        ) : error ? (
          <div style={{ backgroundColor: 'white', padding: '40px', borderRadius: '8px', textAlign: 'center', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
            <p style={{ color: '#b00020', fontSize: '18px' }}>{error}</p>
          </div>
        ) : items.length === 0 && (
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