// My Schedule — activities I host, activities I'm on, and requests awaiting my decision
const Scheduled = {
    currentFilter: 'hosted',
    schedule: { hosted: [], participating: [], requests: [] },

    async loadScheduledActivities() {
        this.setLoading();
        try {
            const data = await API.getMySchedule();
            this.schedule = {
                hosted: data.hosted || [],
                participating: data.participating || [],
                requests: data.requests || []
            };
            this.render();
        } catch (err) {
            const message = `Could not load your schedule: ${this.escapeHtml(err.message)}`;
            ['hostedActivitiesList', 'joinedActivitiesList', 'requestsList'].forEach(id => {
                const el = document.getElementById(id);
                if (el) el.innerHTML = `<p style="color:#ef4444;">${message}</p>`;
            });
        }
    },

    setLoading() {
        const loading = '<p style="color:#94a3b8;">Loading…</p>';
        ['hostedActivitiesList', 'joinedActivitiesList', 'requestsList'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.innerHTML = loading;
        });
    },

    render() {
        this.renderHosted();
        this.renderParticipating();
        this.renderRequests();
        this.updateRequestBadge();
        this.switchFilter(this.currentFilter);
    },

    renderHosted() {
        const container = document.getElementById('hostedActivitiesList');
        if (!container) return;

        if (this.schedule.hosted.length === 0) {
            container.innerHTML = this.emptyState(
                'You are not hosting anything yet.',
                'Host an activity',
                'Activities.openCreateModal()'
            );
            return;
        }

        container.innerHTML = this.schedule.hosted.map(act => {
            const pending = this.schedule.requests.filter(r => r.activity && r.activity.id === act.id).length;
            const pendingNote = pending > 0
                ? `<div style="margin-top:0.4rem;font-size:0.82rem;color:#b45309;font-weight:600;">
                     ${pending} join request${pending > 1 ? 's' : ''} awaiting your decision</div>`
                : '';
            return this.card({
                title: act.title,
                subtitle: this.escapeHtml(act.description || ''),
                meta: this.metaLine(act),
                badgeText: act.status === 'COMPLETED' ? 'COMPLETED' : 'HOSTING',
                badgeBg: '#e0f2fe',
                badgeColor: '#0369a1',
                extra: pendingNote
            });
        }).join('');
    },

    renderParticipating() {
        const container = document.getElementById('joinedActivitiesList');
        if (!container) return;

        if (this.schedule.participating.length === 0) {
            container.innerHTML = this.emptyState(
                "You haven't joined any activities yet.",
                'Browse activities',
                "App.switchTab('activities')"
            );
            return;
        }

        container.innerHTML = this.schedule.participating.map(p => {
            const act = p.activity || {};
            const awaiting = p.status === 'PENDING';
            return this.card({
                title: act.title,
                subtitle: `Hosted by <strong>${this.escapeHtml(act.creator ? act.creator.fullName : 'Unknown')}</strong>`,
                meta: this.metaLine(act),
                badgeText: awaiting ? 'AWAITING APPROVAL' : 'CONFIRMED',
                badgeBg: awaiting ? '#fef3c7' : '#dcfce7',
                badgeColor: awaiting ? '#92400e' : '#166534'
            });
        }).join('');
    },

    renderRequests() {
        const container = document.getElementById('requestsList');
        if (!container) return;

        if (this.schedule.requests.length === 0) {
            container.innerHTML = '<p style="color:#94a3b8;">No one is waiting on your approval right now.</p>';
            return;
        }

        container.innerHTML = this.schedule.requests.map(req => {
            const requester = req.user || {};
            const act = req.activity || {};
            const tier = requester.trustTier || 'BRONZE';
            return `
                <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:1rem;display:flex;justify-content:space-between;align-items:center;gap:1rem;flex-wrap:wrap;">
                    <div>
                        <h4 style="margin:0 0 0.3rem 0;color:#1e293b;">
                            ${this.escapeHtml(requester.fullName || 'A member')} wants to join
                        </h4>
                        <p style="margin:0;font-size:0.88rem;color:#64748b;">${this.escapeHtml(act.title || '')}</p>
                        <div style="margin-top:0.4rem;font-size:0.82rem;color:#64748b;">
                            ⭐ Trust ${requester.trustScore != null ? requester.trustScore : '—'} (${this.escapeHtml(tier)})
                            ${requester.identityVerified ? ' • ✅ Identity verified' : ' • ⚠️ Unverified'}
                        </div>
                    </div>
                    <div style="display:flex;gap:0.5rem;">
                        <button onclick="Scheduled.respond(${req.id}, 'approve')"
                            style="padding:0.4rem 1rem;background:#10b981;color:white;border:none;border-radius:6px;cursor:pointer;font-weight:600;">✓ Accept</button>
                        <button onclick="Scheduled.respond(${req.id}, 'reject')"
                            style="padding:0.4rem 1rem;background:#ef4444;color:white;border:none;border-radius:6px;cursor:pointer;font-weight:600;">✕ Decline</button>
                    </div>
                </div>
            `;
        }).join('');
    },

    async respond(participantId, decision) {
        try {
            await API.respondToJoinRequest(participantId, decision);
            App.showToast(decision === 'approve' ? 'Request accepted' : 'Request declined', 'success');
            await this.loadScheduledActivities();
        } catch (err) {
            App.showToast(err.message, 'error');
        }
    },

    updateRequestBadge() {
        const btn = document.querySelector('[data-schedule-filter="requests"]');
        if (!btn) return;
        const count = this.schedule.requests.length;
        btn.innerHTML = count > 0 ? `📨 Join Requests (${count})` : '📨 Join Requests';
    },

    switchFilter(filter) {
        this.currentFilter = filter;

        document.querySelectorAll('[data-schedule-filter]').forEach(btn => {
            const isActive = btn.dataset.scheduleFilter === filter;
            btn.style.background = isActive ? 'var(--primary)' : '#f1f5f9';
            btn.style.color = isActive ? 'white' : '#1e293b';
        });

        const sections = {
            hosted: 'scheduledHostedSection',
            joined: 'scheduledJoinedSection',
            requests: 'scheduledRequestsSection'
        };
        Object.entries(sections).forEach(([key, id]) => {
            const el = document.getElementById(id);
            if (el) el.style.display = key === filter ? 'block' : 'none';
        });
    },

    metaLine(act) {
        const when = act.activityDate
            ? new Date(act.activityDate).toLocaleString(undefined, {
                weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
              })
            : 'Flexible timing';
        return `📍 ${this.escapeHtml(act.venue || 'Venue TBA')} • 📅 ${when}`;
    },

    card({ title, subtitle, meta, badgeText, badgeBg, badgeColor, extra = '' }) {
        return `
            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:1rem;display:flex;justify-content:space-between;align-items:flex-start;gap:1rem;flex-wrap:wrap;">
                <div style="flex:1;min-width:240px;">
                    <h4 style="margin:0 0 0.3rem 0;color:#1e293b;">${this.escapeHtml(title || 'Untitled activity')}</h4>
                    <p style="margin:0;font-size:0.88rem;color:#64748b;">${subtitle}</p>
                    <div style="margin-top:0.45rem;font-size:0.82rem;color:#64748b;">${meta}</div>
                    ${extra}
                </div>
                <span style="background:${badgeBg};color:${badgeColor};padding:0.25rem 0.75rem;border-radius:12px;font-size:0.75rem;font-weight:700;white-space:nowrap;">
                    ${badgeText}
                </span>
            </div>
        `;
    },

    emptyState(message, ctaLabel, ctaAction) {
        return `<p style="color:#94a3b8;">${message}
            <a href="#" onclick="${ctaAction}; return false;" style="color:var(--primary);font-weight:600;">${ctaLabel}</a>
        </p>`;
    },

    escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#039;');
    }
};
