import React, { useState, useEffect } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Menu,
  MenuItem,
  TextField,
  InputAdornment,
  Box,
  Avatar,
  Badge,
  Select,
  FormControl,
  InputLabel
} from '@mui/material';
import {
  Search as SearchIcon,
  Notifications as NotificationsIcon,
  ExitToApp as LogoutIcon
} from '@mui/icons-material';
// avoid react-router hooks/components in production bundle (no Router)
import { useAuth } from '../context/AuthContext';
import { categoryService } from '../services/categoryService';
import type { Category } from '../types';

export const Navbar: React.FC = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [categories, setCategories] = useState<Category[]>([]);
  
  const { user, isAuthenticated, logout } = useAuth();
  // No react-router navigate in production: use window.location updates instead

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await categoryService.getCategories();
        console.log('Categories API response:', response);
        
        // The API returns { data: Category[], success: true, count: number, message: string }
        if (response && response.data) {
          console.log('Categories from response.data:', response.data);
          console.log('Number of categories:', response.data.length);
          setCategories(response.data);
        } else {
          console.warn('No categories data in response:', response);
          setCategories([]);
        }
      } catch (error) {
        console.error('Failed to fetch categories:', error);
      }
    };
    
    fetchCategories();
  }, []);

  const handleUserMenuClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleUserMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    handleUserMenuClose();
    // ensure we navigate back to root safely
    window.location.href = window.location.origin + window.location.pathname + '#';
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      const searchParams = new URLSearchParams();
      searchParams.set('query', searchQuery);
      if (selectedCategory) {
        searchParams.set('categoryId', selectedCategory);
      }
      const qs = searchParams.toString();
      const target = qs ? `${window.location.origin}${window.location.pathname}?${qs}` : `${window.location.origin}${window.location.pathname}`;
      window.location.href = target;
    }
  };

  const isOpen = Boolean(anchorEl);

  return (
    <AppBar position="static">
      <Toolbar>
          <Typography
          variant="h6"
          component="a"
          href="#"
          sx={{
            flexGrow: 0,
            mr: 4,
            textDecoration: 'none',
            color: 'inherit',
            fontWeight: 'bold'
          }}
        >
          LocalLend
        </Typography>

        <Box component="form" onSubmit={handleSearch} sx={{ display: 'flex', alignItems: 'center', flexGrow: 1, maxWidth: 600 }}>
          <TextField
            size="small"
            placeholder="Search items..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
            sx={{ flexGrow: 1, mr: 1, bgcolor: 'background.paper', borderRadius: 1 }}
          />
          
          <FormControl size="small" sx={{ minWidth: 120, mr: 1 }}>
            <InputLabel>Category</InputLabel>
            <Select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              label="Category"
              sx={{ bgcolor: 'background.paper' }}
            >
              <MenuItem value="">All Categories</MenuItem>
              {categories.map((category) => (
                <MenuItem key={category.id} value={category.id}>
                  {category.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          
          <Button
            type="submit"
            variant="contained"
            color="secondary"
            startIcon={<SearchIcon />}
          >
            Search
          </Button>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', ml: 2 }}>
          {isAuthenticated ? (
            <>
              <IconButton color="inherit" sx={{ mr: 1 }}>
                <Badge badgeContent={0} color="error">
                  <NotificationsIcon />
                </Badge>
              </IconButton>
              
              <IconButton
                color="inherit"
                onClick={handleUserMenuClick}
                sx={{ p: 0 }}
              >
                <Avatar alt={user?.name} src={user?.profileImageUrl}>
                  {user?.name?.[0]?.toUpperCase()}
                </Avatar>
              </IconButton>
              
              <Menu
                anchorEl={anchorEl}
                open={isOpen}
                onClose={handleUserMenuClose}
                transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
              >
                <MenuItem onClick={handleUserMenuClose}>
                  <a href="#dashboard" style={{ textDecoration: 'none', color: 'inherit' }}>
                    Dashboard
                  </a>
                </MenuItem>
                <MenuItem onClick={handleUserMenuClose}>
                  <a href="#profile" style={{ textDecoration: 'none', color: 'inherit' }}>
                    Profile
                  </a>
                </MenuItem>
                <MenuItem onClick={handleLogout}>
                  <LogoutIcon sx={{ mr: 1 }} />
                  Logout
                </MenuItem>
              </Menu>
            </>
          ) : (
            <>
              <Button
                color="inherit"
                component="a"
                href="#login"
                sx={{ mr: 1 }}
              >
                Login
              </Button>
              <Button
                variant="outlined"
                color="inherit"
                component="a"
                href="#register"
              >
                Register
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};
