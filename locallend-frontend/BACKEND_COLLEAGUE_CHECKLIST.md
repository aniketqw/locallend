# Backend Colleague Diagnostic Checklist
## Item Status Update API Issue: "Make Unavailable" Button Not Working

### 🚨 **Error Reported:**
```
Status: 500 Internal Server Error
Message: "No static resource api/items/690e7cbd134f602b79d356df/status"
Timestamp: 2025-11-12T13:28:08.316908852
```

### 🎯 **Expected Frontend Request:**
```http
# Option 1: Toggle endpoint (simpler)
PATCH /api/items/{itemId}/toggle-availability
Content-Type: application/json
Authorization: Bearer {jwt_token}
X-User-Id: {user_id}
# No body needed - just toggles between AVAILABLE/UNAVAILABLE

# Option 2: Availability endpoint (with status)
PATCH /api/items/{itemId}/availability
Content-Type: application/json
Authorization: Bearer {jwt_token}
X-User-Id: {user_id}

{
  "status": "UNAVAILABLE"
}
```

---

## 🔍 **Backend Checks Required**

### **1. API Server Status**
**Question**: Is your Spring Boot API server actually running and serving API endpoints?

**Check:**
```bash
# Verify what's running on port 8080
curl -I http://localhost:8080/api/categories
# Should return: HTTP/1.1 200 OK (or 401/403)
# Should NOT return: HTML content or "No static resource"

# Check Spring Boot actuator health
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

**If getting static resource errors**: Your API server is not running properly.

---

### **2. API Endpoint Configuration**
**Question**: Do you have the PATCH status endpoint properly configured?

**Required Controller Methods:**
```java
// Option 1: Toggle endpoint
@PatchMapping("/api/items/{itemId}/toggle-availability")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<Item> toggleItemAvailability(
    @PathVariable String itemId,
    @RequestHeader("X-User-Id") String userId,
    Authentication auth
) {
    // Toggle between AVAILABLE/UNAVAILABLE
}

