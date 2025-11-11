# ============================================================================
# LocalLend API Testing - CURL Commands
# ============================================================================
# For PowerShell users, curl is aliased to Invoke-WebRequest
# These commands use proper curl syntax
# ============================================================================

BASE_URL="http://localhost:8080"

# ============================================================================
# 1. USER ENDPOINTS
# ============================================================================

# 1.1 Create User 1
curl -X POST $BASE_URL/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'
# Save the returned 'id' as USER_ID_1

# 1.2 Create User 2
curl -X POST $BASE_URL/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Smith","email":"jane@example.com"}'
# Save the returned 'id' as USER_ID_2

# 1.3 Get All Users
curl -X GET $BASE_URL/api/users

# ============================================================================
# 2. CATEGORY ENDPOINTS
# ============================================================================

# 2.1 Create Category - Power Tools
curl -X POST $BASE_URL/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Power Tools","description":"Electric and battery-powered tools"}'
# Save the returned 'data.id' as CAT_ID_1

# 2.2 Create Category - Hand Tools
curl -X POST $BASE_URL/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Hand Tools","description":"Manual tools"}'
# Save the returned 'data.id' as CAT_ID_2

# 2.3 Create Category - Garden Equipment
curl -X POST $BASE_URL/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Garden Equipment","description":"Outdoor and garden tools"}'
# Save the returned 'data.id' as CAT_ID_3

# 2.4 Get All Categories
curl -X GET $BASE_URL/api/categories

# 2.5 Get All Categories (sorted by popular)
curl -X GET "$BASE_URL/api/categories?sort=popular"

# 2.6 Get Category by ID
curl -X GET $BASE_URL/api/categories/{CAT_ID_1}

# 2.7 Get Root Categories
curl -X GET $BASE_URL/api/categories/root

# 2.8 Search Categories
curl -X GET "$BASE_URL/api/categories/search?q=tool"

# 2.9 Get Popular Categories
curl -X GET "$BASE_URL/api/categories/popular?min_items=1"

# 2.10 Create Subcategory
curl -X POST $BASE_URL/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Drills","description":"Power drills","parent_category_id":"{CAT_ID_1}"}'

# 2.11 Get Subcategories
curl -X GET $BASE_URL/api/categories/{CAT_ID_1}/subcategories

# 2.12 Update Category Status
curl -X PATCH "$BASE_URL/api/categories/{CAT_ID_1}/status?is_active=false"

# ============================================================================
# 3. ITEM ENDPOINTS
# ============================================================================

# 3.1 Create Item 1 - Cordless Drill (owned by User 1)
curl -X POST $BASE_URL/api/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: {USER_ID_1}" \
  -d '{
    "name": "Cordless Drill",
    "description": "18V battery-powered drill with charger",
    "categoryId": "{CAT_ID_1}",
    "deposit": 25.0,
    "images": ["https://example.com/drill1.jpg"],
    "condition": "GOOD"
  }'
# Save the returned 'id' as ITEM_ID_1

# 3.2 Create Item 2 - Hammer (owned by User 1)
curl -X POST $BASE_URL/api/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: {USER_ID_1}" \
  -d '{
    "name": "Claw Hammer",
    "description": "16oz steel hammer",
    "categoryId": "{CAT_ID_2}",
    "deposit": 5.0,
    "condition": "EXCELLENT"
  }'
# Save the returned 'id' as ITEM_ID_2

# 3.3 Create Item 3 - Lawn Mower (owned by User 2)
curl -X POST $BASE_URL/api/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: {USER_ID_2}" \
  -d '{
    "name": "Electric Lawn Mower",
    "description": "Corded electric lawn mower",
    "categoryId": "{CAT_ID_3}",
    "deposit": 50.0,
    "condition": "GOOD"
  }'
# Save the returned 'id' as ITEM_ID_3

