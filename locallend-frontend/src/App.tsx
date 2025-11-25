// App.tsx - Main application component for LocalLend

import React, { useState, useEffect } from 'react';
import { useAuth } from './context/AuthContext';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { BrowsePage } from './pages/BrowsePage';
import { DashboardPage } from './pages/DashboardPage';
import { MyItemsPage } from './pages/MyItemsPage';
import { MyBookingsPage } from './pages/MyBookingsPage';
import { AddItemPage } from './pages/AddItemPage';
import { SearchResultsPage } from './pages/SearchResultsPage';
import { BookItemPage } from './pages/BookItemPage';
import { categoryService } from './services/categoryService';
import type { Category } from './types';

type Page = 'home' | 'login' | 'register' | 'browse' | 'search' | 'dashboard' | 'my-items' | 'my-bookings' | 'add-item' | 'book-item';

const App = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState<Page>('home');
  const [categories, setCategories] = useState<Category[]>([]);

  // Restore last page from localStorage or URL hash (simple persistence)
  useEffect(() => {
    // If an explicit hash is present, prefer it (for cross-build/routing compatibility)
    const hashPage = window.location.hash ? window.location.hash.replace('#', '') as Page : null;
    const saved = localStorage.getItem('currentPage') as Page | null;
    if (hashPage) {
      setCurrentPage(hashPage);
    } else if (saved) {
      // If page requires auth and user not logged in, fallback to home
      const authPages: Page[] = ['dashboard', 'my-items', 'my-bookings', 'add-item', 'book-item'];
      if (authPages.includes(saved) && !user) {
        setCurrentPage('home');
      } else {
        setCurrentPage(saved);
        // Restore selected item for book-item page
        if (saved === 'book-item') {
          const savedItem = localStorage.getItem('selectedItem');
          if (savedItem) {
            try {
              setSelectedItem(JSON.parse(savedItem));
            } catch {}
          }
        }
      }
    }
  }, [user]);

  // Listen for hash changes so fallback navigation (e.g. `location.hash='#register'`) works
  useEffect(() => {
    const onHash = () => {
      const p = window.location.hash ? window.location.hash.replace('#', '') as Page : null;
      if (p && p !== currentPage) setCurrentPage(p);
    };
    window.addEventListener('hashchange', onHash);
    return () => window.removeEventListener('hashchange', onHash);
  }, [currentPage]);

  // Persist page on change
  useEffect(() => {
    localStorage.setItem('currentPage', currentPage);
    if (currentPage !== 'book-item') {
      localStorage.removeItem('selectedItem');
    }
  }, [currentPage]);

  // Fetch categories for dropdown
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await categoryService.getCategories();
        if (response && response.data) {
          setCategories(response.data);
        }
      } catch (error) {
        console.error('Failed to fetch categories:', error);
      }
    };
    fetchCategories();
  }, []);

  const [selectedItem, setSelectedItem] = useState<any>(null);
  // Debugging: surface any uncaught runtime errors to the UI so blank screens are diagnosable
  const [globalError, setGlobalError] = useState<string | null>(null);

  useEffect(() => {
    const onErr = (event: any) => {
      try {
        const msg = event?.message || String(event?.reason || event);
        console.error('Global error captured:', event);
        setGlobalError(msg?.toString?.() || 'Unknown error');
      } catch (e) {
        setGlobalError('Fatal error (could not stringify)');
      }
    };

    const onRejection = (ev: PromiseRejectionEvent) => {
      try {
        const info = ev?.reason?.message || String(ev?.reason || ev);
        console.error('Unhandled rejection:', ev);
        setGlobalError(info?.toString?.() || 'Unhandled rejection');
      } catch {
        setGlobalError('Unhandled rejection (could not stringify)');
      }
    };

    window.addEventListener('error', onErr as EventListener);
    window.addEventListener('unhandledrejection', onRejection as EventListener);
    return () => {
      window.removeEventListener('error', onErr as EventListener);
      window.removeEventListener('unhandledrejection', onRejection as EventListener);
    };
  }, []);

  // Auto-route to dashboard after successful login
  useEffect(() => {
    if (isAuthenticated && user && currentPage === 'login') {
      setCurrentPage('dashboard');
    }
  }, [isAuthenticated, user, currentPage]);

  const handleLogin = () => setCurrentPage('login');

  const handleRegister = () => setCurrentPage('register');

  const handleBrowse = () => setCurrentPage('browse');

  const handleDashboard = () => setCurrentPage('dashboard');

  // my-items / my-bookings routing are available via menu/buttons in the app,
  // these local handlers were defined but not used — removed to avoid TS errors.

  const handleAddItem = () => setCurrentPage('add-item');

    const handleBookItem = (item: any) => {
    if (!user) {
      alert('Please log in to book items');
      return;
    }
      // Prevent booking own item
      if (item?.ownerId === user.id || item?.owner?.id === user.id) {
        alert("You can't book your own item.");
        return;
      }
    
      setSelectedItem(item);
      try { localStorage.setItem('selectedItem', JSON.stringify(item)); } catch {}
    setCurrentPage('book-item');
  };

  const handleLogout = () => {
    logout();
    setCurrentPage('home');
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      setCurrentPage('search');
    }
  };

  const navigateToHome = () => {
    setCurrentPage('home');
  };

  // Render different pages based on current page state
  if (currentPage === 'login') {
    return <LoginPage onBack={navigateToHome} onRegister={handleRegister} />;
  }

  if (currentPage === 'register') {
    return <RegisterPage onBack={navigateToHome} />;
  }

  if (currentPage === 'browse') {
    return <BrowsePage onBack={navigateToHome} />;
  }

  if (currentPage === 'search') {
    return <SearchResultsPage onBack={navigateToHome} searchQuery={searchQuery} onSearchQueryChange={setSearchQuery} onBookItem={handleBookItem} />;
  }

  if (currentPage === 'dashboard') {
    return <DashboardPage onBack={navigateToHome} onAddItem={handleAddItem} />;
  }

  if (currentPage === 'my-items') {
    return <MyItemsPage onBack={navigateToHome} />;
  }

  if (currentPage === 'my-bookings') {
    return <MyBookingsPage onBack={navigateToHome} />;
  }

  if (currentPage === 'add-item') {
    return <AddItemPage onBack={navigateToHome} />;
  }

  if (currentPage === 'book-item') {
    return <BookItemPage onBack={() => setCurrentPage('search')} item={selectedItem} />;
  }

  return (
    <div style={{ fontFamily: 'Roboto, Arial, sans-serif', margin: '0', padding: '0', backgroundColor: '#ffffff', color: '#213547', minHeight: '100vh' }}>
      {/* Global error overlay (diagnostic) */}
      {globalError && (
        <div style={{ position: 'fixed', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 9999 }}>
          <div style={{ maxWidth: '900px', width: 'calc(100% - 40px)', background: 'rgba(255,255,255,0.98)', boxShadow: '0 10px 40px rgba(0,0,0,0.4)', padding: '22px', borderRadius: '8px', border: '2px solid #f44336', color: '#333' }}>
            <h3 style={{ margin: 0, color: '#b71c1c' }}>Application error detected</h3>
            <p style={{ marginTop: '8px' }}>The app encountered a runtime error. This overlay is a diagnostic helper — please copy the message below and share it with me.</p>
            <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word', background: '#fff', padding: '8px', borderRadius: '6px', border: '1px solid #eee', maxHeight: '220px', overflow: 'auto' }}>{globalError}</pre>
            <div style={{ display: 'flex', gap: '8px', marginTop: '12px', justifyContent: 'flex-end' }}>
              <button style={{ padding: '8px 12px', borderRadius: '4px', border: '1px solid #ccc', background: 'white', cursor: 'pointer' }} onClick={() => { navigator.clipboard?.writeText(globalError || ''); }}>Copy</button>
              <button style={{ padding: '8px 12px', borderRadius: '4px', border: 'none', background: '#f44336', color: 'white', cursor: 'pointer' }} onClick={() => setGlobalError(null)}>Dismiss</button>
            </div>
          </div>
        </div>
      )}
      <nav style={{ backgroundColor: '#ff9800', color: 'white', padding: '12px 0', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', marginBottom: '0' }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '0 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '20px' }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>
            <button onClick={navigateToHome} style={{ color: 'white', textDecoration: 'none', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.5rem', fontWeight: 'bold' }}>LocalLend</button>
          </div>

          <form onSubmit={handleSearch} style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1, maxWidth: '500px' }}>
            <input type="search" placeholder="Search items..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} style={{ flex: 1, padding: '8px 12px', border: 'none', borderRadius: '4px', fontSize: '14px' }} />
            <select style={{ padding: '8px', border: 'none', borderRadius: '4px', fontSize: '14px' }}>
              <option value="">All Categories</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>{category.name}</option>
              ))}
            </select>
            <button type="submit" style={{ padding: '8px 16px', backgroundColor: '#1565c0', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px' }}>Search</button>
          </form>

          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            {isAuthenticated && user ? (
              <>
                  <span style={{ color: 'white', fontSize: '14px', padding: '8px 12px', backgroundColor: 'rgba(255, 255, 255, 0.1)', borderRadius: '4px' }}>Welcome, {user.name}!</span>
                  <button onClick={handleBrowse} style={{ color: 'white', textDecoration: 'none', padding: '8px 12px', background: 'none', border: 'none', cursor: 'pointer' }}>Browse Items</button>
                  <button onClick={handleDashboard} style={{ color: 'white', textDecoration: 'none', padding: '8px 12px', background: 'none', border: 'none', cursor: 'pointer' }}>Dashboard</button>
                <button onClick={handleLogout} style={{ padding: '8px 16px', backgroundColor: '#d32f2f', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px' }}>Logout</button>
              </>
            ) : (
              <>
                <button onClick={handleBrowse} style={{ color: 'white', textDecoration: 'none', padding: '8px 12px', background: 'none', border: 'none', cursor: 'pointer' }}>Browse Items</button>
                <button onClick={handleLogin} style={{ padding: '8px 16px', backgroundColor: 'transparent', color: 'white', border: '1px solid white', borderRadius: '4px', cursor: 'pointer', fontSize: '14px' }}>Login</button>
                <button onClick={handleRegister} style={{ padding: '8px 16px', backgroundColor: '#4caf50', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px' }}>Register</button>
              </>
            )}
          </div>
        </div>
      </nav>

      <div style={{ padding: '20px' }}>
        <header style={{ backgroundColor: '#ff9800', color: 'white', padding: '30px', borderRadius: '12px', marginBottom: '30px', textAlign: 'center', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>
          <h1 style={{ margin: '0 0 10px 0', fontSize: '2.5rem' }}>LocalLend - Peer-to-Peer Item Sharing</h1>
          <p style={{ margin: '0', fontSize: '1.2rem', opacity: '0.9' }}>A community platform for borrowing and lending items</p>
        </header>

        <main style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <section style={{ marginBottom: '40px', backgroundColor: '#ffffff', padding: '30px', borderRadius: '12px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
            <h2 style={{ color: '#ff9800', marginBottom: '20px' }}>Welcome to LocalLend</h2>
            <p style={{ fontSize: '1.1rem', lineHeight: '1.6', color: '#424242' }}>LocalLend is a peer-to-peer item sharing platform where users can:</p>
            <ul style={{ fontSize: '1rem', lineHeight: '1.8', color: '#424242' }}>
              <li>List items they own for others to borrow</li>
              <li>Browse and search items available in their community</li>
              <li>Request to borrow items from other users</li>
              <li>Manage bookings (approve, track, complete)</li>
              <li>Rate users and items after completed transactions</li>
              <li>Build trust scores through positive interactions</li>
            </ul>
          </section>

        </main>

        <footer style={{ marginTop: '50px', textAlign: 'center', color: '#666', borderTop: '2px solid #e9ecef', paddingTop: '30px', backgroundColor: '#f8f9fa', marginLeft: '-20px', marginRight: '-20px', padding: '30px 20px' }}>
          <p style={{ margin: '10px 0', fontSize: '1rem' }}>LocalLend Frontend - Built with React + TypeScript + Vite</p>
          <p style={{ margin: '10px 0', fontSize: '0.9rem' }}>Backend API: http://localhost:8080</p>
          <p style={{ margin: '10px 0', fontSize: '0.9rem' }}>Frontend: http://localhost:5173</p>
        </footer>
      </div>
    </div>
  );
};

export default App;