// Option 2: Set specific availability
@PatchMapping("/api/items/{itemId}/availability")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<Item> setItemAvailability(
    @PathVariable String itemId,
    @RequestBody Map<String, String> statusUpdate,
    @RequestHeader("X-User-Id") String userId,
    Authentication auth
) {
    // Set specific status
}
```

**Check:**
- ✅ `@PatchMapping` annotation present
- ✅ Path matches `/api/items/{itemId}/toggle-availability` OR `/api/items/{itemId}/availability`
- ✅ Toggle endpoint needs no body, availability endpoint accepts JSON body
- ✅ Handles X-User-Id header
- ✅ Proper authorization checks

---

### **3. Request Routing**
**Question**: Are API requests being routed to your controllers or to static resources?

**Check Application Properties:**
```properties
# Should NOT have conflicting static resource mappings
spring.mvc.static-path-pattern=/static/**
# Should NOT map /api/** to static resources

# Ensure proper base path
server.servlet.context-path=  # Should be empty or /
```

**Test Routing:**
```bash
# Test if /api/* routes to controllers
curl -X GET http://localhost:8080/api/items
# Should return: JSON response or 401 auth error
# Should NOT return: "No static resource" error
```

---

### **4. CORS Configuration**
**Question**: Is CORS properly configured for PATCH requests from frontend?

**Required Configuration:**
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        return source;
    }
}
```

**Check:**
- ✅ PATCH method allowed
- ✅ Authorization header allowed
- ✅ X-User-Id header allowed
- ✅ Frontend origin (http://localhost:5173) allowed

---

### **5. Authentication & Authorization**
**Question**: Is JWT token validation working for the status endpoint?

**Test Authentication:**
```bash
# Test with valid token (replace with actual values)
curl -X PATCH http://localhost:8080/api/items/{ITEM_ID}/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "X-User-Id: {USER_ID}" \
  -d '{"status":"UNAVAILABLE"}'

# Expected responses:
# 200: Success with updated item JSON
# 400: Bad request (invalid status value)
# 401: Invalid/expired token
# 403: User doesn't own the item
# 404: Item not found
```

**Check:**
- ✅ JWT filter processes requests properly
- ✅ X-User-Id header is read correctly
- ✅ User ownership validation works
- ✅ Security context is set properly

---

### **6. Item Model & Repository**
**Question**: Does your item update logic handle status changes correctly?

**Expected Item Model Fields:**
```java
public class Item {
    private String id;
    private String name;
    private String description;
    private ItemStatus status; // AVAILABLE, UNAVAILABLE, BORROWED
    private String ownerId;
    // ... other fields
}

public enum ItemStatus {
    AVAILABLE, UNAVAILABLE, BORROWED
}
```

**Repository Method:**
```java
@Query("UPDATE Item i SET i.status = :status WHERE i.id = :itemId AND i.ownerId = :ownerId")
int updateItemStatus(@Param("itemId") String itemId, 
                    @Param("status") ItemStatus status, 
                    @Param("ownerId") String ownerId);
```

**Check:**
- ✅ Item entity has status field
- ✅ Status enum matches frontend values
- ✅ Update query works correctly
- ✅ Owner validation in query/service

---

### **7. Error Handling**
**Question**: Are you properly handling and returning API errors?

**Expected Error Responses:**
```java
// 400 Bad Request
return ResponseEntity.badRequest()
    .body(new ErrorResponse("Invalid status value"));

// 403 Forbidden  
return ResponseEntity.status(HttpStatus.FORBIDDEN)
    .body(new ErrorResponse("You don't own this item"));

// 404 Not Found
return ResponseEntity.notFound().build();
```

**Check:**
- ✅ Proper HTTP status codes returned
- ✅ JSON error responses (not HTML)
- ✅ No 500 errors for valid requests
- ✅ Meaningful error messages

---

## 🧪 **Backend Testing Checklist**

### **Test 1: Endpoint Accessibility**
```bash
curl -v http://localhost:8080/api/items
# Should get JSON response or 401, NOT "No static resource"
```

### **Test 2: PATCH Method Support**
```bash
# Test toggle endpoint
curl -X PATCH http://localhost:8080/api/items/test/toggle-availability \
  -H "Content-Type: application/json"
# Should get 401/403, NOT "Method not allowed" or static resource error

# Test availability endpoint
curl -X PATCH http://localhost:8080/api/items/test/availability \
  -H "Content-Type: application/json" \
  -d '{"status":"UNAVAILABLE"}'
# Should get 401/403, NOT "Method not allowed" or static resource error
```

### **Test 3: With Valid Authentication**
```bash
# Test toggle endpoint with real auth
curl -X PATCH http://localhost:8080/api/items/{REAL_ITEM_ID}/toggle-availability \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {REAL_TOKEN}" \
  -H "X-User-Id: {REAL_USER_ID}"
# Should return 200 with updated item JSON

# Test availability endpoint with real auth
curl -X PATCH http://localhost:8080/api/items/{REAL_ITEM_ID}/availability \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {REAL_TOKEN}" \
  -H "X-User-Id: {REAL_USER_ID}" \
  -d '{"status":"UNAVAILABLE"}'
# Should return 200 with updated item JSON
```

---

## 🔴 **Red Flags (Backend Issues)**

### **If you see any of these, it's a backend problem:**

1. **Static Resource Errors** - API server not running properly
2. **HTML Responses** - Wrong server or routing issue
3. **Method Not Allowed** - PATCH not configured
4. **CORS Errors** - Frontend can't reach backend
5. **500 Errors on Valid Requests** - Backend code bugs

### **Backend Logs to Check:**
```bash
# Check for these in backend logs:
docker logs {backend-container-name}

# Look for:
✅ "Started Application in X seconds" - Server started
✅ "Mapping servlet: 'dispatcherServlet' to [/]" - Routing configured  
❌ Static resource mapping conflicts
❌ Authentication/authorization errors
❌ Database connection issues
```

---

## 🟢 **Green Flags (Frontend Issues)**

### **If backend works correctly, check frontend:**

1. **API calls return proper JSON responses**
2. **Authentication works with curl but not frontend**
3. **Different behavior between direct API calls and frontend**
4. **Network/CORS issues only from browser**

---

## 📊 **Expected Working Flow**

### **Backend Should:**
1. ✅ Receive PATCH request to `/api/items/{id}/status`
2. ✅ Validate JWT token and extract user info
3. ✅ Check X-User-Id header matches token user
4. ✅ Verify user owns the item with given ID
5. ✅ Validate status value (AVAILABLE/UNAVAILABLE)
6. ✅ Update item status in database
7. ✅ Return 200 with updated item JSON

### **Backend Should NOT:**
1. ❌ Return "No static resource" errors
2. ❌ Treat API calls as file requests
3. ❌ Return HTML error pages
4. ❌ Give 500 errors for properly formatted requests

---

## 🎯 **Quick Diagnosis**

**Run these commands to check if backend is working:**
```bash
# Test toggle endpoint
curl -X PATCH http://localhost:8080/api/items/test123/toggle-availability \
  -H "Content-Type: application/json"

# Test availability endpoint
curl -X PATCH http://localhost:8080/api/items/test123/availability \
  -H "Content-Type: application/json" \
  -d '{"status":"UNAVAILABLE"}'
```

**Expected Results:**
- ✅ **401 Unauthorized** = Backend API working, auth required ✓
- ✅ **403 Forbidden** = Backend working, permission issue ✓  
- ✅ **400 Bad Request** = Backend working, validation issue ✓
- ❌ **"No static resource"** = Backend API not running ✗
- ❌ **Connection refused** = Backend not running ✗
- ❌ **HTML error page** = Wrong server running ✗

**If you get 401/403/400 = Backend is working correctly!**
**If you get static resource errors = Backend setup issue!**