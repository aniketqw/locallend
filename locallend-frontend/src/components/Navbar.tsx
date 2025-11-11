import React, { useState } from 'react';// Navigation Bar Component

import React, { useState } from 'react';

export interface NavbarProps {

  isAuthenticated?: boolean;export interface NavbarProps {

  onLogin?: () => void;  isAuthenticated?: boolean;

  onRegister?: () => void;  onLogin?: () => void;

  onLogout?: () => void;  onRegister?: () => void;

}  onLogout?: () => void;

}

export const Navbar: React.FC<NavbarProps> = ({ 

  isAuthenticated = false, export const Navbar: React.FC<NavbarProps> = ({ 

  onLogin,   isAuthenticated = false, 

  onRegister,   onLogin, 

  onLogout   onRegister, 

}) => {  onLogout 

  const [searchQuery, setSearchQuery] = useState('');}) => {

  const [searchQuery, setSearchQuery] = useState('');

  const handleSearch = (e: React.FormEvent) => {

    e.preventDefault();  const handleSearch = (e: React.FormEvent) => {

    console.log('Search for:', searchQuery);    e.preventDefault();

  };    console.log('Search for:', searchQuery);

  };

  return (

    <nav style={{  return (

      backgroundColor: '#1976d2',    <nav style={{

      color: 'white',      backgroundColor: '#1976d2',

      padding: '12px 0',      color: 'white',

      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',      padding: '12px 0',

      position: 'sticky',      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',

      top: 0,      position: 'sticky',

      zIndex: 1000      top: 0,

    }}>      zIndex: 1000

      <div style={{    }}>

        maxWidth: '1200px',      <div style={{

        margin: '0 auto',        maxWidth: '1200px',

        padding: '0 20px',        margin: '0 auto',

        display: 'flex',        padding: '0 20px',

        alignItems: 'center',        display: 'flex',

        justifyContent: 'space-between',        alignItems: 'center',

        gap: '20px'        justifyContent: 'space-between',

      }}>        gap: '20px'

        <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>      }}>

          <a href="/" style={{ color: 'white', textDecoration: 'none' }}>        <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>

            LocalLend          <a href="/" style={{ color: 'white', textDecoration: 'none' }}>

          </a>            LocalLend

        </div>          </a>

        </div>

        <form onSubmit={handleSearch} style={{

          display: 'flex',        <form onSubmit={handleSearch} style={{

          alignItems: 'center',          display: 'flex',

          gap: '8px',          alignItems: 'center',

          flex: 1,          gap: '8px',

          maxWidth: '500px'          flex: 1,

        }}>          maxWidth: '500px'

          <input        }}>

            type="search"          <input

            placeholder="Search items..."            type="search"

            value={searchQuery}            placeholder="Search items..."

            onChange={(e) => setSearchQuery(e.target.value)}            value={searchQuery}

            style={{            onChange={(e) => setSearchQuery(e.target.value)}

              flex: 1,            style={{

              padding: '8px 12px',              flex: 1,

              border: 'none',              padding: '8px 12px',

              borderRadius: '4px',              border: 'none',

              fontSize: '14px'              borderRadius: '4px',

            }}              fontSize: '14px'

          />            }}

          <select style={{          />

            padding: '8px',          <select style={{

            border: 'none',            padding: '8px',

            borderRadius: '4px',            border: 'none',

            fontSize: '14px'            borderRadius: '4px',

          }}>            fontSize: '14px'

            <option value="">All Categories</option>          }}>

            <option value="electronics">Electronics</option>            <option value="">All Categories</option>

            <option value="tools">Tools</option>            <option value="electronics">Electronics</option>

            <option value="sports">Sports</option>            <option value="tools">Tools</option>

            <option value="books">Books</option>            <option value="sports">Sports</option>

          </select>            <option value="books">Books</option>

          <button type="submit" style={{          </select>

            padding: '8px 16px',          <button type="submit" style={{

            backgroundColor: '#1565c0',            padding: '8px 16px',

            color: 'white',            backgroundColor: '#1565c0',

            border: 'none',            color: 'white',

            borderRadius: '4px',            border: 'none',

            cursor: 'pointer',            borderRadius: '4px',

            fontSize: '14px'            cursor: 'pointer',

          }}>            fontSize: '14px'

            Search          }}>

          </button>            Search

        </form>          </button>

        </form>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>

          {isAuthenticated ? (        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>

            <>          {isAuthenticated ? (

              <a href="/dashboard" style={{ color: 'white', textDecoration: 'none', padding: '8px 12px' }}>            <>

                Dashboard              <a href="/dashboard" style={{ color: 'white', textDecoration: 'none', padding: '8px 12px' }}>

              </a>                Dashboard

              <a href="/my-items" style={{ color: 'white', textDecoration: 'none', padding: '8px 12px' }}>              </a>

                My Items              <a href="/my-items" style={{ color: 'white', textDecoration: 'none', padding: '8px 12px' }}>

              </a>                My Items

              <a href="/my-bookings" style={{ color: 'white', textDecoration: 'none', padding: '8px 12px' }}>              </a>

                Bookings              <a href="/my-bookings" style={{ color: 'white', textDecoration: 'none', padding: '8px 12px' }}>

              </a>                Bookings

              <button              </a>

                onClick={onLogout}              <button

                style={{                onClick={onLogout}

                  padding: '8px 16px',                style={{

                  backgroundColor: '#d32f2f',                  padding: '8px 16px',

                  color: 'white',                  backgroundColor: '#d32f2f',

                  border: 'none',                  color: 'white',

                  borderRadius: '4px',                  border: 'none',

                  cursor: 'pointer',                  borderRadius: '4px',

                  fontSize: '14px'                  cursor: 'pointer',

                }}                  fontSize: '14px'

              >                }}

                Logout              >

              </button>                Logout

            </>              </button>

          ) : (            </>

            <>          ) : (

              <a href="/browse" style={{ color: 'white', textDecoration: 'none', padding: '8px 12px' }}>            <>

                Browse Items              <a href="/browse" style={{ color: 'white', textDecoration: 'none', padding: '8px 12px' }}>

              </a>                Browse Items

              <button              </a>

                onClick={onLogin}              <button

                style={{                onClick={onLogin}

                  padding: '8px 16px',                style={{

                  backgroundColor: 'transparent',                  padding: '8px 16px',

                  color: 'white',                  backgroundColor: 'transparent',

                  border: '1px solid white',                  color: 'white',

                  borderRadius: '4px',                  border: '1px solid white',

                  cursor: 'pointer',                  borderRadius: '4px',

                  fontSize: '14px'                  cursor: 'pointer',

                }}                  fontSize: '14px'

              >                }}

                Login              >

              </button>                Login

              <button              </button>

                onClick={onRegister}              <button

                style={{                onClick={onRegister}

                  padding: '8px 16px',                style={{

                  backgroundColor: '#4caf50',                  padding: '8px 16px',

                  color: 'white',                  backgroundColor: '#4caf50',

                  border: 'none',                  color: 'white',

                  borderRadius: '4px',                  border: 'none',

                  cursor: 'pointer',                  borderRadius: '4px',

                  fontSize: '14px'                  cursor: 'pointer',

                }}                  fontSize: '14px'

              >                }}

                Register              >

              </button>                Register

            </>              </button>

          )}            </>

        </div>          )}

      </div>        </div>

    </nav>      </div>

  );    </nav>

};  );
};
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
  Person as PersonIcon,
  Notifications as NotificationsIcon,
  ExitToApp as LogoutIcon
} from '@mui/icons-material';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { categoryService } from '../services/categoryService';

