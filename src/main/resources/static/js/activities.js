// Activities Explorer & Creation Module
const Activities = {
    currentCategory: 'ALL',
    activitiesList: [],

    async loadActivities(category = 'ALL') {
        this.currentCategory = category;
        const grid = document.getElementById('activitiesGrid');
        if (!grid) return;

        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: #64748b;">Loading verified activities...</div>';

        try {
            const data = await API.getActivities(category);
            this.activitiesList = data || [];
            this.renderActivities();
        } catch (err) {
            grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: #ef4444;">Failed to load activities: ${err.message}</div>`;
        }
    },

    renderActivities() {
        const grid = document.getElementById('activitiesGrid');
        if (!grid) return;

        if (this.activitiesList.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 4rem 1rem; background: white; border-radius: 12px; border: 1px dashed #cbd5e1;">
                    <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🎯</div>
                    <h3 style="font-weight: 700; margin-bottom: 0.3rem;">No activities found</h3>
                    <p style="color: #64748b; font-size: 0.9rem; margin-bottom: 1rem;">Be the first verified member to organize a real-world activity in this category!</p>
                    <button class="btn-primary" onclick="Activities.openCreateModal()">+ Create Activity</button>
                </div>
            `;
            return;
        }

        grid.innerHTML = this.activitiesList.map(act => {
            const dateStr = act.activityDate ? new Date(act.activityDate).toLocaleDateString(undefined, {
                weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
            }) : 'Flexible timing';

            const catClass = `tag-${(act.category || 'social').toLowerCase()}`;
            const hostTrust = act.creator ? act.creator.trustScore || 50 : 50;
            const hostTier = act.creator ? act.creator.trustTier || 'BRONZE' : 'BRONZE';
            const tierClass = `tier-${hostTier.toLowerCase()}`;
            const minTrust = act.minTrustScore || 50;

            const isHost = App.currentUser && act.creator && App.currentUser.id === act.creator.id;
            const isCompleted = act.status === 'COMPLETED';

            let actionBtn = '';
            if (isCompleted) {
                actionBtn = `<span style="font-size: 0.8rem; font-weight: 700; color: #059669; background: #ecfdf5; padding: 4px 10px; border-radius: 9999px;">✓ Completed</span>`;
            } else if (isHost) {
                actionBtn = `<button class="btn-secondary" style="font-size: 0.8rem; padding: 0.4rem 0.8rem;" onclick="Activities.completeActivity(${act.id})">Mark Completed</button>`;
            } else {
                actionBtn = `<button class="btn-primary" style="font-size: 0.85rem; padding: 0.45rem 1rem;" onclick="Activities.joinActivity(${act.id}, ${minTrust})">Join Activity</button>`;
            }

            return `
                <div class="activity-card">
                    <div>
                        <div class="activity-top">
                            <span class="category-tag ${catClass}">${act.category}</span>
                            <span class="min-trust-badge" title="Minimum Trust Score required to join">
                                🛡️ Min Trust: <strong>${minTrust}</strong>
                            </span>
                        </div>
                        <h3 class="activity-title">${this.escapeHtml(act.title)}</h3>
                        <p class="activity-desc">${this.escapeHtml(act.description)}</p>
                    </div>

                    <div>
                        <div class="activity-meta">
                            <div class="meta-row">
                                <span>📍</span>
                                <span><strong>${this.escapeHtml(act.venue || 'TBD')}</strong> (${act.city || 'Local'})</span>
                            </div>
                            <div class="meta-row">
                                <span>🗓️</span>
                                <span>${dateStr}</span>
                            </div>
                            <div class="meta-row">
                                <span>👥</span>
                                <span>Max ${act.maxParticipants || 6} participants • Budget: <strong>${act.budget || 'FREE'}</strong></span>
                            </div>
                        </div>

                        <div class="activity-footer">
                            <div class="host-info">
                                <div class="host-avatar">${(act.creator?.fullName || 'U').charAt(0)}</div>
                                <div>
                                    <div style="font-size: 0.85rem; font-weight: 600;">${this.escapeHtml(act.creator?.fullName || 'Host')}</div>
                                    <div style="display: flex; align-items: center; gap: 4px;">
                                        <span class="trust-badge-pill ${tierClass}" style="font-size: 0.65rem;">${hostTier} ${hostTrust}</span>
                                        ${act.creator?.locationVerified ? '<span style="font-size: 0.7rem;" title="GPS Verified">📍</span>' : ''}
                                    </div>
                                </div>
                            </div>
                            <div>${actionBtn}</div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    async joinActivity(id, minTrust) {
        if (!App.currentUser) {
            App.showToast('Please sign up or log in with a verified account to join activities', 'error');
            App.switchTab('verify');
            return;
        }

        const userScore = App.currentUser.trustScore || 0;
        if (userScore < minTrust) {
            App.showTrustWarningModal(userScore, minTrust);
            return;
        }

        try {
            await API.joinActivity(id);
            App.showToast('🎉 Successfully requested to join activity! The host has been notified.', 'success');
            this.loadActivities(this.currentCategory);
            App.refreshUserData();
        } catch (err) {
            App.showToast(err.message, 'error');
        }
    },

    async completeActivity(id) {
        if (!confirm('Mark this activity as completed? This unlocks peer ratings and vouching.')) return;
        try {
            await API.completeActivity(id);
            App.showToast('Activity marked as completed! You can now rate your companions.', 'success');
            this.loadActivities(this.currentCategory);
            App.refreshUserData();
        } catch (err) {
            App.showToast(err.message, 'error');
        }
    },

    openCreateModal() {
        if (!App.currentUser) {
            App.showToast('Please verify your account to create activities', 'error');
            App.switchTab('verify');
            return;
        }
        document.getElementById('createActivityModal').classList.add('active');
    },

    closeCreateModal() {
        document.getElementById('createActivityModal').classList.remove('active');
    },

    async handleCreateSubmit(e) {
        e.preventDefault();
        const title = document.getElementById('act_title').value.trim();
        const description = document.getElementById('act_desc').value.trim();
        const category = document.getElementById('act_cat').value;
        const venue = document.getElementById('act_venue').value.trim();
        const minTrustScore = parseInt(document.getElementById('act_minTrust').value) || 50;
        const maxParticipants = parseInt(document.getElementById('act_maxPart').value) || 5;
        const budget = document.getElementById('act_budget').value;
        const activityDate = document.getElementById('act_date').value;

        const submitBtn = document.getElementById('submitActBtn');
        submitBtn.disabled = true;
        submitBtn.innerText = 'Scanning with AI Safety Layer...';

        try {
            await API.createActivity({
                title,
                description,
                category,
                venue,
                activityDate: activityDate || null,
                minTrustScore,
                maxParticipants,
                budget
            });

            this.closeCreateModal();
            document.getElementById('createActivityForm').reset();
            App.showToast('✅ Activity verified & published to Trust Circle!', 'success');

            // Show the feed unfiltered so the new activity is visible whatever its category.
            document.querySelectorAll('.cat-chip').forEach(c =>
                c.classList.toggle('active', c.dataset.cat === 'ALL'));
            await this.loadActivities('ALL');
            App.refreshUserData();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerText = 'Publish Activity';
        }
    },

    escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
    }
};
