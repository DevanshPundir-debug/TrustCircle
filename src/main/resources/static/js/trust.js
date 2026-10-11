// Dynamic Trust Scorecard & Peer Reviews
const Trust = {
    async loadScorecard() {
        if (!App.currentUser) {
            document.getElementById('trustContent').innerHTML = `
                <div style="text-align: center; padding: 4rem 1rem; background: white; border-radius: 12px; border: 1px dashed #cbd5e1;">
                    <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🛡️</div>
                    <h3 style="font-weight: 700; margin-bottom: 0.3rem;">Personalized Dynamic Trust Scorecard</h3>
                    <p style="color: #64748b; font-size: 0.9rem; margin-bottom: 1rem;">Complete identity verification or log in to inspect your live credibility breakdown.</p>
                    <button class="btn-primary" onclick="App.switchTab('verify')">Verify My Identity</button>
                </div>
            `;
            return;
        }

        try {
            const card = await API.getScorecard();
            this.renderScorecard(card);
            this.loadReviews(App.currentUser.id);
        } catch (err) {
            console.error('Error fetching scorecard:', err);
        }
    },

    renderScorecard(card) {
        const container = document.getElementById('trustContent');
        if (!container) return;

        const score = card.trustScore || 50;
        const tier = card.trustTier || 'BRONZE';
        const tierClass = `tier-${tier.toLowerCase()}`;

        // Factors calculation
        const verifPts = (card.isIdentityVerified ? 20 : 0) + (card.isLocationVerified ? 15 : 0);
        const attendPts = Math.min(25, (card.activitiesAttended + card.activitiesHosted) * 5);
        const ratingAvg = card.peerRatingAvg || 5.0;
        const reviewPts = Math.min(30, Math.round((ratingAvg / 5.0) * 30));
        const badgeList = card.badges || [];
        const badgePts = Math.min(10, badgeList.length * 3);

        const badgesHtml = badgeList.map(b =>
            `<span style="background: #eef2ff; color: #4338ca; border: 1px solid #c7d2fe; font-size: 0.8rem; font-weight: 600; padding: 4px 10px; border-radius: 9999px;">🏅 ${this.escapeHtml(b)}</span>`
        ).join(' ');

        container.innerHTML = `
            <div class="scorecard-grid">
                <!-- Gauge Column -->
                <div class="gauge-card">
                    <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.5rem;">Dynamic Trust Metric</h3>
                    <p style="font-size: 0.85rem; color: #64748b;">Continuously calculated from biometric sensors, peer audits, and offline attendance.</p>

                    <div class="trust-meter-circle" style="--score: ${score};">
                        <div class="trust-meter-inner">
                            <span class="meter-num">${score}</span>
                            <span class="meter-label">/ 100</span>
                        </div>
                    </div>

                    <div style="margin-bottom: 1rem;">
                        <span class="trust-badge-pill ${tierClass}" style="font-size: 0.95rem; padding: 4px 14px;">
                            ${tier} TIER
                        </span>
                    </div>

                    <div style="font-size: 0.85rem; color: #475569; text-align: left; background: #f8fafc; padding: 1rem; border-radius: 8px;">
                        <div>📍 <strong>Location:</strong> ${card.isLocationVerified ? '<span style="color: #059669; font-weight: 600;">Verified GPS sensor</span>' : '<span style="color: #94a3b8;">Manual entry</span>'}</div>
                        <div style="margin-top: 4px;">🤝 <strong>Attended / Hosted:</strong> ${card.activitiesAttended} / ${card.activitiesHosted} activities</div>
                        <div style="margin-top: 4px;">⭐ <strong>Peer Rating:</strong> ${ratingAvg.toFixed(1)} / 5.0 (${card.ratingsCount} reviews)</div>
                    </div>

                    <div style="margin-top: 1.25rem;">
                        <h4 style="font-size: 0.85rem; font-weight: 700; color: #334155; text-align: left; margin-bottom: 0.5rem;">Earned Badges:</h4>
                        <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
                            ${badgesHtml || '<span style="font-size: 0.8rem; color: #94a3b8;">Attend your first meetup to earn badges!</span>'}
                        </div>
                    </div>
                </div>

                <!-- Breakdown Column -->
                <div class="factor-breakdown-card">
                    <h3 style="font-size: 1.2rem; font-weight: 700; margin-bottom: 0.3rem;">Algorithmic Credibility Breakdown</h3>
                    <p style="font-size: 0.88rem; color: #64748b; margin-bottom: 1.5rem;">Transparent multi-factor computation ensures offline accountability with zero fake accounts.</p>

                    <!-- Factor 1: Verification Baseline -->
                    <div class="factor-row">
                        <div class="factor-header">
                            <span>1. Multi-Step Identity & GPS Sensor Verification (35 pts max)</span>
                            <span style="color: #4f46e5;">${verifPts} / 35 pts</span>
                        </div>
                        <div class="factor-progress-bg">
                            <div class="factor-progress-fill" style="width: ${(verifPts / 35) * 100}%; background: #4f46e5;"></div>
                        </div>
                        <div style="font-size: 0.8rem; color: #64748b; margin-top: 3px;">
                            ${card.locationVerificationNotes || 'Location sensor confirmed.'}
                        </div>
                    </div>

                    <!-- Factor 2: Participation History -->
                    <div class="factor-row">
                        <div class="factor-header">
                            <span>2. Real-World Attendance & Organizing Reliability (25 pts max)</span>
                            <span style="color: #06b6d4;">${attendPts} / 25 pts</span>
                        </div>
                        <div class="factor-progress-bg">
                            <div class="factor-progress-fill" style="width: ${(attendPts / 25) * 100}%; background: #06b6d4;"></div>
                        </div>
                        <div style="font-size: 0.8rem; color: #64748b; margin-top: 3px;">
                            Earn +5 pts per completed real-world meetup. No-shows trigger automatic penalties.
                        </div>
                    </div>

                    <!-- Factor 3: Peer Ratings -->
                    <div class="factor-row">
                        <div class="factor-header">
                            <span>3. Verified Peer Reviews: Punctuality & Safety (30 pts max)</span>
                            <span style="color: #10b981;">${reviewPts} / 30 pts</span>
                        </div>
                        <div class="factor-progress-bg">
                            <div class="factor-progress-fill" style="width: ${(reviewPts / 30) * 100}%; background: #10b981;"></div>
                        </div>
                        <div style="font-size: 0.8rem; color: #64748b; margin-top: 3px;">
                            Current Peer Rating: <strong>${ratingAvg.toFixed(1)} / 5.0</strong> across ${card.ratingsCount} verified reviews.
                        </div>
                    </div>

                    <!-- Factor 4: Community Endorsements -->
                    <div class="factor-row">
                        <div class="factor-header">
                            <span>4. Community Badges & Circle Endorsements (10 pts max)</span>
                            <span style="color: #8b5cf6;">${badgePts} / 10 pts</span>
                        </div>
                        <div class="factor-progress-bg">
                            <div class="factor-progress-fill" style="width: ${(badgePts / 10) * 100}%; background: #8b5cf6;"></div>
                        </div>
                        <div style="font-size: 0.8rem; color: #64748b; margin-top: 3px;">
                            ${badgeList.length} verified platform endorsements active.
                        </div>
                    </div>

                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 1.5rem 0;" />

                    <!-- Verified Reviews Section -->
                    <h4 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 0.75rem;">Verified Peer Vouches</h4>
                    <div id="userReviewsList" style="display: flex; flex-direction: column; gap: 0.75rem;">
                        <span style="color: #94a3b8; font-size: 0.85rem;">Loading peer testimonials...</span>
                    </div>
                </div>
            </div>
        `;
    },

    async loadReviews(userId) {
        const box = document.getElementById('userReviewsList');
        if (!box) return;

        try {
            const list = await API.getUserReviews(userId);
            if (!list || list.length === 0) {
                box.innerHTML = '<span style="color: #94a3b8; font-size: 0.85rem;">No reviews yet. Join or host an activity to receive verified reviews!</span>';
                return;
            }

            box.innerHTML = list.map(r => `
                <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0.85rem;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                        <span style="font-size: 0.85rem; font-weight: 700; color: #1e293b;">
                            From: ${this.escapeHtml(r.reviewer?.fullName || 'Verified Member')}
                        </span>
                        <span style="color: #eab308; font-size: 0.85rem; font-weight: 700;">★ ${r.rating.toFixed(1)}</span>
                    </div>
                    <p style="font-size: 0.85rem; color: #475569; margin-bottom: 4px;">"${this.escapeHtml(r.feedback || 'Great companion!')}"</p>
                    <div style="font-size: 0.75rem; color: #64748b; display: flex; gap: 8px;">
                        <span>⏱️ Punctuality: ${r.punctualityScore || 5}/5</span>
                        <span>🤝 Friendliness: ${r.friendlinessScore || 5}/5</span>
                        <span>🛡️ Safety: ${r.safetyScore || 5}/5</span>
                    </div>
                </div>
            `).join('');
        } catch (err) {
            box.innerHTML = `<span style="color: #ef4444; font-size: 0.85rem;">Could not load reviews.</span>`;
        }
    },

    openPeerReviewModal(targetUserId, targetName) {
        if (!App.currentUser) {
            App.showToast('Please log in to submit a peer review', 'error');
            App.switchTab('verify');
            return;
        }

        document.getElementById('pr_targetUserId').value = targetUserId;
        document.getElementById('pr_targetUserName').innerText = targetName;
        document.getElementById('peerReviewModal').classList.add('active');
    },

    closePeerReviewModal() {
        document.getElementById('peerReviewModal').classList.remove('active');
    },

    async handleReviewSubmit(e) {
        e.preventDefault();
        const targetUserId = parseInt(document.getElementById('pr_targetUserId').value);
        const rating = parseFloat(document.getElementById('pr_rating').value) || 5.0;
        const punctualityScore = parseInt(document.getElementById('pr_punctuality').value) || 5;
        const friendlinessScore = parseInt(document.getElementById('pr_friendliness').value) || 5;
        const safetyScore = parseInt(document.getElementById('pr_safety').value) || 5;
        const feedback = document.getElementById('pr_feedback').value.trim();
        const badgeAwarded = document.getElementById('pr_badge').value;

        // Use the first activity ID or 1 for demo purposes
        const activityId = 1;

        try {
            const resp = await API.submitPeerReview({
                activityId,
                revieweeId: targetUserId,
                rating,
                punctualityScore,
                friendlinessScore,
                safetyScore,
                feedback,
                badgeAwarded
            });

            this.closePeerReviewModal();
            App.showToast(`🎉 Review recorded! Companion's new trust score is ${resp.newTrustScore} (${resp.newTier})`, 'success');
            App.refreshUserData();
            this.loadScorecard();
        } catch (err) {
            App.showToast(err.message, 'error');
        }
    },

    escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
    }
};
