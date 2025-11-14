// API Base URL - will use relative URLs in production
const API_BASE = '/api';

// Current user state
let currentUser = null;
let allItems = [];
let categories = [];

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');

    if (token && user) {
        currentUser = JSON.parse(user);
        updateNavigation();
    }

    // Load initial data
    loadCategories();

    // Show home page by default
    const hash = window.location.hash.slice(1);
    if (hash) {
        showPage(hash);
    }
});

// Navigation
function showPage(pageName) {
    // Hide all pages
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });

    // Show requested page
    const page = document.getElementById(pageName + 'Page');
    if (page) {
        page.classList.add('active');

        // Load page-specific data
        if (pageName === 'browse') {
            loadItems();
        } else if (pageName === 'my-items') {
            if (!currentUser) {
                showPage('login');
                return;
            }
            loadMyItems();
        } else if (pageName === 'my-bookings') {
            if (!currentUser) {
                showPage('login');
                return;
            }
            loadMyBookings();
        } else if (pageName === 'add-item') {
            if (!currentUser) {
                showPage('login');
                return;
            }
            loadCategoriesForForm();
        }

        // Update URL hash
        window.location.hash = pageName;
    }
}

function updateNavigation() {
    if (currentUser) {
        document.getElementById('loginLink').style.display = 'none';
        document.getElementById('registerLink').style.display = 'none';
        document.getElementById('logoutLink').style.display = 'block';
        document.getElementById('myItemsLink').style.display = 'block';
        document.getElementById('myBookingsLink').style.display = 'block';
        document.getElementById('heroLoginBtn').style.display = 'none';
    } else {
        document.getElementById('loginLink').style.display = 'block';
        document.getElementById('registerLink').style.display = 'block';
        document.getElementById('logoutLink').style.display = 'none';
        document.getElementById('myItemsLink').style.display = 'none';
        document.getElementById('myBookingsLink').style.display = 'none';
        document.getElementById('heroLoginBtn').style.display = 'inline-block';
    }
}

// API Helper Functions
async function apiCall(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(API_BASE + endpoint, {
            ...options,
            headers
        });

        if (response.status === 401) {
            logout();
            throw new Error('Session expired. Please login again.');
        }

        if (response.status === 204) {
            return null;
        }

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || data.error || 'Request failed');
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Auth Functions
async function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');

    try {
        const response = await apiCall('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });

        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));
        currentUser = response.user;

        updateNavigation();
        showPage('browse');

        // Clear form
        document.getElementById('loginForm').reset();
        errorDiv.classList.remove('show');
    } catch (error) {
        errorDiv.textContent = error.message;
        errorDiv.classList.add('show');
    }
}

