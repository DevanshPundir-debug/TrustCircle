# Trust Circle - Frontend Integration Guide

## Phase 2: Connecting Frontend to Backend

---

## Overview

The frontend is 70% complete with:
- ✅ HTML templates for all pages
- ✅ CSS styling (19KB)
- ✅ JavaScript modules with API wrapper
- ⚠️ API endpoints need configuration adjustment
- ⚠️ Error handling needs improvement
- ⚠️ Form validation needs enhancement

---

## STEP 1: API Configuration

### 1.1 Update API Base URL

Edit: `src/main/resources/static/js/api.js`

**Current (around line 50):**
```javascript
// API endpoints
auth: {
    login: '/api/auth/login',
    register: '/api/auth/register',
    getCurrentUser: '/api/auth/me',
    verifyForm: '/api/auth/verify-form'
},
```

**Should be:**
```javascript
// API endpoints - now using absolute URLs
auth: {
    login: 'http://localhost:8085/api/auth/login',
    register: 'http://localhost:8085/api/auth/register',
    getCurrentUser: 'http://localhost:8085/api/auth/me',
    verifyForm: 'http://localhost:8085/api/auth/verify-form'
},

activities: {
    list: 'http://localhost:8085/api/activities',
    getById: (id) => `http://localhost:8085/api/activities/${id}`,
    create: 'http://localhost:8085/api/activities',
    join: (id) => `http://localhost:8085/api/activities/${id}/join`,
    participants: (id) => `http://localhost:8085/api/activities/${id}/participants`,
    complete: (id) => `http://localhost:8085/api/activities/${id}/complete`
},

matching: {
    companions: 'http://localhost:8085/api/matching/companions'
},

trust: {
    scorecard: 'http://localhost:8085/api/trust/scorecard',
    submitReview: 'http://localhost:8085/api/trust/review',
    getUserReviews: (userId) => `http://localhost:8085/api/trust/reviews/${userId}`
},

safety: {
    alerts: 'http://localhost:8085/api/safety/alerts',
    report: 'http://localhost:8085/api/safety/report',
    resolveAlert: (id) => `http://localhost:8085/api/safety/alerts/${id}/resolve`
}
```

### 1.2 Update API Client Methods

Add these corrected methods to API object in `api.js`:

```javascript
// Activities API
activities: {
    async list() {
        return await API.request('http://localhost:8085/api/activities', {
            method: 'GET'
        });
    },

    async getById(id) {
        return await API.request(`http://localhost:8085/api/activities/${id}`, {
            method: 'GET'
        });
    },

    async create(activityData) {
        return await API.request('http://localhost:8085/api/activities', {
            method: 'POST',
            body: JSON.stringify(activityData)
        });
    },

    async join(activityId) {
        return await API.request(`http://localhost:8085/api/activities/${activityId}/join`, {
            method: 'POST'
        });
    },

    async getParticipants(activityId) {
        return await API.request(
            `http://localhost:8085/api/activities/${activityId}/participants`,
            { method: 'GET' }
        );
    },

    async complete(activityId) {
        return await API.request(
            `http://localhost:8085/api/activities/${activityId}/complete`,
            { method: 'POST' }
        );
    }
},

// Matching API
matching: {
    async getCompanions() {
        return await API.request('http://localhost:8085/api/matching/companions', {
            method: 'GET'
        });
    }
},

// Trust & Reviews API
trust: {
    async getScorecard() {
        return await API.request('http://localhost:8085/api/trust/scorecard', {
            method: 'GET'
        });
    },

    async submitReview(reviewData) {
        return await API.request('http://localhost:8085/api/trust/review', {
            method: 'POST',
            body: JSON.stringify(reviewData)
        });
    },

    async getUserReviews(userId) {
        return await API.request(
            `http://localhost:8085/api/trust/reviews/${userId}`,
            { method: 'GET' }
        );
    }
},

// Safety API
safety: {
    async getAlerts() {
        return await API.request('http://localhost:8085/api/safety/alerts', {
            method: 'GET'
        });
    },

    async reportSuspicious(reportData) {
        return await API.request('http://localhost:8085/api/safety/report', {
            method: 'POST',
            body: JSON.stringify(reportData)
        });
    },

    async resolveAlert(alertId, resolutionData) {
        return await API.request(
            `http://localhost:8085/api/safety/alerts/${alertId}/resolve`,
            {
                method: 'POST',
                body: JSON.stringify(resolutionData)
            }
        );
    }
}
```

---

## STEP 2: Fix CORS Issues

### 2.1 Update Backend CORS Configuration

Edit: `src/main/java/TrustCircle/Project/config/WebConfig.java`

**Current:**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:8085")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

**Should include frontend origins:**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:8085",
                "http://localhost:3000",
                "http://127.0.0.1:8085",
                "file://*"  // For local file access
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### 2.2 Rebuild Backend

```bash
cd /path/to/Project
mvn clean compile
mvn package -DskipTests

