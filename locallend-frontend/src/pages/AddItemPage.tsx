import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

interface AddItemPageProps {
  onBack: () => void;
}

export const AddItemPage: React.FC<AddItemPageProps> = ({ onBack }) => {
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [categories, setCategories] = useState<any[]>([]);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: '',
    condition: 'GOOD',
    deposit: ''
  });

  // Load categories from backend on component mount
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/categories`);
        if (response.ok) {
          const categoryResponse = await response.json();
          console.log('Categories loaded:', categoryResponse);
          // Extract the data array from the response
          const categoryData = categoryResponse.data || categoryResponse;
          setCategories(categoryData);
        } else {
          console.error('Failed to load categories');
          // Fallback categories if API fails
          setCategories([
            { id: '1', name: 'Electronics' },
            { id: '2', name: 'Tools' },
            { id: '3', name: 'Sports' },
            { id: '4', name: 'Books' },
            { id: '5', name: 'Furniture' },
            { id: '6', name: 'Vehicles' },
            { id: '7', name: 'Appliances' },
            { id: '8', name: 'Other' }
          ]);
        }
      } catch (error) {
        console.error('Error fetching categories:', error);
        // Fallback categories if API fails
        setCategories([
          { id: '1', name: 'Electronics' },
          { id: '2', name: 'Tools' },
          { id: '3', name: 'Sports' },
          { id: '4', name: 'Books' },
          { id: '5', name: 'Furniture' },
          { id: '6', name: 'Vehicles' },
          { id: '7', name: 'Appliances' },
          { id: '8', name: 'Other' }
        ]);
      }
    };

    fetchCategories();
  }, []);

  const conditions = [
    { value: 'NEW', label: 'New' },
    { value: 'EXCELLENT', label: 'Excellent - Like new' },
    { value: 'GOOD', label: 'Good - Minor wear' },
    { value: 'FAIR', label: 'Fair - Noticeable wear' },
    { value: 'POOR', label: 'Poor - Significant wear' }
  ];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!user) {
      alert('You must be logged in to add items');
      return;
    }

    // Client-side validation
    if (formData.title.length < 3 || formData.title.length > 100) {
      alert('Item title must be between 3 and 100 characters long');
      return;
    }

    if (formData.description.length < 10 || formData.description.length > 500) {
      alert('Item description must be between 10 and 500 characters long');
      return;
    }

    if (!formData.category) {
      alert('Please select a category');
      return;
    }

    if (formData.deposit === '' || isNaN(Number(formData.deposit)) || Number(formData.deposit) < 0) {
      alert('Deposit must be a non-negative number');
      return;
    }

    setIsLoading(true);
    
    try {
      // Prepare item data matching the backend CreateItemRequest interface
      const itemData = {
        name: formData.title, // Backend expects 'name', not 'title'
        description: formData.description,
        categoryId: formData.category, // Backend expects categoryId
        condition: formData.condition,
        deposit: Number(formData.deposit) || 0,
        images: [] // No image upload implemented yet
      };

      console.log('Submitting item data:', itemData);

      // Use the itemService to create the item (it handles auth headers properly)
      const newItem = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/items`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'X-User-Id': user.id.toString()
        },
        body: JSON.stringify(itemData)
      });

      if (newItem.ok) {
        const createdItem = await newItem.json();
        console.log('Item created successfully:', createdItem);
        alert(`Item "${createdItem.name || formData.title}" added successfully!`);
        
        // Reset form
        setFormData({
          title: '',
          description: '',
          category: '',
          condition: 'GOOD',
          deposit: ''
        });
        
        onBack(); // Go back to dashboard/home
      } else {
        const errorResponse = await newItem.text();
        console.error('Server error response:', errorResponse);
        
        let errorMessage = 'Failed to add item';
        try {
          const errorData = JSON.parse(errorResponse);
          errorMessage = errorData.message || errorData.error || errorMessage;
        } catch (e) {
          errorMessage = errorResponse || errorMessage;
        }
        
        throw new Error(`Server error (${newItem.status}): ${errorMessage}`);
      }
    } catch (error: any) {
      console.error('Error adding item:', error);
      
      if (error.name === 'TypeError' && error.message.includes('fetch')) {
        alert('Network error: Cannot connect to backend server. Please ensure the backend is running on http://localhost:8080');
      } else {
        alert(`Error adding item: ${error.message}`);
      }
    } finally {
      setIsLoading(false);
    }
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

      {/* Add Item Form */}
      <div style={{ padding: '0 20px', maxWidth: '800px', margin: '0 auto' }}>
        <h1 style={{ marginBottom: '10px', color: '#1976d2' }}>Add New Item</h1>
        <p style={{ color: '#666', marginBottom: '30px' }}>
          List an item you'd like to lend to others in your community.
        </p>

        <div style={{ 
          backgroundColor: 'white', 
          padding: '30px', 
          borderRadius: '8px', 
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)' 
        }}>
          <form onSubmit={handleSubmit}>
            {/* Title */}
            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
                Item Title * (3-100 characters)
              </label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                required
                minLength={3}
                maxLength={100}
                placeholder="e.g., Canon DSLR Camera, Mountain Bike, Power Drill"
                style={{
                  width: '100%',
                  padding: '12px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '16px',
                  boxSizing: 'border-box'
                }}
              />
              {formData.title.length > 0 && formData.title.length < 3 && (
                <div style={{ color: 'red', fontSize: '12px', marginTop: '4px' }}>
                  Title must be at least 3 characters long
                </div>
              )}
              {formData.title.length > 100 && (
                <div style={{ color: 'red', fontSize: '12px', marginTop: '4px' }}>
                  Title must be no more than 100 characters long
                </div>
              )}
            </div>

            {/* Description */}
            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
                Description * (10-500 characters)
              </label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                required
                minLength={10}
                maxLength={500}
                rows={4}
                placeholder="Describe your item in detail - condition, features, what's included..."
                style={{
                  width: '100%',
                  padding: '12px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '16px',
                  boxSizing: 'border-box',
                  resize: 'vertical'
                }}
              />
              <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                {formData.description.length}/500 characters
              </div>
              {formData.description.length > 0 && formData.description.length < 10 && (
                <div style={{ color: 'red', fontSize: '12px', marginTop: '4px' }}>
                  Description must be at least 10 characters long
                </div>
              )}
            </div>

            {/* Category and Condition Row */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
                  Category *
                </label>
                <select
                  name="category"
                  value={formData.category}
                  onChange={handleChange}
                  required
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '4px',
                    fontSize: '16px',
                    boxSizing: 'border-box'
                  }}
                >
                  <option value="">Select a category</option>
                  {categories.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
                  Condition *
                </label>
                <select
                  name="condition"
                  value={formData.condition}
                  onChange={handleChange}
                  required
                  style={{
                    width: '100%',
                    padding: '12px',
                    border: '1px solid #ddd',
                    borderRadius: '4px',
                    fontSize: '16px',
                    boxSizing: 'border-box'
                  }}
                >
                  {conditions.map(condition => (
                    <option key={condition.value} value={condition.value}>
                      {condition.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Deposit */}
            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
                Deposit ($) *
              </label>
              <input
                type="number"
                name="deposit"
                value={formData.deposit}
                onChange={handleChange}
                required
                min="0"
                step="0.01"
                placeholder="25.00"
                style={{
                  width: '200px',
                  padding: '12px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  fontSize: '16px',
                  boxSizing: 'border-box'
                }}
              />
            </div>


            {/* Submit Buttons */}
            <div style={{ display: 'flex', gap: '15px' }}>
              <button
                type="submit"
                disabled={isLoading}
                style={{
                  padding: '12px 30px',
                  backgroundColor: isLoading ? '#ccc' : '#4caf50',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                  fontSize: '16px',
                  fontWeight: 'bold'
                }}
              >
                {isLoading ? 'Adding Item...' : 'Add Item'}
              </button>
              
              <button
                type="button"
                onClick={onBack}
                disabled={isLoading}
                style={{
                  padding: '12px 30px',
                  backgroundColor: '#757575',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                  fontSize: '16px'
                }}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
