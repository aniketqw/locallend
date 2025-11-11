import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

interface SearchResultsPageProps {
  onBack: () => void;
  searchQuery: string;
  onSearchQueryChange: (query: string) => void;
    onBookItem: (item: any) => void;
}

export const SearchResultsPage: React.FC<SearchResultsPageProps> = ({ onBack, searchQuery, onSearchQueryChange, onBookItem }) => {
  console.log('🔍🔍🔍 SearchResultsPage rendered with onBookItem:', onBookItem);
  console.log('🔍🔍🔍 SearchResultsPage component loaded successfully');
  
  useEffect(() => {
    console.log('🔍🔍🔍 SearchResultsPage useEffect - this should show when page loads');
  }, []);
  
  const { user } = useAuth();
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState('');

  // Helpers for richer display
  const formatCurrency = (amount: any) => {
    const n = Number(amount ?? 0);
    if (!isFinite(n)) return '$0.00';
    return n.toLocaleString(undefined, { style: 'currency', currency: 'USD' });
  };

  const formatDate = (dateLike: any) => {
    if (!dateLike) return '—';
    try {
      const d = new Date(dateLike);
      return d.toLocaleDateString();
    } catch {
      return '—';
    }
  };

  const getStatusStyle = (status?: string) => {
    const s = (status || 'AVAILABLE').toUpperCase();
    if (s === 'AVAILABLE') return { bg: '#4caf50', label: 'Available' };
    if (s === 'BORROWED') return { bg: '#ff9800', label: 'Borrowed' };
    return { bg: '#9e9e9e', label: 'Unavailable' };
  };

  const renderStars = (rating?: number) => {
    const r = Math.round(Number(rating || 0));
    const stars = Array.from({ length: 5 }, (_, i) => (i < r ? '★' : '☆')).join('');
    return <span aria-label={`Rating ${r} of 5`} title={`Rating ${r}/5`} style={{ color: '#fbc02d' }}>{stars}</span>;
  };

  useEffect(() => {
    const performSearch = async () => {
      try {
        setIsLoading(true);
        const term = (searchQuery || '').trim();
        const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
        const headers: Record<string, string> = {
          'Content-Type': 'application/json',
        };
        const token = localStorage.getItem('token');
        if (token) headers['Authorization'] = `Bearer ${token}`;
        if (user?.id) headers['X-User-Id'] = user.id.toString();

        let results: any[] = [];

        // 1) If user typed a term, try the dedicated search endpoint
        if (term) {
          const qp = new URLSearchParams();
          qp.append('query', term);
          // Only pass categoryId if it looks like a real id (not display name)
          if (selectedCategory && selectedCategory.length > 12) {
            qp.append('categoryId', selectedCategory);
          }
          const url = `${baseUrl}/api/items/search?${qp.toString()}`;
          console.log('[Search] Fetching:', url);
          const res = await fetch(url, { headers });
          if (res.ok) {
            const data = await res.json();
            results = Array.isArray(data) ? data : (data.content || data.data || []);
            console.log('[Search] /api/items/search returned:', results.length, 'items');
          } else {
            console.warn('[Search] /api/items/search failed with status:', res.status);
          }
        }

        // 2) Fallback: fetch general items and optionally client-filter
        if (!term || results.length === 0) {
          const qp = new URLSearchParams();
          qp.append('page', '0');
          qp.append('size', '50');
          if (selectedCategory && selectedCategory.length > 12) {
            qp.append('categoryId', selectedCategory);
          }
          const url = `${baseUrl}/api/items?${qp.toString()}`;
          console.log('[Search] Fallback fetching:', url);
          const res = await fetch(url, { headers });
          if (res.ok) {
            const data = await res.json();
            const all = data.content || data.data || [];
            console.log('[Search] /api/items returned:', all.length, 'items');
            results = term
              ? all.filter((it: any) => {
                  const name = (it.name || it.title || '').toString().toLowerCase();
                  const desc = (it.description || '').toString().toLowerCase();
                  const categoryName = (it.categoryName || it.category || '').toString().toLowerCase();
                  const q = term.toLowerCase();
                  return name.includes(q) || desc.includes(q) || categoryName.includes(q);
                })
              : all;
            console.log('[Search] After client filter:', results.length, 'items');
          } else {
            console.warn('[Search] /api/items fallback failed with status:', res.status);
          }
        }

        // Exclude current user's items
        const filtered = results.filter((it: any) => {
          const ownerId = it.ownerId || it.owner?.id || it.owner_id;
          return !user?.id || ownerId !== user.id;
        });
        console.log('[Search] After excluding own items:', filtered.length, 'items');
        setSearchResults(filtered);
      } catch (error) {
        console.error('Search error:', error);
        setSearchResults([]);
      } finally {
        setIsLoading(false);
      }
    };

    performSearch();
  }, [searchQuery, selectedCategory, user?.id]);

  const handleNewSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // Search will be triggered by useEffect when searchQuery changes
  };

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
          justifyContent: 'space-between',
          gap: '20px'
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

          {/* Search Bar */}
          <form onSubmit={handleNewSearch} style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            flex: 1,
            maxWidth: '500px'
          }}>
            <input
              type="search"
              placeholder="Search items..."
              value={searchQuery}
              onChange={(e) => onSearchQueryChange(e.target.value)}
              style={{
                flex: 1,
                padding: '8px 12px',
                border: 'none',
                borderRadius: '4px',
                fontSize: '14px'
              }}
            />
            <select 
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              style={{
                padding: '8px',
                border: 'none',
                borderRadius: '4px',
                fontSize: '14px'
              }}
            >
              <option value="">All Categories</option>
              <option value="electronics">Electronics</option>
              <option value="tools">Tools</option>
              <option value="sports">Sports</option>
              <option value="books">Books</option>
            </select>
            <button type="submit" style={{
              padding: '8px 16px',
              backgroundColor: '#1565c0',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px'
            }}>
              Search
            </button>
          </form>
          
          {user && (
            <span style={{ 
              color: 'white', 
              fontSize: '14px',
              padding: '8px 12px',
              backgroundColor: 'rgba(255, 255, 255, 0.1)',
              borderRadius: '4px'
            }}>
              Welcome, {user.name}!
            </span>
          )}
        </div>
      </nav>

      {/* Search Results Content */}
      <div style={{ padding: '0 20px', maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{ marginBottom: '20px' }}>
          <h1 style={{ color: '#1976d2', marginBottom: '10px' }}>
            Search Results {searchQuery && `for "${searchQuery}"`}
          </h1>
          {selectedCategory && (
            <p style={{ color: '#666', margin: '0' }}>
              Category: {selectedCategory}
            </p>
          )}
        </div>
        
        {isLoading ? (
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <p>Searching for items...</p>
          </div>
        ) : (
          <>
            <div style={{ marginBottom: '20px', color: '#666' }}>
              Found {searchResults.length} item{searchResults.length !== 1 ? 's' : ''}
            </div>
            
            {searchResults.length === 0 ? (
              <div style={{ 
                backgroundColor: 'white', 
                padding: '40px', 
                borderRadius: '8px', 
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                textAlign: 'center'
              }}>
                <h3 style={{ color: '#666', marginBottom: '10px' }}>No items found</h3>
                <p style={{ color: '#888' }}>
                  Try adjusting your search terms or browse all available items.
                </p>
                <button 
                  onClick={() => onSearchQueryChange('')}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#1976d2',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    marginTop: '15px'
                  }}
                >
                  Clear Search
                </button>
              </div>
            ) : (
              <div style={{ 
                display: 'grid', 
                gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', 
                gap: '20px' 
              }}>
                {searchResults.map((item) => {
                  const isOwn = !!user && (item.ownerId === user.id || item.owner?.id === user.id);
                  const name = item.name || item.title || 'Untitled';
                  const description = item.description || '';
                  const ownerName = item.ownerName || item.owner?.name || 'Unknown';
                  const categoryName = item.categoryName || item.category || 'General';
                  const imageSrc = item.imageUrl || item.images?.[0] || '';
                  const deposit = item.deposit ?? 0;
                  const avgRating = item.averageRating ?? item.rating ?? 0;
                  const statusInfo = getStatusStyle(item.status);
                  const createdAt = formatDate(item.createdAt);
                  const canBorrow = item.canBeBorrowed !== false;

                  return (
                  <div key={item.id} style={{ 
                    backgroundColor: 'white', 
                    padding: '20px', 
                    borderRadius: '8px', 
                    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                    display: 'flex',
                    flexDirection: 'column',
                    height: 'fit-content'
                  }}>
                    {/* Item Image */}
                    <div style={{
                      width: '100%',
                      height: '200px',
                      backgroundColor: '#e0e0e0',
                      borderRadius: '4px',
                      marginBottom: '15px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: '#666'
                    }}>
                      {imageSrc ? (
                        <img src={imageSrc} alt={name} style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '4px' }} />
                      ) : (
                        <span>No Image</span>
                      )}
                    </div>

                    {/* Title and status */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8 }}>
                      <h3 style={{ margin: '0 0 8px 0', color: '#333' }}>{name}</h3>
                      <span style={{ padding: '2px 8px', borderRadius: 12, backgroundColor: statusInfo.bg, color: 'white', fontSize: 12 }}>
                        {statusInfo.label}
                      </span>
                    </div>
                    <p style={{ margin: '0 0 10px 0', color: '#666', fontSize: '14px', flexGrow: 1 }}>{description}</p>

                    {/* Owner */}
                    <div style={{ marginBottom: '10px', fontSize: '14px' }}>
                      <span style={{ color: '#666' }}>Owner: </span>
                      <span style={{ color: '#333', fontWeight: 'bold' }}>{ownerName}</span>
                    </div>

                    {/* Chips: deposit, condition, category, borrowable */}
                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '15px', fontSize: '12px' }}>
                      <span style={{ padding: '2px 8px', borderRadius: '12px', backgroundColor: '#4caf50', color: 'white' }}>
                        Deposit {formatCurrency(deposit)}
                      </span>
                      {item.condition && (
                        <span style={{ padding: '2px 8px', borderRadius: '12px', backgroundColor: '#2196f3', color: 'white' }}>
                          {item.condition}
                        </span>
                      )}
                      <span style={{ padding: '2px 8px', borderRadius: '12px', backgroundColor: '#ff9800', color: 'white' }}>
                        {categoryName}
                      </span>
                      <span style={{ padding: '2px 8px', borderRadius: '12px', backgroundColor: canBorrow ? '#7cb342' : '#9e9e9e', color: 'white' }}>
                        {canBorrow ? 'Borrowable' : 'Not Borrowable'}
                      </span>
                    </div>

                    {/* Rating & Created */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                      <div>
                        {renderStars(avgRating)}
                        <span style={{ marginLeft: 8, color: '#666', fontSize: 12 }}>{Number(avgRating || 0).toFixed(1)}</span>
                      </div>
                      <div style={{ color: '#666', fontSize: 12 }}>
                        Added: {createdAt}
                      </div>
                    </div>

                    {/* Book button */}
                    <button
                      onClick={(e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        console.log('🔥 Book click for item:', item);
                        try {
                          if (isOwn) {
                            alert('You cannot book your own item.');
                            return;
                          }
                          onBookItem(item);
                        } catch (error) {
                          console.error('🔥🔥🔥 Error calling onBookItem:', error);
                        }
                      }}
                      style={{
                        width: '100%',
                        padding: '10px',
                        backgroundColor: isOwn ? '#bdbdbd' : '#1976d2',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: isOwn ? 'not-allowed' : 'pointer',
                        fontSize: '14px',
                        fontWeight: 'bold'
                      }}
                      disabled={isOwn}
                    >
                      {isOwn ? 'Cannot Book Own Item' : 'Book This Item'}
                    </button>
                  </div>
                );})}
              </div>
            )}
          </>
        )}
        
        <div style={{ marginTop: '30px', textAlign: 'center' }}>
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
