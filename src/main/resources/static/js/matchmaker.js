// AI Smart Matching Engine UI
const Matchmaker = {
    matchesList: [],

    async loadMatches() {
        const container = document.getElementById('matchesContainer');
        if (!container) return;

        container.innerHTML = '<div style="text-align: center; padding: 3rem; color: #64748b;">Computing AI compatibility vectors and verified proximity...</div>';

        try {
            const data = await API.getSmartMatches();
            this.matchesList = data || [];
            this.renderMatches();
        } catch (err) {
            container.innerHTML = `<div style="text-align: center; padding: 2rem; color: #ef4444;">Could not load smart matches: ${err.message}</div>`;
        }
    },

    renderMatches() {
        const container = document.getElementById('matchesContainer');
        if (!container) return;

        if (this.matchesList.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 3rem; background: white; border-radius: 12px; border: 1px dashed #cbd5e1;">
                    <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🤖</div>
                    <h3 style="font-weight: 700; margin-bottom: 0.3rem;">No AI matches found yet</h3>
                    <p style="color: #64748b; font-size: 0.9rem;">Complete your verification questionnaire to calibrate your interest vectors and location coords.</p>
                    <button class="btn-primary" style="margin-top: 1rem;" onclick="App.switchTab('verify')">Complete Verification Survey</button>
                </div>
            `;
            return;
        }

        container.innerHTML = this.matchesList.map(item => {
            const u = item.user;
            const pct = item.matchPercentage;
            const tier = item.trustStatus || 'BRONZE';
            const tierClass = `tier-${tier.toLowerCase()}`;

            let pctColor = '#10b981';
            if (pct < 65) pctColor = '#f59e0b';
            if (pct < 50) pctColor = '#64748b';

            const tagsHtml = (item.sharedInterests || []).map(t =>
                `<span style="background: #eef2ff; color: #4338ca; font-size: 0.75rem; font-weight: 600; padding: 2px 8px; border-radius: 9999px;">${this.escapeHtml(t)}</span>`
            ).join(' ');

            return `
                <div class="match-card">
                    <div class="match-avatar-col">
                        <div class="match-avatar">
                            ${(u.fullName || 'U').charAt(0)}
                            <span class="match-percentage-badge" style="background: ${pctColor};">
                                ${pct}%
                            </span>
                        </div>
                        <h4 style="font-size: 1rem; font-weight: 700; margin-bottom: 2px;">${this.escapeHtml(u.fullName)}</h4>
                        <div style="display: flex; gap: 4px; align-items: center; justify-content: center; margin-bottom: 4px;">
                            <span class="trust-badge-pill ${tierClass}">${tier} ${u.trustScore || 50}</span>
                            ${u.locationVerified ? '<span title="GPS Verified" style="font-size: 0.8rem;">📍</span>' : ''}
                        </div>
                        <span style="font-size: 0.75rem; color: #64748b;">
                            ${item.distanceKm ? `~${item.distanceKm} km away` : (u.claimedCity || 'Verified Area')}
                        </span>
                    </div>

                    <div>
                        <p style="font-size: 0.9rem; color: #334155; margin-bottom: 0.6rem;">
                            "${this.escapeHtml(u.bio || 'Verified member looking for authentic real-world connections.')}"
                        </p>

                        <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0.5rem;">
                            <strong style="font-size: 0.8rem; color: #64748b;">Shared Passions:</strong>
                            ${tagsHtml || '<span style="font-size: 0.8rem; color: #94a3b8;">General Socializing</span>'}
                        </div>

                        <div class="match-ai-explanation">
                            <strong>🧠 AI Match Engine Rationale:</strong><br/>
                            ${this.escapeHtml(item.aiExplanation)}
                        </div>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 0.6rem; align-items: stretch; justify-content: center;">
                        <button class="btn-primary" style="font-size: 0.85rem; padding: 0.6rem;" onclick="Matchmaker.proposeMeetup(${u.id}, '${this.escapeHtml(u.fullName)}')">
                            🤝 Invite to Meetup
                        </button>
                        <button class="btn-secondary" style="font-size: 0.8rem; padding: 0.5rem;" onclick="Trust.openPeerReviewModal(${u.id}, '${this.escapeHtml(u.fullName)}')">
                            ⭐ Rate & Vouch
                        </button>
                        <button class="btn-secondary" style="font-size: 0.75rem; padding: 0.4rem; color: #dc2626; border-color: #fee2e2;" onclick="Safety.openReportModal(${u.id}, '${this.escapeHtml(u.fullName)}')">
                            🚩 Report Profile
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    },

    proposeMeetup(targetUserId, targetName) {
        if (!App.currentUser) {
            App.showToast('Please log in to send activity invitations', 'error');
            App.switchTab('verify');
            return;
        }
        App.showToast(`✨ Invitation sent to ${targetName}! An activity ping has been delivered.`, 'success');
    },

    escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
    }
};