# Restart Spring Boot
# mvn spring-boot:run
```

---

## STEP 3: Fix Frontend API Calls

### 3.1 Fix Login Form

Edit: `src/main/resources/static/js/app.js`

Find the login handler and update it:

**Before:**
```javascript
async handleLogin() {
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    
    if (!email || !password) {
        alert('Please fill in all fields');
        return;
    }

    try {
        const response = await API.login(email, password);
        API.setToken(response.token);
        this.currentUser = response;
        this.switchTab('activities');
    } catch (err) {
        alert('Login failed: ' + err.message);
    }
}
```

**After (with better error handling):**
```javascript
async handleLogin() {
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    
    if (!email || !password) {
        this.showError('Please fill in all fields');
        return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        this.showError('Please enter a valid email address');
        return;
    }

    try {
        const loginBtn = document.querySelector('[data-action="login"]');
        loginBtn.disabled = true;
        loginBtn.textContent = 'Logging in...';

        const response = await fetch('http://localhost:8085/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Login failed');
        }

        // Store token and user data
        localStorage.setItem('trust_circle_jwt', data.token);
        localStorage.setItem('trust_circle_user', JSON.stringify(data));
        
        this.currentUser = data;
        this.showSuccess('Login successful!');
        
        // Redirect to activities
        setTimeout(() => {
            this.switchTab('activities');
            this.loadActivities();
        }, 500);

    } catch (err) {
        console.error('Login error:', err);
        this.showError('Login failed: ' + err.message);
    } finally {
        loginBtn.disabled = false;
        loginBtn.textContent = 'Login';
    }
}
```

### 3.2 Fix Activities Loading

Edit: `src/main/resources/static/js/activities.js`

Update activity loading:

```javascript
async function loadActivities() {
    try {
        const response = await fetch('http://localhost:8085/api/activities', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load activities');
        }

        const activities = await response.json();
        renderActivities(activities);

    } catch (err) {
        console.error('Error loading activities:', err);
        document.getElementById('activitiesList').innerHTML = 
            '<p style="color: red;">Error loading activities. Please refresh.</p>';
    }
}

function renderActivities(activities) {
    const container = document.getElementById('activitiesList');
    
    if (!activities || activities.length === 0) {
        container.innerHTML = '<p>No activities found. Create one to get started!</p>';
        return;
    }

    container.innerHTML = activities.map(activity => `
        <div class="activity-card">
            <h3>${activity.title}</h3>
            <p>${activity.description}</p>
            <div class="activity-meta">
                <span>📍 ${activity.location || 'Location TBA'}</span>
                <span>👥 ${activity.minParticipants}-${activity.maxParticipants} people</span>
                <span>💰 ₹${activity.estimatedBudget || 'TBA'}</span>
            </div>
            <div class="activity-actions">
                <button onclick="viewActivityDetails(${activity.id})">View Details</button>
                <button onclick="joinActivity(${activity.id})" class="btn-primary">Join</button>
            </div>
        </div>
    `).join('');
}
```

### 3.3 Fix Create Activity Form

Update form submission in `activities.js`:

```javascript
async function createActivity(e) {
    e.preventDefault();

    const token = localStorage.getItem('trust_circle_jwt');
    if (!token) {
        alert('Please login first');
        return;
    }

    const formData = {
        title: document.getElementById('actTitle').value,
        description: document.getElementById('actDescription').value,
        activityType: document.getElementById('actType').value,
        latitude: parseFloat(document.getElementById('actLat').value),
        longitude: parseFloat(document.getElementById('actLng').value),
        location: document.getElementById('actLocation').value,
        scheduledDate: document.getElementById('actDate').value,
        scheduledTime: document.getElementById('actTime').value,
        durationMinutes: parseInt(document.getElementById('actDuration').value),
        minParticipants: parseInt(document.getElementById('actMinParticipants').value),
        maxParticipants: parseInt(document.getElementById('actMaxParticipants').value),
        estimatedBudget: parseFloat(document.getElementById('actBudget').value)
    };

    // Validation
    if (!formData.title || !formData.description) {
        alert('Please fill in all required fields');
        return;
    }

    try {
        const response = await fetch('http://localhost:8085/api/activities', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Failed to create activity');
        }

        alert('Activity created successfully!');
        document.getElementById('createActivityForm').reset();
        loadActivities();

    } catch (err) {
        console.error('Error creating activity:', err);
        alert('Error: ' + err.message);
    }
}
```

---

## STEP 4: Implement Error Handling

### 4.1 Create Global Error/Success Handlers

Add to `app.js`:

```javascript
App.showError = function(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'notification error';
    errorDiv.textContent = message;
    errorDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background-color: #ff6b6b;
        color: white;
        padding: 15px 20px;
        border-radius: 5px;
        z-index: 9999;
        animation: slideIn 0.3s ease;
    `;
    document.body.appendChild(errorDiv);
    
    setTimeout(() => {
        errorDiv.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => errorDiv.remove(), 300);
    }, 4000);
};

App.showSuccess = function(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'notification success';
    successDiv.textContent = message;
    successDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background-color: #51cf66;
        color: white;
        padding: 15px 20px;
        border-radius: 5px;
        z-index: 9999;
        animation: slideIn 0.3s ease;
    `;
    document.body.appendChild(successDiv);
    
    setTimeout(() => {
        successDiv.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => successDiv.remove(), 300);
    }, 3000);
};

