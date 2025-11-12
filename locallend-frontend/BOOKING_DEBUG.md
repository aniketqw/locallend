# Booking 400 Bad Request - Comprehensive Debugging Guide

## Current Issue Analysis

You're getting a 400 Bad Request when trying to create a booking. Based on the integration guide, here are the most likely causes and fixes:

## 🔍 Debugging Steps Added

### 1. **Backend Connectivity Check**
Added a health check to verify backend is accessible:
```javascript
const healthCheck = await fetch('http://localhost:8080/api/categories');
```

### 2. **Enhanced Error Logging**
Added comprehensive logging to see exactly what's being sent and received:
- Request headers and body
- Response status and detailed error messages
- Token verification

### 3. **Direct API Call Test**
Bypassing the service layer to test the raw API directly for clearer error messages.

## 📋 Integration Guide Requirements Check

### Required Headers (Must Have All):
```
Content-Type: application/json
Authorization: Bearer <jwt_token>
X-User-Id: <borrower_id>
```

### Required Request Body (Exact Format):
```json
{
  "itemId": "string (required)",
  "startDate": "2025-11-10T10:00:00 (required, future date)",
  "endDate": "2025-11-13T10:00:00 (required, after start date)",
  "bookingNotes": "string (max 500 chars, optional)",
  "depositAmount": 100.0,
  "requestedDurationDays": 3,
  "acceptTerms": true (required, must be true)
}
```

### Backend Validation Rules:
1. **startDate**: Must be future date
2. **endDate**: Must be after startDate  
3. **acceptTerms**: Must be `true`
4. **itemId**: Must exist and be available
5. **X-User-Id**: Must match authenticated user
6. **bookingNotes**: Max 500 characters
7. **depositAmount**: Must be >= 0

## 🚨 Common 400 Bad Request Causes

### 1. **Date Format Issues**
**Problem**: Backend expects exact format `"2025-11-10T10:00:00"`
**Fixed**: Using custom formatter without timezone info

### 2. **Missing Required Fields**
**Check**: All required fields present?
- ✅ itemId
- ✅ startDate  
- ✅ endDate
- ✅ acceptTerms
- ✅ requestedDurationDays

### 3. **Header Issues**
**Check**: All required headers?
- ✅ Authorization: Bearer <token>
- ✅ X-User-Id: <user_id>
- ✅ Content-Type: application/json

### 4. **Data Type Issues**
**Check**: Correct data types?
- ✅ depositAmount: number
- ✅ acceptTerms: boolean
- ✅ requestedDurationDays: number

### 5. **Authentication Issues**
**Check**: Valid JWT token?
- Token exists in localStorage
- Token not expired
- User ID matches token

## 🧪 Testing Instructions

### Step 1: Check Browser Console
1. Open Developer Tools → Console
2. Try to create a booking
3. Look for these logs:
   - `🏥 Backend health check: 200` (should be OK)
   - `📝 Booking data to send:` (verify data format)
   - `🔐 Using token:` (verify token exists)
   - `📡 Direct response status:` (see exact HTTP status)
   - `📡 Direct response error body:` (see exact error message)

### Step 2: Verify Backend is Running
Run these commands:
```bash
# Check if backend container is running
docker ps | grep backend

# Check backend logs
docker logs <backend-container-name>

# Test backend directly
curl http://localhost:8080/api/categories
```

### Step 3: Test with Valid Data
Try booking with these exact values:
- **Start Date**: Tomorrow's date
- **End Date**: Day after tomorrow  
- **Accept Terms**: ✅ Checked
- **Notes**: Empty or short text
- **Deposit**: 0 or any positive number

## 🔧 Potential Backend Issues to Check

### 1. **Item Availability**
- Item must exist and be AVAILABLE
- Item owner cannot book their own item
- Item must not have conflicting bookings

### 2. **User Authentication**
- JWT token must be valid and not expired
- X-User-Id must match the authenticated user
- User must be active and not banned

### 3. **Date Validation**
- Start date must be in the future (not today)
- End date must be after start date
- Dates must be valid calendar dates

### 4. **Business Logic**
- User cannot book if they have pending bookings for same item
- Item deposit requirements met
- Category-specific booking rules

## 🐛 Common Error Messages & Solutions

### "Required request header 'X-User-Id' is not present"
**Fix**: User ID header missing
```javascript
headers: {
  'X-User-Id': user.id  // Make sure user.id is valid
}
```

### "startDate must be in the future"
**Fix**: Date validation issue
```javascript
// Ensure start date is at least tomorrow
const tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate() + 1);
```

### "acceptTerms must be true"
**Fix**: Terms not accepted
```javascript
acceptTerms: true  // Must be boolean true, not string
```

### "Item not found or not available"
**Fix**: Invalid item or item not available
- Check if item exists
- Check if item status is AVAILABLE
- Check if user owns the item (cannot book own items)

## 🎯 Next Steps

1. **Run the updated code** and check browser console logs
2. **Copy the exact error message** from console
3. **Check if backend is accessible** via the health check
4. **Verify the request payload** matches integration guide exactly
5. **Test with minimal data** (no optional fields) first

The enhanced logging should now show exactly what's causing the 400 error!