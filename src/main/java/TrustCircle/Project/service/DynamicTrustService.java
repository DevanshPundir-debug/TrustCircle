package TrustCircle.Project.service;

import TrustCircle.Project.model.PeerReview;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.PeerReviewRepository;
import TrustCircle.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DynamicTrustService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PeerReviewRepository peerReviewRepository;

    /**
     * Recomputes the user's dynamic trust score (0 - 100) based on:
     * 1. Verification Baseline (Location verified, Identity verified): up to 35 pts
     * 2. Activity Attendance & Hosting History: up to 25 pts
     * 3. Peer Ratings & Reviews: up to 30 pts
     * 4. Community Vouches / Badges: up to 10 pts
     * Deductions: AI Safety flags / reports: -30 pts
     */
    public User recalculateTrustScore(User user) {
        int score = 0;

        // 1. Verification Baseline (Max 35)
        if (user.isIdentityVerified()) {
            score += 20;
        }
        if (user.isLocationVerified()) {
            score += 15;
        }

        // 2. Activity Attendance & Hosting History (Max 25)
        int activitiesCount = user.getActivitiesAttended() + user.getActivitiesHosted();
        int activityPoints = Math.min(25, activitiesCount * 5);
        score += activityPoints;

        // 3. Peer Ratings & Reviews (Max 30)
        List<PeerReview> reviews = peerReviewRepository.findByRevieweeOrderByCreatedAtDesc(user);
        if (!reviews.isEmpty()) {
            double sumRatings = 0.0;
            int totalPunctuality = 0;
            int totalSafety = 0;
            int totalFriendliness = 0;

            for (PeerReview r : reviews) {
                sumRatings += (r.getRating() != null ? r.getRating() : 5.0);
                totalPunctuality += (r.getPunctualityScore() != null ? r.getPunctualityScore() : 5);
                totalSafety += (r.getSafetyScore() != null ? r.getSafetyScore() : 5);
                totalFriendliness += (r.getFriendlinessScore() != null ? r.getFriendlinessScore() : 5);
            }

            double avgRating = sumRatings / reviews.size();
            user.setPeerRatingAvg(Math.round(avgRating * 10.0) / 10.0);
            user.setRatingsCount(reviews.size());

            // Rating score scaled to 30 pts
            // 5.0 -> 30, 4.0 -> 24, 3.0 -> 18, 2.0 -> 12, 1.0 -> 6
            int reviewPoints = (int) Math.round((avgRating / 5.0) * 30.0);
            score += reviewPoints;
        } else {
            // New user grace points if verified
            if (user.isIdentityVerified()) {
                score += 15; // neutral baseline review credit
            }
        }

        // 4. Badges & Endorsements (Max 10)
        List<String> badgeList = new ArrayList<>();
        if (user.isLocationVerified()) {
            badgeList.add("Location Verified");
        }
        if (user.isIdentityVerified()) {
            badgeList.add("Verified Explorer");
        }
        if (user.getActivitiesHosted() >= 2) {
            badgeList.add("Active Host");
        }
        if (user.getActivitiesAttended() >= 3) {
            badgeList.add("Trusted Companion");
        }
        if (user.getPeerRatingAvg() != null && user.getPeerRatingAvg() >= 4.8 && user.getRatingsCount() >= 2) {
            badgeList.add("Top Rated ⭐");
        }
        score += Math.min(10, badgeList.size() * 3);

        // Deductions if flagged
        if (user.isFlagged()) {
            score = Math.max(10, score - 35);
        }

        // Cap score between 0 and 100
        score = Math.max(0, Math.min(100, score));
        user.setTrustScore(score);

        // Determine Trust Tier
        if (score >= 90) {
            user.setTrustTier("PLATINUM");
        } else if (score >= 75) {
            user.setTrustTier("GOLD");
        } else if (score >= 60) {
            user.setTrustTier("SILVER");
        } else {
            user.setTrustTier("BRONZE");
        }

        user.setBadges(String.join(",", badgeList));
        return userRepository.save(user);
    }
}