# 3.4 Get All Available Items (paginated)
curl -X GET "$BASE_URL/api/items?page=0&size=10"

# 3.5 Get All Items (sorted by createdAt descending)
curl -X GET "$BASE_URL/api/items?page=0&size=10&sortBy=createdAt&sortDir=desc"

# 3.6 Get Item by ID
curl -X GET $BASE_URL/api/items/{ITEM_ID_1}

# 3.7 Search Items
curl -X GET "$BASE_URL/api/items/search?q=drill&page=0&size=10"

# 3.8 Get Items by Category
curl -X GET "$BASE_URL/api/items/category/{CAT_ID_1}?page=0&size=10"

# 3.9 Get Items by Owner
curl -X GET "$BASE_URL/api/items/owner/{USER_ID_1}?page=0&size=10"

# 3.10 Get Current User's Items
curl -X GET "$BASE_URL/api/items/my-items?page=0&size=10" \
  -H "X-User-Id: {USER_ID_1}"

# 3.11 Update Item (partial update)
curl -X PUT $BASE_URL/api/items/{ITEM_ID_1} \
  -H "Content-Type: application/json" \
  -H "X-User-Id: {USER_ID_1}" \
  -d '{
    "name": "Updated Cordless Drill",
    "deposit": 30.0,
    "condition": "EXCELLENT"
  }'

# 3.12 Toggle Item Availability
curl -X PATCH $BASE_URL/api/items/{ITEM_ID_1}/toggle-availability \
  -H "X-User-Id: {USER_ID_1}"

# 3.13 Set Item Availability
curl -X PATCH $BASE_URL/api/items/{ITEM_ID_1}/availability \
  -H "Content-Type: application/json" \
  -H "X-User-Id: {USER_ID_1}" \
  -d '{"isAvailable": false}'

# 3.14 Delete Item (soft delete)
curl -X DELETE $BASE_URL/api/items/{ITEM_ID_2} \
  -H "X-User-Id: {USER_ID_1}"

# ============================================================================
# 4. ERROR TESTING
# ============================================================================

# 4.1 Test 404 - Get Non-existent Item
curl -X GET $BASE_URL/api/items/nonexistentid123

# 4.2 Test 400 - Create Item with Missing Field
curl -X POST $BASE_URL/api/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: {USER_ID_1}" \
  -d '{"description":"Missing name field"}'

# 4.3 Test 400 - Invalid Category
curl -X POST $BASE_URL/api/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: {USER_ID_1}" \
  -d '{
    "name": "Test Item",
    "description": "Test",
    "categoryId": "invalidcategoryid123",
    "deposit": 10.0
  }'

# 4.4 Test 403 - Try to update someone else's item
curl -X PUT $BASE_URL/api/items/{ITEM_ID_1} \
  -H "Content-Type: application/json" \
  -H "X-User-Id: {USER_ID_2}" \
  -d '{"name":"Hacked Item"}'

# 4.5 Test 400 - Duplicate Category
curl -X POST $BASE_URL/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Power Tools","description":"Duplicate"}'

# ============================================================================
# PRETTY PRINT JSON (add | jq to any command)
# ============================================================================

# Example:
curl -X GET $BASE_URL/api/items | jq '.'

# Filter specific fields:
curl -X GET $BASE_URL/api/items | jq '.content[] | {id, name, deposit}'

# Count items:
curl -X GET $BASE_URL/api/items | jq '.totalElements'

# ============================================================================
# NOTES
# ============================================================================
# 
# 1. Replace {USER_ID_1}, {CAT_ID_1}, {ITEM_ID_1} etc. with actual IDs
# 2. For PowerShell, use Invoke-RestMethod instead or see QUICK_TEST.ps1
# 3. Install jq for JSON formatting: https://stedolan.github.io/jq/
# 4. Add -v flag to curl for verbose output (see headers, status codes)
# 5. Add -i flag to curl to see response headers
#
# ============================================================================
