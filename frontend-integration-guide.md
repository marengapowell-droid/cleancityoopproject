# Frontend Integration Guide - Clean City Backend

## Root Cause of Pickup Confirmation Failure

The original frontend had a critical issue where the `confirmPickup` function only updated localStorage and never called the backend API. The Node.js backend had a `/api/pickups/:id/confirm` endpoint, but the frontend didn't use it.

### Original Problematic Code:
```javascript
function confirmPickup(pickupId) {
  let pickup = pickups.find(p => p.id === pickupId);
  if(pickup) {
    pickup.status = 'COMPLETED';
    save();  // Only saves to localStorage
    renderTrack();
    updateDashboard();
    toast(`✅ Pickup #${pickupId} marked as COMPLETED!`);
  }
}
```

### Fixed Code:
```javascript
async function confirmPickup(pickupId) {
  try {
    const token = localStorage.getItem('jwt_token');
    const response = await fetch(`${API_BASE}/pickups/confirm`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ pickupId: pickupId })
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to confirm pickup');
    }
    
    const result = await response.json();
    toast(`✅ Pickup confirmed successfully!`);
    
    // Reload data from server
    await loadData();
    renderTrack();
    updateDashboard();
    
  } catch (error) {
    console.error('Error confirming pickup:', error);
    toast(`❌ ${error.message}`);
  }
}
```

## New Spring Boot Backend API Endpoints

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "uuid",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "RESIDENT"
  },
  "timestamp": "2026-05-19T20:00:00"
}
```

### Pickup Endpoints

#### Create Pickup (Residents Only)
```http
POST /pickups
Authorization: Bearer {token}
Content-Type: application/json

{
  "wasteType": "PLASTIC",
  "location": "123 Main St",
  "scheduledDate": "2026-05-20",
  "scheduledTime": "09:00:00",
  "zone": "CBD",
  "notes": "Optional notes"
}
```

#### Confirm Pickup (Collectors & Admins Only) - **CRITICAL FIX**
```http
POST /pickups/confirm
Authorization: Bearer {token}
Content-Type: application/json

{
  "pickupId": "uuid-of-pickup",
  "notes": "Optional completion notes"
}
```

Response:
```json
{
  "success": true,
  "message": "Pickup confirmed successfully",
  "data": {
    "id": "uuid",
    "status": "COMPLETED",
    "completedAt": "2026-05-19T20:00:00",
    "collectorName": "John Collector"
  },
  "timestamp": "2026-05-19T20:00:00"
}
```

#### Get My Pickups (Residents Only)
```http
GET /pickups/my-pickups
Authorization: Bearer {token}
```

#### Get Assigned Pickups (Collectors Only)
```http
GET /pickups/assigned
Authorization: Bearer {token}
```

#### Get All Pickups (Admin Only)
```http
GET /pickups
Authorization: Bearer {token}
```

#### Assign Pickup to Collector (Admin Only)
```http
PATCH /pickups/{pickupId}/assign/{collectorId}
Authorization: Bearer {token}
```

#### Cancel Pickup (Residents & Admins)
```http
PATCH /pickups/{pickupId}/cancel
Authorization: Bearer {token}
```

## Frontend Integration Steps

### 1. Update API Configuration
```javascript
const API_BASE = 'http://localhost:8080/api';
```

### 2. Implement JWT Token Storage
```javascript
function setAuthToken(token) {
  localStorage.setItem('jwt_token', token);
}

function getAuthToken() {
  return localStorage.getItem('jwt_token');
}

function clearAuthToken() {
  localStorage.removeItem('jwt_token');
}
```

### 3. Update Login Function
```javascript
async function doResidentLogin() {
  const email = document.getElementById('loginResidentEmail').value;
  const password = document.getElementById('loginResidentPassword').value;
  
  try {
    const response = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    
    const result = await response.json();
    
    if (result.success) {
      setAuthToken(result.data.token);
      loginAs(result.data, 'resident');
    } else {
      toast('❌ ' + result.message);
    }
  } catch (error) {
    toast('❌ Login failed');
  }
}
```

### 4. Update Pickup Confirmation Function
```javascript
async function confirmPickup(pickupId) {
  const token = getAuthToken();
  if (!token) {
    toast('❌ Not authenticated');
    return;
  }
  
  try {
    const response = await fetch(`${API_BASE}/pickups/confirm`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ pickupId: pickupId })
    });
    
    const result = await response.json();
    
    if (result.success) {
      toast('✅ Pickup confirmed successfully!');
      await loadPickups(); // Reload from server
      renderTrack();
      updateDashboard();
    } else {
      toast('❌ ' + result.message);
    }
  } catch (error) {
    console.error('Error confirming pickup:', error);
    toast('❌ Failed to confirm pickup');
  }
}
```

### 5. Update Logout Function
```javascript
function logout() {
  clearAuthToken();
  currentUser = null;
  currentRole = null;
  document.getElementById('dashboardScreen').style.display = 'none';
  document.getElementById('loginScreen').style.display = 'flex';
  toast('👋 Signed out');
}
```

### 6. Add Authorization Header to All API Calls
```javascript
async function apiCall(endpoint, options = {}) {
  const token = getAuthToken();
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers
  });
}
```

## Testing the Integration

### Test Pickup Confirmation Flow:

1. **Login as Collector**
   - Use the login endpoint with collector credentials
   - Store the JWT token

2. **View Assigned Pickups**
   - Call `GET /pickups/assigned`
   - Verify pickups are displayed

3. **Confirm a Pickup**
   - Call `POST /pickups/confirm` with pickup ID
   - Verify status changes to COMPLETED
   - Verify completedAt timestamp is set
   - Verify collector statistics are updated

4. **Verify Database Update**
   - Check Supabase PostgreSQL database
   - Verify pickup status is COMPLETED
   - Verify completed_at field is populated

## Error Handling

### Common Errors and Solutions:

1. **401 Unauthorized**
   - Cause: Invalid or expired JWT token
   - Solution: Re-authenticate and get new token

2. **403 Forbidden**
   - Cause: User doesn't have permission
   - Solution: Check user role and permissions

3. **404 Not Found**
   - Cause: Pickup ID doesn't exist
   - Solution: Verify pickup ID is correct

4. **400 Bad Request**
   - Cause: Invalid request data
   - Solution: Validate request payload

## Security Notes

1. **Always use HTTPS in production**
2. **Store JWT tokens securely (consider httpOnly cookies)**
3. **Implement token refresh mechanism**
4. **Validate all user inputs**
5. **Use parameterized queries (already implemented in JPA)**
6. **Implement rate limiting on API endpoints**
7. **Log all authentication attempts**

## Monitoring and Debugging

### Enable Debug Logging:
```properties
logging.level.com.cleancity=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Check Logs:
- Backend logs: `logs/cleancity-backend.log`
- Console output for real-time debugging

### API Documentation:
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/api-docs`

## Next Steps

1. Update the frontend HTML files with the new API integration
2. Test all endpoints thoroughly
3. Implement error handling in the frontend
4. Add loading states for async operations
5. Implement token refresh mechanism
6. Add proper error messages for users
7. Test with real Supabase PostgreSQL database
8. Deploy to production environment
