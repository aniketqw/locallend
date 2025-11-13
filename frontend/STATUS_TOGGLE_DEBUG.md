# Item Status Toggle - Debugging Guide

## 🔍 Issue Analysis

The "Make Unavailable" button is not working. Based on the Integration Guide, here are potential causes and debugging steps:

## 📋 Integration Guide Requirements

### **PATCH /api/items/{itemId}/status**
```
Headers:
- Authorization: Bearer <jwt_token>
- X-User-Id: <user_id>
- Content-Type: application/json

Request Body:
{
  "status": "AVAILABLE" // AVAILABLE|UNAVAILABLE|BORROWED
}

Response (200): Returns updated item object
```

## 🐛 Potential Issues & Debugging

### 1. **Backend Connectivity**
**Added**: Health check to verify backend is reachable
```javascript
const healthCheck = await fetch('http://localhost:8080/api/items');
```

### 2. **Authentication Issues**
**Added**: Token validation and preview
```javascript
console.log('🔐 Token exists:', !!token);
console.log('🔐 Token preview:', token.substring(0, 20) + '...');
```

### 3. **Request Format Issues**
**Added**: Detailed request logging
```javascript
console.log('📝 Request body:', JSON.stringify({ status: newStatus }));
console.log('📝 Request headers:', headers);
```

### 4. **Status Validation Issues**
**Added**: Client-side validation
```javascript
// Validate current status
if (!['AVAILABLE', 'UNAVAILABLE', 'BORROWED'].includes(currentStatus)) {
  alert('Invalid current item status');
  return;
}

// Prevent changing borrowed items
if (currentStatus === 'BORROWED') {
  alert('Cannot change status of borrowed items');
  return;
}
```

### 5. **Backend Response Issues**
**Added**: Detailed error parsing
```javascript
// Parse different error types
if (directResponse.status === 400) {
  errorMessage = 'Bad Request: Invalid status value or request format';
} else if (directResponse.status === 403) {
  errorMessage = 'Forbidden: You are not the owner of this item';
} else if (directResponse.status === 404) {
  errorMessage = 'Not Found: Item not found or endpoint not available';
}
```

## 🧪 Enhanced Debugging Steps

### **Step 1: Check Browser Console**
1. Open Developer Tools → Console
2. Click "Make Unavailable" button
3. Look for these logs:
   - `🏥 Backend health check for items endpoint: 200`
   - `🔐 Token exists: true`
   - `📝 Status change: AVAILABLE → UNAVAILABLE`
   - `📡 Direct response status: 200`

### **Step 2: Check Network Tab**
1. Open Developer Tools → Network tab
2. Filter by "Fetch/XHR"
3. Click the status toggle button
4. Look for PATCH request to `/api/items/{itemId}/status`
5. Check:
   - Request headers (Authorization, X-User-Id)
   - Request body: `{"status":"UNAVAILABLE"}`
   - Response status and body

### **Step 3: Test Backend Directly**
Test the backend endpoint directly:
```bash
# Replace with actual values
curl -X PATCH http://localhost:8080/api/items/{ITEM_ID}/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "X-User-Id: {USER_ID}" \
  -d '{"status":"UNAVAILABLE"}'
```

## 🔧 Common Error Scenarios

### **400 Bad Request**
**Possible Causes:**
- Invalid status value (not AVAILABLE/UNAVAILABLE/BORROWED)
- Missing Content-Type header
- Malformed JSON body
- Invalid item ID format

**Debug**: Check request body format and headers

### **401 Unauthorized**
**Possible Causes:**
- Missing Authorization header
- Invalid/expired JWT token
- Token format incorrect

**Debug**: Check token existence and format

### **403 Forbidden**
**Possible Causes:**
- User is not the owner of the item
- Missing X-User-Id header
- X-User-Id doesn't match token user

**Debug**: Verify user ownership and header presence

### **404 Not Found**
**Possible Causes:**
- Item doesn't exist
- Invalid item ID
- Backend endpoint not available
- Wrong API URL

**Debug**: Verify item exists and API URL is correct

## 🎯 Alternative Implementation

If the direct approach fails, try using the service layer properly:

```typescript
// Alternative: Use service layer with better error handling
const handleToggleStatusAlternative = async (itemId: string, currentStatus: string) => {
  try {
    const newStatus = currentStatus === 'AVAILABLE' ? 'UNAVAILABLE' : 'AVAILABLE';
    
    // Use the item service
    const updatedItem = await itemService.updateItemStatus(itemId, newStatus, user.id);
    
    // Update local state
    setMyItems(prevItems => 
      prevItems.map(item => 
        item.id === itemId ? { ...item, status: updatedItem.status } : item
      )
    );
    
    alert(`✅ Item status changed to ${updatedItem.status}`);
  } catch (error: any) {
    console.error('Service layer error:', error);
    
    // Extract detailed error information
    let errorMessage = 'Failed to update item status';
    if (error?.response?.status === 400) {
      errorMessage = 'Invalid request: Check item status and permissions';
    } else if (error?.response?.status === 403) {
      errorMessage = 'You are not authorized to modify this item';
    } else if (error?.response?.data?.message) {
      errorMessage = error.response.data.message;
    }
    
    alert(`❌ ${errorMessage}`);
  }
};
```

## 🔍 Backend Validation Checklist

### **Item Requirements:**
- ✅ Item must exist
- ✅ Item must be owned by current user
- ✅ Item must not be currently borrowed (status !== 'BORROWED')

### **Request Requirements:**
- ✅ Valid JWT token in Authorization header
- ✅ Correct X-User-Id header
- ✅ Valid status value (AVAILABLE/UNAVAILABLE)
- ✅ Proper JSON format

### **Permission Requirements:**
- ✅ User must be authenticated
- ✅ User must own the item
- ✅ No active bookings preventing status change

## 📱 Testing Instructions

### **Test Valid Status Change:**
1. Find an item with status "AVAILABLE"
2. Click "⏸️ Make Unavailable"
3. Should see success message
4. Item status should change to "UNAVAILABLE"
5. Button should change to "▶️ Make Available"

### **Test Error Scenarios:**
1. Try changing status of borrowed item
2. Try with network disconnected
3. Try with invalid authentication

### **Verify UI Updates:**
1. Status badge color should change
2. Button text should update
3. Local state should reflect change immediately

## 🎯 Expected Behavior

### **Success Flow:**
1. User clicks status toggle button
2. Confirmation (optional)
3. API call with proper headers
4. Backend validates ownership and status
5. Backend updates item status
6. Frontend receives updated item
7. UI updates immediately
8. Success message shown

### **Error Flow:**
1. User clicks status toggle button
2. API call fails with specific error
3. Detailed error message shown
4. UI remains in original state
5. User can retry or fix issue

The enhanced debugging should now pinpoint exactly where the status toggle is failing!