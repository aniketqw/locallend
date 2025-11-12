# TOGGLE FIX: Prioritize Specific Status Setting

## 🎯 **Issue Fixed**

**Problem:** Clicking "Make Unavailable" was using the toggle endpoint first, which just flips the status regardless of intent.

**Result:** Item that was already UNAVAILABLE would become AVAILABLE when clicking "Make Unavailable" (opposite of expected behavior).

---

## 🔧 **Solution Applied**

### **Changed Endpoint Priority Order:**

**BEFORE (Wrong):**
1. Try `/api/items/{id}/toggle-availability` (just flips status)
2. Fallback to `/api/items/{id}/availability` (sets specific status)

**AFTER (Fixed):**
1. Try `/api/items/{id}/availability` (sets specific status) ✅ **PREFERRED**
2. Fallback to `/api/items/{id}/toggle-availability` (just flips status)

### **Why This Fixes It:**

- **`/availability` endpoint:** Lets us set EXACTLY the status we want (AVAILABLE or UNAVAILABLE)
- **`/toggle-availability` endpoint:** Just flips whatever the current status is

By using `/availability` first, we ensure:
- ✅ "Make Unavailable" → Always sets to UNAVAILABLE
- ✅ "Make Available" → Always sets to AVAILABLE
- ✅ No unwanted toggling behavior

---

## 📁 **Files Updated:**

### **1. DashboardPage.tsx**
```typescript
// Now tries /availability first (with specific status)
endpointUsed = `/api/items/${itemId}/availability`;
directResponse = await fetch(url, {
  method: 'PATCH',
  body: JSON.stringify({ status: newStatus })  // Specific status!
});

// Only uses toggle as fallback if availability doesn't exist
if (directResponse.status === 404) {
  endpointUsed = `/api/items/${itemId}/toggle-availability`;
  // ...
}
```

### **2. itemService.ts**
```typescript
updateItemStatus: async (itemId: string, status: ItemStatus, userId: string) => {
  // Try availability endpoint first (specific status setting)
  try {
    return await api.patch(`/api/items/${itemId}/availability`, { status });
  } catch (error) {
    // Fallback to toggle only if availability doesn't exist
    if (error?.response?.status === 404) {
      return await api.patch(`/api/items/${itemId}/toggle-availability`);
    }
    throw error;
  }
}
```

---

## 🎯 **Expected Behavior Now:**

### **When clicking "Make Unavailable":**
1. Frontend sends: `PATCH /api/items/{id}/availability` with `{"status":"UNAVAILABLE"}`
2. Backend sets item status to UNAVAILABLE (regardless of current status)
3. Button changes to "Make Available"
4. Status badge shows red/unavailable

### **When clicking "Make Available":**
1. Frontend sends: `PATCH /api/items/{id}/availability` with `{"status":"AVAILABLE"}`  
2. Backend sets item status to AVAILABLE (regardless of current status)
3. Button changes to "Make Unavailable"
4. Status badge shows green/available

---

## 🧪 **Test It:**

1. **Find an item that shows "AVAILABLE"**
2. **Click "Make Unavailable"** 
   - Should change to UNAVAILABLE and button becomes "Make Available"
3. **Click "Make Available"** 
   - Should change to AVAILABLE and button becomes "Make Unavailable"  
4. **Repeat several times** - should work predictably every time

**No more unwanted toggling!** The button will now do exactly what it says.