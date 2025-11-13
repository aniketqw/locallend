# LocalLend Platform - Use Case Documentation

## Table of Contents
1. [Overview](#overview)
2. [Actors](#actors)
3. [Use Case Diagram Description](#use-case-diagram-description)
4. [Detailed Use Cases](#detailed-use-cases)
5. [Use Case Relationships](#use-case-relationships)
6. [System Requirements Mapping](#system-requirements-mapping)

---

## Overview

**LocalLend** is a community-based tool and appliance sharing platform where neighbors can lend and borrow items within their community. This document describes all use cases, actors, and their interactions with the system.

**Tagline**: *Where neighbors become your lending library for tools, appliances, and everyday essentials.*

---

## Actors

### 1. User (Regular Community Member)
**Description**: A verified resident who can borrow items, list their own items, participate in forums, and provide ratings.

**Primary Responsibilities**:
- Search and browse available items
- Borrow items from other community members
- List items for lending
- Manage personal item listings
- Return borrowed items
- Participate in community forum discussions
- Rate other users and items
- Provide feedback on transactions

**System Access**: Requires authentication via login

---

### 2. Admin (System Administrator)
**Description**: A privileged user who manages the platform, moderates content, verifies residents, and reviews user feedback.

**Primary Responsibilities**:
- Verify new resident registrations
- Review and moderate user feedback/ratings
- Manage system-level configurations
- Handle disputes and violations
- Monitor platform health
- Access administrative dashboard

**System Access**: Requires authentication with elevated privileges

---

### 3. Authentication Service (External System)
**Description**: An external authentication system that handles secure login, registration, and session management.

**Primary Responsibilities**:
- Validate user credentials
- Issue authentication tokens (JWT)
- Manage user sessions
- Handle password encryption
- Support secure resident verification

**System Access**: Invoked by the system for authentication operations

---

## Use Case Diagram Description

The use case diagram illustrates the interactions between actors (User, Admin, Authentication Service) and the LocalLend Platform system. The diagram shows:

### User Interactions (Blue Stick Figure - Left)
- Search Item
- Borrow Item
- Return Item
- List Item
- Manage Items
- Login User
- Register User

### Admin Interactions (Blue Stick Figure - Top Left)
- Login Admin
- Review Feedback
- Verify Resident

### System Use Cases (Enclosed in Blue Rectangle)
**Authentication Flow**:
- Register User
- Verify Resident (includes Register User)
- Login User
- Login Admin

**Item Management**:
- Manage Items
- Search Item (includes Borrow Item)
- Borrow Item
- Return Item
- List Item

**Rating and Feedback System**:
- User Rating (included in Review Feedback)
- Item Rating (extends Return Item)
- Provide Feedback (extends Return Item)
- Review Feedback (includes User Rating)

**Extension Points**:
- Return Item has extension points for Item Rating and Provide Feedback

### External System Interaction
- Authentication Service (Connected with <<Authentication Service>> stereotype)
- Handles authentication for Login User, Login Admin, and Register User

---

## Detailed Use Cases

### UC-01: Register User

**Actor**: User, Authentication Service  
**Preconditions**: User has valid resident information  
**Postconditions**: User account created and pending verification

**Main Flow**:
1. User navigates to registration page
2. User enters personal information (name, email, phone, address, password)
3. User submits registration form
4. System validates input data
5. System sends request to Authentication Service
6. Authentication Service encrypts password
7. System creates user account with "PENDING" verification status
8. System sends verification email to admin
9. System displays registration success message

**Alternative Flows**:
- **4a**: Validation fails → Display error messages
- **5a**: Email already exists → Display "Email already registered" error
- **8a**: Email service unavailable → Log error, proceed with account creation

**Business Rules**:
- Email must be unique
- Password must meet complexity requirements (min 8 characters, 1 uppercase, 1 number)
- Only community residents can register

**Related Use Cases**: UC-02 (Verify Resident)

---

### UC-02: Verify Resident

**Actor**: Admin  
**Preconditions**: User has registered; Admin is logged in  
**Postconditions**: User verification status updated

**Main Flow**:
1. Admin logs into admin dashboard
2. System displays pending verification requests
3. Admin reviews user registration details
4. Admin verifies user's resident status
5. Admin approves or rejects the request
6. System updates user verification status
7. System sends email notification to user
8. System displays confirmation message

**Alternative Flows**:
- **4a**: Admin cannot verify resident → Admin rejects request with reason
- **7a**: Email service fails → Log error, update status anyway

**Business Rules**:
- Only admin can verify residents
- User must provide valid address within community boundaries
- Verification decision must be recorded with timestamp

**Related Use Cases**: UC-01 (Register User), UC-03 (Login User)

---

### UC-03: Login User

**Actor**: User, Authentication Service  
**Preconditions**: User has verified account  
**Postconditions**: User authenticated and session created

**Main Flow**:
1. User navigates to login page
2. User enters email and password
3. System validates input format
4. System sends credentials to Authentication Service
5. Authentication Service validates credentials
6. Authentication Service generates JWT token
7. System creates user session
8. System redirects user to dashboard
9. System displays welcome message with user's trust score

**Alternative Flows**:
- **5a**: Invalid credentials → Display "Invalid email or password" error
- **5b**: Account not verified → Display "Account pending verification" message
- **5c**: Account suspended → Display "Account suspended, contact admin"

**Business Rules**:
- Maximum 5 failed login attempts within 15 minutes
- Account temporarily locked after 5 failed attempts
- JWT token expires after 24 hours

**Related Use Cases**: UC-01 (Register User), UC-02 (Verify Resident)

---

### UC-04: Login Admin

**Actor**: Admin, Authentication Service  
**Preconditions**: Admin account exists  
**Postconditions**: Admin authenticated with elevated privileges

**Main Flow**:
1. Admin navigates to admin login page
2. Admin enters admin credentials
3. System validates input format
4. System sends credentials to Authentication Service
5. Authentication Service validates admin credentials
6. Authentication Service generates admin JWT token with elevated roles
7. System creates admin session
8. System redirects to admin dashboard

**Alternative Flows**:
- **5a**: Invalid credentials → Display "Invalid admin credentials"
- **5b**: Admin account suspended → Display error and log security event

**Business Rules**:
- Admin accounts require stronger password requirements
- All admin actions are logged for audit purposes
- Admin JWT tokens expire after 8 hours

**Related Use Cases**: UC-02 (Verify Resident), UC-11 (Review Feedback)

---

### UC-05: Search Item

**Actor**: User  
**Preconditions**: User is logged in  
**Postconditions**: List of matching items displayed

**Main Flow**:
1. User navigates to search/browse page
2. User enters search criteria (keywords, category, condition, availability)
3. System queries item database
4. System filters items by availability status
5. System sorts results by relevance/distance
6. System displays paginated results with item details
7. User views item list with images, descriptions, ratings

**Alternative Flows**:
- **3a**: No search criteria → Display all available items
- **6a**: No items found → Display "No items match your search" message
- **6b**: User applies additional filters → Repeat from step 3

**Business Rules**:
- Only "AVAILABLE" items are shown in search results
- Items are ranked by: proximity (future), rating, availability
- Search is case-insensitive and supports partial matches

**Related Use Cases**: UC-06 (Borrow Item), UC-08 (List Item)

---

### UC-06: Borrow Item

**Actor**: User  
**Preconditions**: User is logged in; Item is available  
**Postconditions**: Booking created; Item marked as borrowed; Email notifications sent

**Main Flow**:
1. User searches for item (includes UC-05)
2. User selects item from search results
3. System displays item details with owner information
4. User clicks "Borrow" button
5. User specifies borrow duration (start date, end date)
6. System validates availability for requested dates
7. System creates booking record
8. System updates item status to "BORROWED"
9. System sends email to item owner confirming booking
10. System sends email to borrower with pickup details
11. System displays booking confirmation with reference number

**Alternative Flows**:
- **6a**: Item unavailable for selected dates → Display "Item not available for these dates"
- **6b**: User already has pending booking for this item → Display error
- **9a**: Email service fails → Log error, booking still created

**Business Rules**:
- User cannot borrow their own items
- Maximum borrow duration: 30 days
- User can have maximum 5 active borrowings simultaneously
- Borrow request creates booking in "CONFIRMED" status

**Related Use Cases**: UC-05 (Search Item), UC-07 (Return Item), UC-08 (List Item)

---

### UC-07: Return Item

**Actor**: User  
**Preconditions**: User has borrowed item; Booking is active  
**Postconditions**: Booking completed; Item available again; Extension points triggered

**Main Flow**:
1. User navigates to "My Borrowings" page
2. System displays active borrowings
3. User selects item to return
4. User clicks "Mark as Returned" button
5. System prompts for return confirmation
6. System updates booking status to "RETURNED"
7. System updates item status to "AVAILABLE"
8. System sends notification to item owner
9. **Extension Point**: System prompts user to rate item (UC-09)
10. **Extension Point**: System prompts user to provide feedback (UC-10)
11. System displays return confirmation message

**Alternative Flows**:
- **3a**: Late return → System calculates late fee/penalty (future feature)
- **6a**: Owner disputes return → Booking marked for admin review
- **8a**: Email notification fails → Log error, proceed with return

**Business Rules**:
- Only borrower can mark item as returned
- Item owner can confirm or dispute the return
- Return triggers rating and feedback extension points
- Late returns affect user's trust score

**Related Use Cases**: UC-06 (Borrow Item), UC-09 (Item Rating), UC-10 (Provide Feedback)

---

### UC-08: List Item

**Actor**: User  
**Preconditions**: User is logged in and verified  
**Postconditions**: New item listed and available for borrowing

**Main Flow**:
1. User navigates to "List New Item" page
2. User enters item details:
   - Title
   - Description
   - Category (selected from dropdown)
   - Condition (NEW, LIKE_NEW, GOOD, FAIR)
   - Upload images (optional)
   - Set availability status
3. System validates input data
4. System saves item with owner association
5. System sets item status to "AVAILABLE"
6. System displays success message
7. System redirects to "My Items" page

**Alternative Flows**:
- **3a**: Validation fails → Display field-specific error messages
- **4a**: Image upload fails → Save item without image, log warning
- **2a**: User sets availability to "UNAVAILABLE" → Item saved but not searchable

**Business Rules**:
- User can list unlimited items
- Category must be from predefined list
- Images must be JPG/PNG, max 5MB each
- Maximum 5 images per item
- Item description minimum 20 characters

**Related Use Cases**: UC-09 (Manage Items), UC-05 (Search Item)

---

### UC-09: Manage Items

**Actor**: User  
**Preconditions**: User is logged in; User has listed items  
**Postconditions**: Item details updated/deleted

**Main Flow**:
1. User navigates to "My Items" page
2. System displays user's items with current status
3. User selects an item to manage
4. User chooses action: Edit, Delete, or Toggle Availability
5. **If Edit**:
   - User modifies item details
   - System validates changes
   - System saves updated item
   - System displays success message
6. **If Delete**:
   - System checks for active bookings
   - User confirms deletion
   - System marks item as deleted (soft delete)
   - System displays deletion confirmation
7. **If Toggle Availability**:
   - System updates availability status
   - System notifies waitlisted users (future feature)
   - System displays status update confirmation

**Alternative Flows**:
- **6a**: Item has active bookings → Display "Cannot delete item with active bookings"
- **5a**: Validation fails during edit → Display error messages
- **7a**: User sets to unavailable → Item removed from search results

**Business Rules**:
- Cannot delete items with active bookings
- Can edit items but not category if item has booking history
- Availability toggle is instant
- All changes are timestamped and logged

**Related Use Cases**: UC-08 (List Item), UC-06 (Borrow Item)

---

### UC-10: Item Rating

**Actor**: User  
**Preconditions**: User has returned an item; Rating not already given for this booking  
**Postconditions**: Item rating recorded; Item's average rating updated

**Main Flow**:
1. User completes item return (extends UC-07)
2. System displays item rating modal
3. User rates item (1-5 stars)
4. User adds optional review text
5. System validates rating
6. System saves rating linked to item and booking
7. System recalculates item's average rating
8. System updates item rating score
9. System displays "Thank you for rating" message

**Alternative Flows**:
- **2a**: User skips rating → Rating marked as "Not Provided"
- **5a**: Invalid rating value → Display "Please select 1-5 stars"
- **3a**: User rates later → Access via "My Borrowings" history

**Business Rules**:
- Rating is optional but encouraged
- Can only rate items after return
- One rating per booking
- Cannot edit rating after submission
- Rating affects item's visibility in search results

**Related Use Cases**: UC-07 (Return Item), UC-11 (Review Feedback)

---

### UC-11: User Rating

**Actor**: User  
**Preconditions**: User completed transaction (as borrower or lender)  
**Postconditions**: User rating recorded; Trust score updated

**Main Flow**:
1. User completes transaction (borrow or lend)
2. System prompts to rate the other party
3. User selects rating (1-5 stars)
4. User adds feedback comments
5. System validates rating
6. System saves rating linked to rated user and booking
7. System recalculates user's trust score
8. System updates user's profile with new trust score
9. System displays rating confirmation

**Alternative Flows**:
- **2a**: User skips rating → Reminder sent after 24 hours
- **5a**: Inappropriate content detected → Submit for admin review
- **3a**: User rates later → Access via transaction history

**Business Rules**:
- Mutual rating: borrower rates lender, lender rates borrower
- Trust score = weighted average of all received ratings
- New users start with default trust score of 3.0
- Trust score visible on user profile
- Rating impacts matching and recommendations

**Related Use Cases**: UC-11 (Review Feedback), UC-06 (Borrow Item)

---

### UC-12: Provide Feedback

**Actor**: User  
**Preconditions**: User completed transaction  
**Postconditions**: Feedback recorded and sent to admin for review

**Main Flow**:
1. User completes item return (extends UC-07)
2. System displays feedback form
3. User selects feedback type (Positive, Negative, Suggestion, Issue)
4. User writes detailed feedback
5. User optionally uploads supporting images
6. System validates feedback
7. System saves feedback record
8. System notifies admin of new feedback
9. System displays "Feedback submitted" confirmation

**Alternative Flows**:
- **2a**: User skips feedback → Option available in profile history
- **6a**: Validation fails → Display error messages
- **8a**: Email to admin fails → Log error, feedback still saved

**Business Rules**:
- Feedback is optional
- Negative feedback triggers admin review
- Feedback limited to 500 characters
- Images must be relevant to the transaction
- Anonymous feedback option available

**Related Use Cases**: UC-07 (Return Item), UC-11 (Review Feedback)

---

### UC-13: Review Feedback

**Actor**: Admin  
**Preconditions**: Admin is logged in; Feedback exists  
**Postconditions**: Feedback reviewed; Action taken if necessary

**Main Flow**:
1. Admin logs into admin dashboard
2. System displays feedback queue sorted by priority
3. Admin selects feedback to review
4. System displays feedback details with user ratings (includes UC-10)
5. Admin reviews content and context
6. Admin takes action:
   - Mark as resolved
   - Contact user for clarification
   - Issue warning to violating user
   - Suspend user account
   - Dismiss feedback
7. System records admin action and timestamp
8. System notifies relevant users of resolution
9. System updates feedback status

**Alternative Flows**:
- **5a**: Feedback requires investigation → Admin flags for detailed review
- **6a**: Serious violation → Admin immediately suspends account
- **8a**: User no longer exists → Mark feedback as "User Inactive"

**Business Rules**:
- All negative feedback must be reviewed within 48 hours
- Admin actions are permanently logged
- Users can appeal admin decisions
- Feedback trends are analyzed monthly

**Related Use Cases**: UC-10 (User Rating), UC-12 (Provide Feedback), UC-04 (Login Admin)

---

## Use Case Relationships

### Include Relationships
**<<include>>** relationships represent required sub-use cases that are always executed.

1. **Search Item** includes **Borrow Item**
   - When borrowing, searching is a prerequisite step
   - Borrowing cannot happen without first finding the item

2. **Verify Resident** includes **Register User**
   - Verification process requires prior user registration
   - Cannot verify a user who hasn't registered

3. **Review Feedback** includes **User Rating**
   - Reviewing feedback requires access to user ratings
   - Admin sees ratings as part of feedback context

---

### Extend Relationships
**<<extend>>** relationships represent optional behavior that may be triggered under certain conditions.

1. **Item Rating** extends **Return Item**
   - **Extension Point**: After marking item as returned
   - **Condition**: User chooses to rate the item
   - **Optional**: User can skip rating

2. **Provide Feedback** extends **Return Item**
   - **Extension Point**: After completing return process
   - **Condition**: User wants to provide additional feedback
   - **Optional**: User can skip feedback

**Rationale**: The extend relationship allows these rating and feedback features to optionally enhance the return process without making them mandatory.

---

### Generalization Relationships
Not explicitly shown in this diagram, but the system supports:

1. **Login User** and **Login Admin** could generalize to "Login"
   - Both perform authentication but with different privilege levels
   - Share common authentication flow

2. **Item Rating** and **User Rating** could generalize to "Provide Rating"
   - Both involve rating (1-5 stars) and review text
   - Different targets (item vs. user) but similar process

---

## System Requirements Mapping

### Functional Requirements

| Requirement ID | Requirement | Related Use Cases |
|----------------|-------------|-------------------|
| FR-01 | System shall allow users to register accounts | UC-01 |
| FR-02 | System shall verify resident status | UC-02 |
| FR-03 | System shall authenticate users securely | UC-03, UC-04 |
| FR-04 | System shall allow users to list items | UC-08 |
| FR-05 | System shall allow users to manage their listings | UC-09 |
| FR-06 | System shall provide item search and browse | UC-05 |
| FR-07 | System shall enable item borrowing | UC-06 |
| FR-08 | System shall track item returns | UC-07 |
| FR-09 | System shall collect item ratings | UC-10 |
| FR-10 | System shall collect user ratings and calculate trust scores | UC-11 |
| FR-11 | System shall allow feedback submission | UC-12 |
| FR-12 | System shall enable admin review of feedback | UC-13 |
| FR-13 | System shall send email notifications | UC-06, UC-07, UC-02 |

---

### Non-Functional Requirements

| Requirement ID | Requirement | Implementation |
|----------------|-------------|----------------|
| NFR-01 | Security: All passwords encrypted | Spring Security + BCrypt |
| NFR-02 | Security: JWT token-based authentication | JWT implementation in Authentication Service |
| NFR-03 | Performance: Search results within 2 seconds | Database indexing on item title, category |
| NFR-04 | Scalability: Support 10,000 concurrent users | Stateless REST API design |
| NFR-05 | Availability: 99.5% uptime | Docker deployment, health monitoring |
| NFR-06 | Usability: Responsive design | Frontend framework (React/Angular) |
| NFR-07 | Data Integrity: ACID transactions | JPA transaction management |
| NFR-08 | Auditability: All actions logged | Logging framework (SLF4J + Logback) |

---

## Use Case Priority Matrix

### High Priority (Must Have - MVP)
- UC-01: Register User
- UC-02: Verify Resident
- UC-03: Login User
- UC-05: Search Item
- UC-06: Borrow Item
- UC-07: Return Item
- UC-08: List Item
- UC-09: Manage Items

### Medium Priority (Should Have - Phase 2)
- UC-04: Login Admin
- UC-10: Item Rating
- UC-11: User Rating
- UC-13: Review Feedback

### Low Priority (Nice to Have - Future)
- UC-12: Provide Feedback (Enhanced version)
- Location-based search (Proximity feature)
- Real-time notifications
- In-app messaging

---

## Use Case Traceability

### Authentication Flow
```
Register User (UC-01) 
    → Verify Resident (UC-02) 
    → Login User (UC-03) 
    → Access Platform Features
```

### Item Lifecycle Flow
```
List Item (UC-08) 
    → Search Item (UC-05) 
    → Borrow Item (UC-06) 
    → Return Item (UC-07) 
    → Item Rating (UC-10) [Optional]
    → Provide Feedback (UC-12) [Optional]
```

### Trust Building Flow
```
Complete Transaction 
    → User Rating (UC-11) 
    → Trust Score Updated 
    → Higher Visibility in Search
```

### Admin Oversight Flow
```
User Activity 
    → Provide Feedback (UC-12) 
    → Review Feedback (UC-13) 
    → Admin Action 
    → User Notification
```

---

## Use Case Coverage Analysis

### Total Use Cases: 13

**By Actor**:
- User: 10 use cases (77%)
- Admin: 2 use cases (15%)
- System (Authentication Service): 1 use case (8%)

**By Feature Area**:
- Authentication & User Management: 4 use cases (31%)
- Item Management: 3 use cases (23%)
- Borrowing & Returns: 2 use cases (15%)
- Rating & Feedback: 4 use cases (31%)

**Complexity Distribution**:
- Simple: 3 use cases (Login User, Login Admin, Search Item)
- Moderate: 6 use cases (Register, List Item, Manage Items, Item Rating, User Rating, Provide Feedback)
- Complex: 4 use cases (Verify Resident, Borrow Item, Return Item, Review Feedback)

---

## Business Rules Summary

### User Management
1. Only verified residents can use the platform
2. Email addresses must be unique
3. Passwords must meet complexity requirements
4. Failed login attempts are limited and tracked
5. User accounts can be suspended by admins

### Item Management
1. Users can list unlimited items
2. Items must have valid category and condition
3. Items with active bookings cannot be deleted
4. Only available items appear in search results
5. Item images are optional but recommended

### Borrowing & Returns
1. Maximum borrow duration: 30 days
2. Maximum active borrowings per user: 5
3. Users cannot borrow their own items
4. Late returns affect trust score
5. Both parties can rate each other

### Trust & Rating System
1. New users start with trust score of 3.0
2. Trust score is weighted average of all ratings
3. One rating per transaction
4. Ratings cannot be edited after submission
5. Trust score affects search ranking

### Administrative
1. All admin actions are logged
2. Negative feedback reviewed within 48 hours
3. Only admins can verify residents
4. Users can appeal admin decisions

---

## Extension Points for Future Features

### Proximity Feature (Location-based)
- **Extends**: Search Item (UC-05)
- **Description**: Filter and sort items by distance from user's location
- **Priority**: Medium
- **Dependencies**: User location data, geolocation API

### Forum System
- **New Use Cases**: Create Post, Reply to Post, Search Forum
- **Actors**: User, Admin
- **Priority**: High (per project requirements)
- **Dependencies**: Forum entity model, notification system

### In-App Messaging
- **Extends**: Borrow Item (UC-06)
- **Description**: Direct messaging between borrower and lender
- **Priority**: Medium
- **Dependencies**: WebSocket support, message entity model

### Payment Integration (Future)
- **Extends**: Borrow Item (UC-06)
- **Description**: Optional deposit or rental fees
- **Priority**: Low
- **Dependencies**: Payment gateway integration

---

## Conclusion

This use case documentation provides a comprehensive view of the LocalLend platform's functionality. The 13 core use cases cover:

✅ **User Authentication & Verification**  
✅ **Item Listing & Management**  
✅ **Search & Discovery**  
✅ **Borrowing & Returning Process**  
✅ **Rating & Feedback System**  
✅ **Administrative Oversight**

### Next Steps
1. ✅ Use case documentation complete
2. ⏭️ Create detailed sequence diagrams for complex use cases
3. ⏭️ Design database schema based on use cases
4. ⏭️ Implement REST API endpoints mapped to use cases
5. ⏭️ Develop frontend interfaces for each use case
6. ⏭️ Write integration tests covering all use case flows

---

**Document Version**: 1.0  
**Last Updated**: November 13, 2025  
**Author**: LocalLend Development Team  
**Status**: Approved for Implementation
