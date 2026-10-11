// Trust Circle API Client
const API = {
    TOKEN_KEY: 'trust_circle_jwt',

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    setToken(token) {
        if (token) {
            localStorage.setItem(this.TOKEN_KEY, token);
        } else {
            localStorage.removeItem(this.TOKEN_KEY);
        }
    },

    async request(endpoint, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(endpoint, config);
            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const errorMsg = data && data.error ? data.error : `HTTP ${response.status}: Request failed`;
                throw new Error(errorMsg);
            }

            return data;
        } catch (err) {
            console.error(`API Error on ${endpoint}:`, err);
            throw err;
        }
    },

    // Auth endpoints
    login(email, password) {
        return this.request('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },

    register(payload) {
        return this.request('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
    },

    getCurrentUser() {
        return this.request('/api/auth/me');
    },

    submitVerificationForm(formData) {
        return this.request('/api/auth/verify-form', {
            method: 'POST',
            body: JSON.stringify(formData)
        });
    },

    // Activities
    getActivities(category = 'ALL', status = 'OPEN') {
        const params = new URLSearchParams();
        if (category && category !== 'ALL') params.append('category', category);
        if (status) params.append('status', status);
        return this.request(`/api/activities?${params.toString()}`);
    },

    getMySchedule() {
        return this.request('/api/activities/mine');
    },

    respondToJoinRequest(participantId, decision) {
        return this.request(`/api/activities/requests/${participantId}/${decision}`, {
            method: 'POST'
        });
    },

    getActivity(id) {
        return this.request(`/api/activities/${id}`);
    },

    createActivity(activityData) {
        return this.request('/api/activities', {
            method: 'POST',
            body: JSON.stringify(activityData)
        });
    },

    joinActivity(id) {
        return this.request(`/api/activities/${id}/join`, {
            method: 'POST'
        });
    },

    completeActivity(id) {
        return this.request(`/api/activities/${id}/complete`, {
            method: 'POST'
        });
    },

    // Smart Matching
    getSmartMatches() {
        return this.request('/api/matching/companions');
    },

    // Trust & Reputation
    getScorecard() {
        return this.request('/api/trust/scorecard');
    },

    submitPeerReview(reviewData) {
        return this.request('/api/trust/review', {
            method: 'POST',
            body: JSON.stringify(reviewData)
        });
    },

    getUserReviews(userId) {
        return this.request(`/api/trust/reviews/${userId}`);
    },

    // AI Safety
    getSafetyAlerts() {
        return this.request('/api/safety/alerts');
    },

    resolveAlert(alertId, notes) {
        return this.request(`/api/safety/alerts/${alertId}/resolve`, {
            method: 'POST',
            body: JSON.stringify({ notes })
        });
    },

    reportEntity(targetUserId, activityId, reason) {
        return this.request('/api/safety/report', {
            method: 'POST',
            body: JSON.stringify({ targetUserId, activityId, reason })
        });
    }
};
