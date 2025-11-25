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
  
  // Image upload states
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [uploadedImageUrls, setUploadedImageUrls] = useState<string[]>([]);
  const [isUploadingImages, setIsUploadingImages] = useState(false);

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

  // Handle image file selection
  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;

    const fileArray = Array.from(files);
    
    // Validate file count
    if (fileArray.length + selectedFiles.length > 5) {
      alert('Maximum 5 images allowed per item');
      return;
    }

    // Validate file types and sizes
    const validFiles: File[] = [];
    for (const file of fileArray) {
      if (!file.type.startsWith('image/')) {
        alert(`${file.name} is not an image file`);
        continue;
      }
      if (file.size > 10 * 1024 * 1024) {
        alert(`${file.name} exceeds 10MB size limit`);
        continue;
      }
      validFiles.push(file);
    }

    if (validFiles.length === 0) return;

    // Update selected files
    setSelectedFiles(prev => [...prev, ...validFiles]);

    // Generate previews
    validFiles.forEach(file => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreviews(prev => [...prev, reader.result as string]);
      };
      reader.readAsDataURL(file);
    });
  };

  // Remove an image from selection
  const handleRemoveImage = (index: number) => {
    setSelectedFiles(prev => prev.filter((_, i) => i !== index));
    setImagePreviews(prev => prev.filter((_, i) => i !== index));
    setUploadedImageUrls(prev => prev.filter((_, i) => i !== index));
  };

  // Upload images to backend
  const uploadImages = async (): Promise<string[]> => {
    if (selectedFiles.length === 0) return [];

    setIsUploadingImages(true);
    const uploadedUrls: string[] = [];

    try {
      // Upload images one by one (or use upload-multiple endpoint)
      for (const file of selectedFiles) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('folder', 'locallend/items');

        const response = await fetch(
          `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/images/upload`,
          {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: formData
          }
        );

        if (response.ok) {
          const result = await response.json();
          uploadedUrls.push(result.url);
        } else {
          console.error('Failed to upload image:', file.name);
          throw new Error(`Failed to upload ${file.name}`);
        }
      }

      setUploadedImageUrls(uploadedUrls);
      return uploadedUrls;
    } catch (error: any) {
      console.error('Image upload error:', error);
      throw new Error('Failed to upload images: ' + error.message);
    } finally {
      setIsUploadingImages(false);
    }
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
      // Upload images first (if any selected)
      let imageUrls: string[] = [];
      if (selectedFiles.length > 0) {
        try {
          imageUrls = await uploadImages();
          console.log('Images uploaded successfully:', imageUrls);
        } catch (uploadError: any) {
          alert('Failed to upload images. Please try again.');
          setIsLoading(false);
          return;
        }
      }

      // Prepare item data matching the backend CreateItemRequest interface
      const itemData = {
        name: formData.title, // Backend expects 'name', not 'title'
        description: formData.description,
        categoryId: formData.category, // Backend expects categoryId
        condition: formData.condition,
        deposit: Number(formData.deposit) || 0,
        images: imageUrls // Include uploaded image URLs
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
        setSelectedFiles([]);
        setImagePreviews([]);
        setUploadedImageUrls([]);
        
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

      {/* Add Item Form */}
      <div style={{ padding: '0 20px', maxWidth: '800px', margin: '0 auto' }}>
        <h1 style={{ marginBottom: '10px', color: '#ff9800' }}>Add New Item</h1>
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
                Deposit (₹) *
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

            {/* Image Upload Section */}
            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
                Item Images (Optional, max 5)
              </label>
              <p style={{ fontSize: '12px', color: '#666', marginBottom: '10px' }}>
                Upload clear photos of your item. Supported formats: JPG, PNG, GIF, WebP (max 10MB each)
              </p>
              
              <input
                type="file"
                accept="image/*"
                multiple
                onChange={handleImageSelect}
                disabled={isUploadingImages || selectedFiles.length >= 5}
                style={{
                  display: 'block',
                  marginBottom: '15px',
                  padding: '10px',
                  border: '2px dashed #ddd',
                  borderRadius: '4px',
                  width: '100%',
                  boxSizing: 'border-box',
                  cursor: selectedFiles.length >= 5 ? 'not-allowed' : 'pointer'
                }}
              />

              {/* Image Previews */}
              {imagePreviews.length > 0 && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))', gap: '10px' }}>
                  {imagePreviews.map((preview, index) => (
                    <div key={index} style={{ position: 'relative', borderRadius: '8px', overflow: 'hidden', border: '2px solid #ddd' }}>
                      <img 
                        src={preview} 
                        alt={`Preview ${index + 1}`} 
                        style={{ 
                          width: '100%', 
                          height: '120px', 
                          objectFit: 'cover',
                          display: 'block'
                        }} 
                      />
                      <button
                        type="button"
                        onClick={() => handleRemoveImage(index)}
                        disabled={isUploadingImages}
                        style={{
                          position: 'absolute',
                          top: '5px',
                          right: '5px',
                          backgroundColor: 'rgba(244, 67, 54, 0.9)',
                          color: 'white',
                          border: 'none',
                          borderRadius: '50%',
                          width: '24px',
                          height: '24px',
                          cursor: 'pointer',
                          fontSize: '16px',
                          lineHeight: '24px',
                          padding: '0',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center'
                        }}
                        title="Remove image"
                      >
                        ×
                      </button>
                      {uploadedImageUrls[index] && (
                        <div style={{
                          position: 'absolute',
                          bottom: '5px',
                          left: '5px',
                          backgroundColor: 'rgba(76, 175, 80, 0.9)',
                          color: 'white',
                          padding: '2px 6px',
                          borderRadius: '3px',
                          fontSize: '10px',
                          fontWeight: 'bold'
                        }}>
                          ✓ Uploaded
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}

              {isUploadingImages && (
                <div style={{ 
                  marginTop: '10px', 
                  padding: '10px', 
                  backgroundColor: '#e3f2fd', 
                  borderRadius: '4px',
                  color: '#ff9800',
                  fontSize: '14px'
                }}>
                  📤 Uploading images...
                </div>
              )}
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