App.showLoading = function(message = 'Loading...') {
    const loadingDiv = document.createElement('div');
    loadingDiv.id = 'loadingIndicator';
    loadingDiv.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: white;
        padding: 30px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 9998;
        text-align: center;
    `;
    loadingDiv.innerHTML = `
        <div style="border: 4px solid #f3f3f3; border-top: 4px solid #3498db; 
                    border-radius: 50%; width: 40px; height: 40px; 
                    animation: spin 1s linear infinite; margin: 0 auto;"></div>
        <p style="margin-top: 15px; color: #333;">${message}</p>
    `;
    document.body.appendChild(loadingDiv);
    return loadingDiv;
};

App.hideLoading = function() {
    const loading = document.getElementById('loadingIndicator');
    if (loading) loading.remove();
};
```

### 4.2 Add CSS Animations

Add to `style.css`:

```css
@keyframes slideIn {
    from {
        transform: translateX(400px);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

@keyframes slideOut {
    from {
        transform: translateX(0);
        opacity: 1;
    }
    to {
        transform: translateX(400px);
        opacity: 0;
    }
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.notification {
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    font-weight: 500;
}

.error { background-color: #ff6b6b !important; }
.success { background-color: #51cf66 !important; }
.warning { background-color: #ffd43b !important; }
```

---

## STEP 5: Form Validation

### 5.1 Add Input Validation

Create new file: `src/main/resources/static/js/validation.js`

```javascript
// Form Validation Utility
const FormValidator = {
    isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    },

    isValidPassword(password) {
        // Min 8 chars, at least 1 uppercase, 1 number
        return /^(?=.*[A-Z])(?=.*\d).{8,}$/.test(password);
    },

    isValidPhoneNumber(phone) {
        return /^[0-9]{10}$/.test(phone.replace(/\D/g, ''));
    },

    validateLoginForm(email, password) {
        const errors = [];
        
        if (!email) errors.push('Email is required');
        else if (!this.isValidEmail(email)) errors.push('Invalid email format');
        
        if (!password) errors.push('Password is required');
        else if (password.length < 6) errors.push('Password must be at least 6 characters');
        
        return errors;
    },

    validateRegisterForm(email, password, confirmPassword, fullName) {
        const errors = [];
        
        if (!fullName) errors.push('Full name is required');
        if (!email) errors.push('Email is required');
        else if (!this.isValidEmail(email)) errors.push('Invalid email format');
        
        if (!password) errors.push('Password is required');
        else if (!this.isValidPassword(password)) 
            errors.push('Password must have at least 8 chars, 1 uppercase letter, and 1 number');
        
        if (password !== confirmPassword) errors.push('Passwords do not match');
        
        return errors;
    },

    validateActivityForm(title, description, date, time, minParticipants, maxParticipants) {
        const errors = [];
        
        if (!title || title.trim().length === 0) errors.push('Activity title is required');
        if (!description || description.trim().length === 0) errors.push('Description is required');
        if (!date) errors.push('Date is required');
        if (!time) errors.push('Time is required');
        if (minParticipants >= maxParticipants) errors.push('Max participants must be greater than min');
        if (minParticipants < 2) errors.push('Minimum participants must be at least 2');
        
        return errors;
    }
};
```

### 5.2 Use Validation in Forms

Update form submissions:

```javascript
async function handleRegister(e) {
    e.preventDefault();

    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('regConfirmPassword').value;
    const fullName = document.getElementById('regFullName').value;

    // Validate
    const errors = FormValidator.validateRegisterForm(email, password, confirmPassword, fullName);
    if (errors.length > 0) {
        App.showError(errors.join('\n'));
        return;
    }

    try {
        App.showLoading('Creating account...');

        const response = await fetch('http://localhost:8085/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password, fullName })
        });

        App.hideLoading();
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Registration failed');
        }

        App.showSuccess('Account created! Please login.');
        document.getElementById('registerForm').reset();
        // Redirect to login
        App.switchTab('login');

    } catch (err) {
        App.hideLoading();
        console.error('Registration error:', err);
        App.showError('Registration failed: ' + err.message);
    }
}
```

---

## STEP 6: Handle Token Expiration

### 6.1 Add Token Refresh

Update `api.js`:

```javascript
API.isTokenExpired = function() {
    const token = this.getToken();
    if (!token) return true;

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const expiryTime = payload.exp * 1000; // Convert to milliseconds
        return Date.now() > expiryTime;
    } catch (e) {
        return true;
    }
};

API.handleTokenExpired = function() {
    this.setToken(null);
    App.currentUser = null;
    App.showError('Session expired. Please login again.');
    App.switchTab('login');
};
```

### 6.2 Check Token Before API Calls

Update request handler:

```javascript
API.request = async function(endpoint, options = {}) {
    // Check if token is expired
    if (this.isTokenExpired()) {
        this.handleTokenExpired();
        throw new Error('Session expired');
    }

    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    const token = this.getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(endpoint, {
            ...options,
            headers
        });

        if (response.status === 401) {
            this.handleTokenExpired();
            throw new Error('Unauthorized - session expired');
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(data?.message || `HTTP ${response.status}`);
        }

        return data;
    } catch (err) {
        console.error(`API Error on ${endpoint}:`, err);
        throw err;
    }
};
```

---

## STEP 7: Test Frontend Integration

### 7.1 Check Browser Console

```javascript
// In browser console (F12)
// Test API connectivity
fetch('http://localhost:8085/api/activities')
    .then(r => r.json())
    .then(d => console.log('Activities:', d))
    .catch(e => console.log('Error:', e));
```

### 7.2 Test Login Flow

1. Open http://localhost:8085
2. Click "Login"
3. Enter credentials:
   - Email: `abhijeet@trustcircle.com`
   - Password: `hashed_password_1_bcrypt_here`
4. Should see Activities list

### 7.3 Test Activity Creation

1. After login, click "Create Activity"
2. Fill form with:
   - Title: "Test Activity"
   - Type: "Sports"
   - Date: Tomorrow
   - Time: 18:00
3. Submit
4. Should see success message

### 7.4 Check Network Tab

- Open DevTools (F12)
- Go to Network tab
- Perform action
- Check requests:
  - Status should be 200, 201, etc. (not 404, 500)
  - Response should be valid JSON
  - Headers should have Authorization token

---

## STEP 8: Integration Checklist

```
[ ] API base URLs updated in api.js
[ ] CORS configuration updated in backend
[ ] Backend rebuilt and restarted
[ ] Login form working
[ ] Can retrieve JWT token
[ ] Activities list loading from API
[ ] Can create new activity
[ ] Can join activity
[ ] Peer review form submitting
[ ] Trust scorecard loading
[ ] Safety alerts showing
[ ] Smart matching working
[ ] Error messages displaying
[ ] Loading indicators showing
[ ] Token validation working
[ ] Session timeout handling
[ ] Form validation working
[ ] All 23 endpoints reachable
```

---

## STEP 9: Common Integration Issues

### Issue 1: CORS Errors
**Error:** `Access to XMLHttpRequest blocked by CORS policy`

**Fix:**
```java
// In WebConfig.java, add frontend origins:
registry.addMapping("/**")
    .allowedOrigins("http://localhost:8085", "http://localhost:3000")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
```

### Issue 2: 401 Unauthorized
**Error:** `401 Unauthorized` when calling protected endpoints

**Fix:**
- Ensure token is being sent in Authorization header
- Check token is valid (not expired)
- Verify token format: `Bearer <token>`

### Issue 3: 404 Not Found
**Error:** `404 Not Found` on API endpoints

**Fix:**
- Verify API endpoint URLs are correct
- Check backend server is running on port 8085
- Check endpoint exists in controller

### Issue 4: Empty Response
**Error:** `undefined` or empty data from API

**Fix:**
- Check response JSON format matches expected structure
- Add error logging to understand what's being returned
- Verify backend returns correct data

---

## Next: Phase 3 - Bug Fixes & Code Polish

Once frontend is fully integrated:
1. Run comprehensive E2E tests
2. Fix any remaining bugs
3. Add input validation
4. Improve error messages
5. Add loading states
6. Optimize database queries

See document: `05_Bug_Fixes_And_Optimization.md`