async function handleRegister(event) {
    event.preventDefault();

    const username = document.getElementById('registerUsername').value;
    const email = document.getElementById('registerEmail').value;
    const name = document.getElementById('registerName').value;
    const phone = document.getElementById('registerPhone').value;
    const address = document.getElementById('registerAddress').value;
    const password = document.getElementById('registerPassword').value;
    const errorDiv = document.getElementById('registerError');

    try {
        await apiCall('/auth/register', {
            method: 'POST',
            body: JSON.stringify({
                username,
                email,
                name,
                phoneNumber: phone,
                address,
                password
            })
        });

        // Auto login after registration
        const loginResponse = await apiCall('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });

        localStorage.setItem('token', loginResponse.token);
        localStorage.setItem('user', JSON.stringify(loginResponse.user));
        currentUser = loginResponse.user;

        updateNavigation();
        showPage('browse');

        // Clear form
        document.getElementById('registerForm').reset();
        errorDiv.classList.remove('show');
    } catch (error) {
        errorDiv.textContent = error.message;
        errorDiv.classList.add('show');
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    currentUser = null;
    updateNavigation();
    showPage('home');
}

// Category Functions
async function loadCategories() {
    try {
        categories = await apiCall('/categories');

        // Update category filter
        const categoryFilter = document.getElementById('categoryFilter');
        if (categoryFilter) {
            categoryFilter.innerHTML = '<option value="">All Categories</option>';
            categories.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = cat.name;
                categoryFilter.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Failed to load categories:', error);
    }
}

function loadCategoriesForForm() {
    const itemCategory = document.getElementById('itemCategory');
    if (itemCategory) {
        itemCategory.innerHTML = '<option value="">Select a category</option>';
        categories.forEach(cat => {
            const option = document.createElement('option');
            option.value = cat.id;
            option.textContent = cat.name;
            itemCategory.appendChild(option);
        });
    }
}

// Items Functions
async function loadItems() {
    const grid = document.getElementById('itemsGrid');
    grid.innerHTML = '<div class="loading">Loading items...</div>';

    try {
        const categoryId = document.getElementById('categoryFilter').value;
        let endpoint = '/items';
        if (categoryId) {
            endpoint += `/category/${categoryId}`;
        }

        allItems = await apiCall(endpoint);
        displayItems(allItems, grid);
    } catch (error) {
        grid.innerHTML = '<div class="loading">Failed to load items</div>';
        console.error('Failed to load items:', error);
    }
}

function displayItems(items, container) {
    if (items.length === 0) {
        container.innerHTML = '<div class="loading">No items found</div>';
        return;
    }

    container.innerHTML = items.map(item => `
        <div class="item-card" onclick="showItemDetail('${item.id}')">
            <img src="${item.imageUrl || 'https://via.placeholder.com/280x200?text=No+Image'}"
                 alt="${item.name}"
                 class="item-image"
                 onerror="this.src='https://via.placeholder.com/280x200?text=No+Image'">
            <div class="item-content">
                <span class="item-category">${getCategoryName(item.categoryId)}</span>
                <span class="item-status ${item.availability ? 'available' : 'unavailable'}">
                    ${item.availability ? 'Available' : 'Unavailable'}
                </span>
                <div class="item-name">${item.name}</div>
                <div class="item-description">${item.description}</div>
                <div class="item-price">$${item.pricePerDay}/day</div>
            </div>
        </div>
    `).join('');
}

function getCategoryName(categoryId) {
    const category = categories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
}

function searchItems() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const filtered = allItems.filter(item =>
        item.name.toLowerCase().includes(searchTerm) ||
        item.description.toLowerCase().includes(searchTerm)
    );
    displayItems(filtered, document.getElementById('itemsGrid'));
}

async function loadMyItems() {
    const grid = document.getElementById('myItemsGrid');
    grid.innerHTML = '<div class="loading">Loading your items...</div>';

    try {
        const items = await apiCall(`/items/owner/${currentUser.id}`);

        if (items.length === 0) {
            grid.innerHTML = '<div class="loading">You haven\'t added any items yet</div>';
            return;
        }

        grid.innerHTML = items.map(item => `
            <div class="item-card">
                <img src="${item.imageUrl || 'https://via.placeholder.com/280x200?text=No+Image'}"
                     alt="${item.name}"
                     class="item-image"
                     onerror="this.src='https://via.placeholder.com/280x200?text=No+Image'">
                <div class="item-content">
                    <span class="item-category">${getCategoryName(item.categoryId)}</span>
                    <span class="item-status ${item.availability ? 'available' : 'unavailable'}">
                        ${item.availability ? 'Available' : 'Unavailable'}
                    </span>
                    <div class="item-name">${item.name}</div>
                    <div class="item-description">${item.description}</div>
                    <div class="item-price">$${item.pricePerDay}/day</div>
                    <button onclick="toggleItemAvailability('${item.id}', ${item.availability})" class="btn btn-secondary">
                        ${item.availability ? 'Mark Unavailable' : 'Mark Available'}
                    </button>
                    <button onclick="deleteItem('${item.id}')" class="btn btn-danger">Delete</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        grid.innerHTML = '<div class="loading">Failed to load your items</div>';
        console.error('Failed to load my items:', error);
    }
}

async function handleAddItem(event) {
    event.preventDefault();

    const name = document.getElementById('itemName').value;
    const description = document.getElementById('itemDescription').value;
    const categoryId = document.getElementById('itemCategory').value;
    const pricePerDay = parseFloat(document.getElementById('itemPrice').value);
    const imageUrl = document.getElementById('itemImage').value;
    const errorDiv = document.getElementById('addItemError');

    try {
        await apiCall('/items', {
            method: 'POST',
            body: JSON.stringify({
                name,
                description,
                categoryId,
                pricePerDay,
                imageUrl: imageUrl || null,
                ownerId: currentUser.id
            })
        });

        document.getElementById('addItemForm').reset();
        errorDiv.classList.remove('show');
        showPage('my-items');
    } catch (error) {
        errorDiv.textContent = error.message;
        errorDiv.classList.add('show');
    }
}

async function toggleItemAvailability(itemId, currentAvailability) {
    try {
        await apiCall(`/items/${itemId}/availability`, {
            method: 'PATCH',
            body: JSON.stringify({ availability: !currentAvailability })
        });
        loadMyItems();
    } catch (error) {
        alert('Failed to update item availability');
        console.error('Failed to toggle availability:', error);
    }
}

async function deleteItem(itemId) {
    if (!confirm('Are you sure you want to delete this item?')) {
        return;
    }

    try {
        await apiCall(`/items/${itemId}`, {
            method: 'DELETE'
        });
        loadMyItems();
    } catch (error) {
        alert('Failed to delete item');
        console.error('Failed to delete item:', error);
    }
}

// Item Detail
async function showItemDetail(itemId) {
    showPage('itemDetail');
    const content = document.getElementById('itemDetailContent');
    content.innerHTML = '<div class="loading">Loading item details...</div>';

    try {
        const item = await apiCall(`/items/${itemId}`);

        content.innerHTML = `
            <div class="item-detail">
                <img src="${item.imageUrl || 'https://via.placeholder.com/800x400?text=No+Image'}"
                     alt="${item.name}"
                     class="item-detail-image"
                     onerror="this.src='https://via.placeholder.com/800x400?text=No+Image'">
                <h1>${item.name}</h1>
                <div class="item-detail-info">
                    <span class="item-category">${getCategoryName(item.categoryId)}</span>
                    <span class="item-status ${item.availability ? 'available' : 'unavailable'}">
                        ${item.availability ? 'Available' : 'Unavailable'}
                    </span>
                </div>
                <div class="item-detail-section">
                    <h3>Description</h3>
                    <p>${item.description}</p>
                </div>
                <div class="item-detail-section">
                    <h3>Price</h3>
                    <p class="item-price">$${item.pricePerDay}/day</p>
                </div>
                ${currentUser && currentUser.id !== item.ownerId && item.availability ? `
                    <div class="item-detail-section">
                        <h3>Book this item</h3>
                        <form id="bookingForm" onsubmit="handleBooking(event, '${item.id}')">
                            <div class="form-group">
                                <label>Start Date</label>
                                <input type="date" id="bookingStartDate" required min="${new Date().toISOString().split('T')[0]}">
                            </div>
                            <div class="form-group">
                                <label>End Date</label>
                                <input type="date" id="bookingEndDate" required min="${new Date().toISOString().split('T')[0]}">
                            </div>
                            <div class="error-message" id="bookingError"></div>
                            <button type="submit" class="btn btn-primary">Book Now</button>
                            <button type="button" onclick="showPage('browse')" class="btn btn-secondary">Back to Browse</button>
                        </form>
                    </div>
                ` : `
                    <button onclick="showPage('browse')" class="btn btn-secondary">Back to Browse</button>
                `}
            </div>
        `;
    } catch (error) {
        content.innerHTML = '<div class="loading">Failed to load item details</div>';
        console.error('Failed to load item detail:', error);
    }
}

async function handleBooking(event, itemId) {
    event.preventDefault();

    const startDate = document.getElementById('bookingStartDate').value;
    const endDate = document.getElementById('bookingEndDate').value;
    const errorDiv = document.getElementById('bookingError');

    try {
        await apiCall('/bookings', {
            method: 'POST',
            body: JSON.stringify({
                itemId,
                borrowerId: currentUser.id,
                startDate,
                endDate
            })
        });

        alert('Booking request submitted successfully!');
        showPage('my-bookings');
    } catch (error) {
        errorDiv.textContent = error.message;
        errorDiv.classList.add('show');
    }
}

// Bookings
async function loadMyBookings() {
    const container = document.getElementById('bookingsList');
    container.innerHTML = '<div class="loading">Loading bookings...</div>';

    try {
        const bookings = await apiCall(`/bookings/borrower/${currentUser.id}`);

        if (bookings.length === 0) {
            container.innerHTML = '<div class="loading">You have no bookings yet</div>';
            return;
        }

        container.innerHTML = bookings.map(booking => `
            <div class="booking-card">
                <h3>${booking.itemName || 'Item'}</h3>
                <div class="booking-info">
                    <strong>Start Date:</strong> ${new Date(booking.startDate).toLocaleDateString()}
                </div>
                <div class="booking-info">
                    <strong>End Date:</strong> ${new Date(booking.endDate).toLocaleDateString()}
                </div>
                <div class="booking-info">
                    <strong>Total Price:</strong> $${booking.totalPrice}
                </div>
                <span class="booking-status ${booking.status}">${booking.status}</span>
                ${booking.status === 'PENDING' ? `
                    <button onclick="cancelBooking('${booking.id}')" class="btn btn-danger">Cancel Booking</button>
                ` : ''}
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = '<div class="loading">Failed to load bookings</div>';
        console.error('Failed to load bookings:', error);
    }
}

async function cancelBooking(bookingId) {
    if (!confirm('Are you sure you want to cancel this booking?')) {
        return;
    }

    try {
        await apiCall(`/bookings/${bookingId}/cancel`, {
            method: 'PATCH'
        });
        loadMyBookings();
    } catch (error) {
        alert('Failed to cancel booking');
        console.error('Failed to cancel booking:', error);
    }
}
