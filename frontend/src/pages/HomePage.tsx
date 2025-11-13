// Home/Dashboard Page Component
// This displays items available for borrowing with search and filter functionality

import React, { useState, useEffect } from 'react';
import {
  Container,
  Grid,
  Card,
  CardMedia,
  CardContent,
  Typography,
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Pagination,
  CircularProgress,
  Alert,
  Chip,
  Rating
} from '@mui/material';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { itemService } from '../services/itemService';
import { categoryService } from '../services/categoryService';
import { handleApiError } from '../services/api';
import { formatCurrency } from '../utils/helpers';
import { ITEM_CONDITIONS, DEFAULT_PAGE_SIZE } from '../utils/constants';
import type { Item, Category, ItemCondition } from '../types';

export const HomePage: React.FC = () => {
  const [items, setItems] = useState<Item[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  // Get current filters from URL
  const currentPage = parseInt(searchParams.get('page') || '1');
  const categoryId = searchParams.get('categoryId') || '';
  const condition = searchParams.get('condition') || '';
  const sort = searchParams.get('sort') || 'createdAt,desc';
  const query = searchParams.get('query') || '';

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchItems();
  }, [searchParams]);

  const fetchCategories = async () => {
    try {
      const response = await categoryService.getCategories();
      setCategories(response.data || []);
    } catch (error) {
      console.error('Failed to fetch categories:', error);
    }
  };

  const fetchItems = async () => {
    setLoading(true);
    setError('');

    try {
      const params = {
        page: currentPage - 1, // Backend uses 0-based pagination
        size: DEFAULT_PAGE_SIZE,
        sort,
        ...(categoryId && { categoryId }),
        ...(condition && { condition: condition as ItemCondition }),
        ...(query && { query })
      };

      let response;
      if (query) {
        response = await itemService.searchItems(query, categoryId, condition as ItemCondition);
      } else {
        response = await itemService.getItems(params);
      }

      setItems(response.content || response.data || []);
      setTotalPages(response.totalPages || 1);
      setTotalItems(response.totalElements || response.count || 0);
    } catch (err) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (filterName: string, value: string) => {
    const newParams = new URLSearchParams(searchParams);
    
    if (value) {
      newParams.set(filterName, value);
    } else {
      newParams.delete(filterName);
    }
    
    // Reset to page 1 when filters change
    newParams.set('page', '1');
    
    setSearchParams(newParams);
  };

  const handlePageChange = (_event: React.ChangeEvent<unknown>, page: number) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set('page', page.toString());
    setSearchParams(newParams);
  };

  const handleItemClick = (itemId: string) => {
    navigate(`/items/${itemId}`);
  };

  if (loading && items.length === 0) {
    return (
      <Container>
        <Box display="flex" justifyContent="center" mt={4}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Hero Section */}
      <Box textAlign="center" mb={6}>
        <Typography variant="h3" component="h1" gutterBottom>
          Find Items to Borrow in Your Community
        </Typography>
        <Typography variant="h6" color="text.secondary">
          LocalLend connects neighbors to share items and build community trust.
        </Typography>
      </Box>

      {/* Filters */}
      <Box mb={4}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={3}>
            <FormControl fullWidth>
              <InputLabel>Category</InputLabel>
              <Select
                value={categoryId}
                onChange={(e) => handleFilterChange('categoryId', e.target.value)}
                label="Category"
              >
                <MenuItem value="">All Categories</MenuItem>
                {categories.map((category) => (
                  <MenuItem key={category.id} value={category.id}>
                    {category.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} sm={3}>
            <FormControl fullWidth>
              <InputLabel>Condition</InputLabel>
              <Select
                value={condition}
                onChange={(e) => handleFilterChange('condition', e.target.value)}
                label="Condition"
              >
                <MenuItem value="">All Conditions</MenuItem>
                {Object.values(ITEM_CONDITIONS).map((cond) => (
                  <MenuItem key={cond} value={cond}>
                    {cond}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} sm={3}>
            <FormControl fullWidth>
              <InputLabel>Sort By</InputLabel>
              <Select
                value={sort}
                onChange={(e) => handleFilterChange('sort', e.target.value)}
                label="Sort By"
              >
                <MenuItem value="createdAt,desc">Newest First</MenuItem>
                <MenuItem value="name,asc">Name A-Z</MenuItem>
                <MenuItem value="averageRating,desc">Highest Rated</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} sm={3}>
            <Typography variant="body2" color="text.secondary">
              {totalItems} items found
            </Typography>
          </Grid>
        </Grid>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Items Grid */}
      <Grid container spacing={3}>
        {items.map((item) => (
          <Grid item xs={12} sm={6} md={4} lg={3} key={item.id}>
            <Card 
              sx={{ 
                height: '100%', 
                cursor: 'pointer',
                '&:hover': { transform: 'translateY(-4px)' },
                transition: 'transform 0.2s'
              }}
              onClick={() => handleItemClick(item.id)}
            >
              <CardMedia
                component="img"
                height="200"
                image={item.images?.[0] || '/placeholder-image.jpg'}
                alt={item.name}
              />
              <CardContent>
                <Typography gutterBottom variant="h6" component="h3" noWrap>
                  {item.name}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                  by {item.ownerName}
                </Typography>
                <Typography variant="body2" sx={{ mb: 2, height: 40, overflow: 'hidden' }}>
                  {item.description}
                </Typography>
                
                <Box display="flex" flexDirection="column" gap={1}>
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Chip 
                      label={item.condition} 
                      size="small" 
                      color={item.condition === 'NEW' ? 'success' : 'default'}
                    />
                    <Box display="flex" alignItems="center" gap={0.5}>
                      <Rating value={item.averageRating} readOnly size="small" precision={0.1} />
                      <Typography variant="caption">
                        ({item.averageRating?.toFixed(1)})
                      </Typography>
                    </Box>
                  </Box>
                  
                  <Typography variant="h6" color="primary">
                    {item.deposit > 0 ? `${formatCurrency(item.deposit)} deposit` : 'No deposit'}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {items.length === 0 && !loading && (
        <Box textAlign="center" py={8}>
          <Typography variant="h6" color="text.secondary">
            No items found matching your criteria.
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Try adjusting your filters or search terms.
          </Typography>
        </Box>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <Box display="flex" justifyContent="center" mt={4}>
          <Pagination
            count={totalPages}
            page={currentPage}
            onChange={handlePageChange}
            color="primary"
          />
        </Box>
      )}

      {loading && items.length > 0 && (
        <Box display="flex" justifyContent="center" mt={2}>
          <CircularProgress size={24} />
        </Box>
      )}
    </Container>
  );
};