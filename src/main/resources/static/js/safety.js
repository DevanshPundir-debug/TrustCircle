// AI Safety Monitoring & Moderation Module
const Safety = {
    alertsList: [],

    async loadAlerts() {
        const container = document.getElementById('safetyAlertsList');
        if (!container) return;

        container.innerHTML = '<div style="text-align: center; padding: 2rem; color: #64748b;">Scanning platform safety telemetry...</div>';

        try {
            const data = await API.getSafetyAlerts();
            this.alertsList = data || [];
            this.renderAlerts();
        } catch (err) {
            container.innerHTML = `<div style="text-align: center; padding: 2rem; color: #ef4444;">Failed to load alerts: ${err.message}</div>`;
        }
    },

    renderAlerts() {
        const container = document.getElementById('safetyAlertsList');
        if (!container) return;

        if (this.alertsList.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 3rem 1rem; background: white; border-radius: 12px; border: 1px dashed #a7f3d0;">
                    <div style="font-size: 2.2rem; margin-bottom: 0.5rem;">🛡️</div>
                    <h3 style="font-weight: 700; color: #065f46; margin-bottom: 0.3rem;">All Systems Secure</h3>
                    <p style="color: #64748b; font-size: 0.9rem;">The AI Safety layer has detected zero active anomalies or suspicious accounts.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = this.alertsList.map(a => {
            const riskClass = `risk-${a.riskLevel || 'LOW'}`;
            const timeStr = a.createdAt ? new Date(a.createdAt).toLocaleString() : 'Just now';

            let statusBadge = '';
            if (a.resolved) {
                statusBadge = `<span style="background: #ecfdf5; color: #059669; font-size: 0.75rem; font-weight: 700; padding: 2px 8px; border-radius: 9999px;">✓ Resolved</span>`;
            } else {
                statusBadge = `<span style="background: #fef2f2; color: #dc2626; font-size: 0.75rem; font-weight: 700; padding: 2px 8px; border-radius: 9999px;">⚠️ Flagged Active</span>`;
            }

            return `
                <div class="alert-item ${riskClass}">
                    <div>
                        <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 4px;">
                            <strong style="font-size: 0.95rem; color: #0f172a;">${this.formatAlertType(a.alertType)}</strong>
                            <span style="font-size: 0.75rem; font-weight: 700; padding: 2px 6px; border-radius: 4px; background: #f1f5f9;">
                                ${a.riskLevel} RISK
                            </span>
                            ${statusBadge}
                        </div>
                        <p style="font-size: 0.88rem; color: #334155; margin-bottom: 4px;">
                            ${this.escapeHtml(a.details)}
                        </p>
                        <div style="font-size: 0.75rem; color: #94a3b8;">
                            Target Member: <strong>${this.escapeHtml(a.user?.fullName || 'Anonymous')}</strong> • ${timeStr}
                        </div>
                    </div>

                    <div>
                        ${!a.resolved ? `
                            <button class="btn-secondary" style="font-size: 0.8rem; padding: 0.4rem 0.8rem;" onclick="Safety.resolveAlert(${a.id})">
                                Mark Resolved
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
        }).join('');
    },

    formatAlertType(type) {
        switch (type) {
            case 'GPS_SPOOFING': return '🛰️ GPS Spoofing / Anomaly Detected';
            case 'SUSPICIOUS_CONTENT': return '🤖 Phishing / Scam Keyword Intercepted';
            case 'PEER_REPORT': return '🚩 Peer Safety Report Filed';
            case 'RAPID_BEHAVIOR': return '⚡ Bot-like Rapid Action Throttled';
            default: return '⚠️ Safety Incident';
        }
    },

    async resolveAlert(id) {
        const notes = prompt('Enter resolution or audit notes:', 'Verified safe by community moderator.');
        if (!notes) return;

        try {
            await API.resolveAlert(id, notes);
            App.showToast('Incident marked as resolved. Account restriction lifted if applicable.', 'success');
            this.loadAlerts();
            App.refreshUserData();
        } catch (err) {
            App.showToast(err.message, 'error');
        }
    },

    openReportModal(userId, userName) {
        document.getElementById('rep_userId').value = userId;
        document.getElementById('rep_userName').innerText = userName;
        document.getElementById('reportModal').classList.add('active');
    },

    closeReportModal() {
        document.getElementById('reportModal').classList.remove('active');
    },

    async handleReportSubmit(e) {
        e.preventDefault();
        const targetUserId = parseInt(document.getElementById('rep_userId').value);
        const reason = document.getElementById('rep_reason').value.trim();

        try {
            await API.reportEntity(targetUserId, null, reason);
            this.closeReportModal();
            App.showToast('🚨 Safety incident reported! The AI Safety layer has updated the user trust score.', 'success');
            this.loadAlerts();
            App.refreshUserData();
        } catch (err) {
            App.showToast(err.message, 'error');
        }
    },

    escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
    }
};
