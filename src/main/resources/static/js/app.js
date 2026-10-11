// Trust Circle Main Application Controller
const App = {
    currentUser: null,
    activeTab: 'activities',

    async init() {
        this.setupEventListeners();

        const signedIn = await this.checkAuth();
        if (!signedIn) {
            this.showAuthGate();
            return;
        }

        this.enterApp();
    },

    /** Resolves true when a valid session was restored. */
    async checkAuth() {
        if (!API.getToken()) return false;
        try {
            this.currentUser = await API.getCurrentUser();
            this.updateNavUser(this.currentUser);
            return true;
        } catch (err) {
            console.warn('Session expired or invalid token:', err);
            API.setToken(null);
            this.currentUser = null;
            return false;
        }
    },

    /** Reveal the app shell and land on the right first screen. */
    enterApp() {
        this.hideAuthGate();
        this.updateNavUser(this.currentUser);
        // Unverified members are sent to the survey; everyone else to the feed.
        this.switchTab(this.currentUser && this.currentUser.identityVerified ? 'activities' : 'verify');
    },

    showAuthGate() {
        const gate = document.getElementById('authGate');
        if (gate) gate.style.display = 'flex';
        document.body.classList.add('auth-locked');
        this.showAuthMode('login');
    },

    hideAuthGate() {
        const gate = document.getElementById('authGate');
        if (gate) gate.style.display = 'none';
        document.body.classList.remove('auth-locked');
    },

    showAuthMode(mode) {
        const isLogin = mode === 'login';
        document.getElementById('loginForm').style.display = isLogin ? 'flex' : 'none';
        document.getElementById('signupForm').style.display = isLogin ? 'none' : 'flex';
        document.getElementById('authTabLogin').classList.toggle('active', isLogin);
        document.getElementById('authTabSignup').classList.toggle('active', !isLogin);
        this.clearAuthError();
    },

    showAuthError(message) {
        const box = document.getElementById('authError');
        if (!box) return;
        box.innerText = message;
        box.style.display = 'block';
    },

    clearAuthError() {
        const box = document.getElementById('authError');
        if (box) box.style.display = 'none';
    },

    async handleLogin(event) {
        event.preventDefault();
        this.clearAuthError();

        const email = document.getElementById('login_email').value.trim();
        const password = document.getElementById('login_password').value;
        const btn = document.getElementById('loginSubmitBtn');

        btn.disabled = true;
        btn.innerText = 'Signing in…';
        try {
            const resp = await API.login(email, password);
            API.setToken(resp.token);
            this.currentUser = resp.user;
            this.enterApp();
            this.showToast(`Welcome back, ${resp.user.fullName}!`, 'success');
        } catch (err) {
            this.showAuthError(err.message);
        } finally {
            btn.disabled = false;
            btn.innerText = 'Sign In';
        }
    },

    async handleSignup(event) {
        event.preventDefault();
        this.clearAuthError();

        const payload = {
            fullName: document.getElementById('signup_name').value.trim(),
            email: document.getElementById('signup_email').value.trim(),
            phone: document.getElementById('signup_phone').value.trim(),
            password: document.getElementById('signup_password').value
        };
        const btn = document.getElementById('signupSubmitBtn');

        btn.disabled = true;
        btn.innerText = 'Creating account…';
        try {
            const resp = await API.register(payload);
            API.setToken(resp.token);
            this.currentUser = resp.user;
            this.enterApp();
            this.showToast('Account created — complete verification to unlock activities.', 'success');
        } catch (err) {
            this.showAuthError(err.message);
        } finally {
            btn.disabled = false;
            btn.innerText = 'Create Account';
        }
    },

    toggleUserMenu() {
        const menu = document.getElementById('userMenu');
        if (menu) menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
    },

    async refreshUserData() {
        if (API.getToken()) {
            try {
                this.currentUser = await API.getCurrentUser();
                this.updateNavUser(this.currentUser);
            } catch (e) {}
        }
    },

    updateNavUser(user) {
        const userPill = document.getElementById('navUserPill');
        const loginBtn = document.getElementById('navLoginBtn');

        if (user) {
            if (loginBtn) loginBtn.style.display = 'none';
            if (userPill) {
                userPill.style.display = 'flex';
                const initial = (user.fullName || 'U').charAt(0).toUpperCase();
                const tier = user.trustTier || 'BRONZE';
                const score = user.trustScore || 50;

                document.getElementById('navUserAvatar').innerText = initial;
                document.getElementById('navUserName').innerText = user.fullName;
                const badge = document.getElementById('navUserTrust');
                badge.innerText = `${tier} ${score}`;
                badge.className = `trust-badge-pill tier-${tier.toLowerCase()}`;
            }
        } else {
            if (loginBtn) loginBtn.style.display = 'inline-flex';
            if (userPill) userPill.style.display = 'none';
        }
    },

    switchTab(tabName) {
        this.activeTab = tabName;

        // Update nav buttons
        document.querySelectorAll('.nav-btn').forEach(btn => {
            if (btn.dataset.tab === tabName) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });

        // Hide all views
        document.querySelectorAll('.tab-view').forEach(view => {
            view.style.display = 'none';
        });

        // Show target view
        const targetView = document.getElementById(`view_${tabName}`);
        if (targetView) {
            targetView.style.display = 'block';
        }

        // Trigger view loads
        if (tabName === 'activities') {
            Activities.loadActivities(Activities.currentCategory);
        } else if (tabName === 'scheduled') {
            Scheduled.loadScheduledActivities();
        } else if (tabName === 'matches') {
            Matchmaker.loadMatches();
        } else if (tabName === 'trust') {
            Trust.loadScorecard();
        } else if (tabName === 'safety') {
            Safety.loadAlerts();
        } else if (tabName === 'verify') {
            VerificationForm.init();
        }
    },

    logout() {
        API.setToken(null);
        this.currentUser = null;
        this.updateNavUser(null);
        const menu = document.getElementById('userMenu');
        if (menu) menu.style.display = 'none';
        document.getElementById('loginForm').reset();
        document.getElementById('signupForm').reset();
        this.showAuthGate();
        this.showToast('Signed out', 'success');
    },

    showTrustWarningModal(userScore, requiredScore) {
        document.getElementById('tw_userScore').innerText = userScore;
        document.getElementById('tw_reqScore').innerText = requiredScore;
        document.getElementById('trustWarningModal').classList.add('active');
    },

    closeTrustWarningModal() {
        document.getElementById('trustWarningModal').classList.remove('active');
    },

    showToast(message, type = 'info') {
        const toast = document.getElementById('toastMsg');
        if (!toast) return;

        let icon = 'ℹ️';
        if (type === 'success') icon = '✅';
        if (type === 'error') icon = '⚠️';

        toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
        toast.style.display = 'flex';

        if (this.toastTimeout) clearTimeout(this.toastTimeout);
        this.toastTimeout = setTimeout(() => {
            toast.style.display = 'none';
        }, 4000);
    },

    setupEventListeners() {
        // Nav tabs
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                this.switchTab(btn.dataset.tab);
            });
        });

        // Category filter chips
        document.querySelectorAll('.cat-chip').forEach(chip => {
            chip.addEventListener('click', () => {
                document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('active'));
                chip.classList.add('active');
                Activities.loadActivities(chip.dataset.cat);
            });
        });

        // Modals close on click outside
        document.querySelectorAll('.modal-overlay').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.classList.remove('active');
                }
            });
        });

        // User dropdown closes on any click outside of it
        document.addEventListener('click', (e) => {
            const menu = document.getElementById('userMenu');
            const pill = document.getElementById('navUserPill');
            if (!menu || menu.style.display !== 'block') return;
            if (!menu.contains(e.target) && pill && !pill.contains(e.target)) {
                menu.style.display = 'none';
            }
        });
    }
};

// Start application when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