export const Navbar: React.FC = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [categories, setCategories] = useState([]);
  
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await categoryService.getCategories();
        setCategories(response.data || []);
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
    navigate('/');
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      const searchParams = new URLSearchParams();
      searchParams.set('query', searchQuery);
      if (selectedCategory) {
        searchParams.set('categoryId', selectedCategory);
      }
      navigate(`/?${searchParams.toString()}`);
    }
  };

  const isOpen = Boolean(anchorEl);

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography
          variant="h6"
          component={Link}
          to="/"
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
                  <Link to="/dashboard" style={{ textDecoration: 'none', color: 'inherit' }}>
                    Dashboard
                  </Link>
                </MenuItem>
                <MenuItem onClick={handleUserMenuClose}>
                  <Link to="/my-items" style={{ textDecoration: 'none', color: 'inherit' }}>
                    My Items
                  </Link>
                </MenuItem>
                <MenuItem onClick={handleUserMenuClose}>
                  <Link to="/my-bookings" style={{ textDecoration: 'none', color: 'inherit' }}>
                    My Bookings
                  </Link>
                </MenuItem>
                <MenuItem onClick={handleUserMenuClose}>
                  <Link to="/profile" style={{ textDecoration: 'none', color: 'inherit' }}>
                    Profile
                  </Link>
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
                component={Link}
                to="/login"
                sx={{ mr: 1 }}
              >
                Login
              </Button>
              <Button
                variant="outlined"
                color="inherit"
                component={Link}
                to="/register"
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
*/