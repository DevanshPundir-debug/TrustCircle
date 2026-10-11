package TrustCircle.Project.controller;

import TrustCircle.Project.dto.PeerReviewRequest;
import TrustCircle.Project.model.Activity;
import TrustCircle.Project.model.PeerReview;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.ActivityRepository;
import TrustCircle.Project.repository.PeerReviewRepository;
import TrustCircle.Project.repository.UserRepository;
import TrustCircle.Project.service.DynamicTrustService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trust")
public class TrustController {

    @Autowired
    private DynamicTrustService dynamicTrustService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private PeerReviewRepository peerReviewRepository;

    @GetMapping("/scorecard")
    public ResponseEntity<?> getScorecard(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Authentication required");
            return ResponseEntity.status(401).body(err);
        }

        // Recalculate latest
        User updated = dynamicTrustService.recalculateTrustScore(currentUser);

        Map<String, Object> scorecard = new HashMap<>();
        scorecard.put("trustScore", updated.getTrustScore());
        scorecard.put("trustTier", updated.getTrustTier());
        scorecard.put("isLocationVerified", updated.isLocationVerified());
        scorecard.put("isIdentityVerified", updated.isIdentityVerified());
        scorecard.put("locationVerificationNotes", updated.getLocationVerificationNotes());
        scorecard.put("activitiesAttended", updated.getActivitiesAttended());
        scorecard.put("activitiesHosted", updated.getActivitiesHosted());
        scorecard.put("peerRatingAvg", updated.getPeerRatingAvg());
        scorecard.put("ratingsCount", updated.getRatingsCount());
        scorecard.put("badges", updated.getBadges() != null ? updated.getBadges().split(",") : new String[0]);
        scorecard.put("isFlagged", updated.isFlagged());
        scorecard.put("flagReason", updated.getFlagReason());

        return ResponseEntity.ok(scorecard);
    }

    @PostMapping("/review")
    public ResponseEntity<?> submitReview(@RequestBody PeerReviewRequest req, HttpServletRequest request) {
        User reviewer = (User) request.getAttribute("currentUser");
        if (reviewer == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Authentication required");
            return ResponseEntity.status(401).body(err);
        }

        if (reviewer.getId().equals(req.getRevieweeId())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "You cannot rate yourself");
            return ResponseEntity.badRequest().body(err);
        }

        Activity activity = activityRepository.findById(req.getActivityId())
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        User reviewee = userRepository.findById(req.getRevieweeId())
                .orElseThrow(() -> new RuntimeException("Reviewee not found"));

        if (peerReviewRepository.existsByActivityAndReviewerAndReviewee(activity, reviewer, reviewee)) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "You have already submitted a review for this member on this activity");
            return ResponseEntity.badRequest().body(err);
        }

        PeerReview review = new PeerReview();
        review.setActivity(activity);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(req.getRating() != null ? req.getRating() : 5.0);
        review.setPunctualityScore(req.getPunctualityScore() != null ? req.getPunctualityScore() : 5);
        review.setFriendlinessScore(req.getFriendlinessScore() != null ? req.getFriendlinessScore() : 5);
        review.setSafetyScore(req.getSafetyScore() != null ? req.getSafetyScore() : 5);
        review.setFeedback(req.getFeedback());
        review.setBadgeAwarded(req.getBadgeAwarded());

        peerReviewRepository.save(review);

        // Dynamically recalculate reviewee's trust score
        User updatedReviewee = dynamicTrustService.recalculateTrustScore(reviewee);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Review submitted successfully! Dynamic Trust Score updated.");
        resp.put("newTrustScore", updatedReviewee.getTrustScore());
        resp.put("newTier", updatedReviewee.getTrustTier());

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/reviews/{userId}")
    public ResponseEntity<List<PeerReview>> getUserReviews(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(peerReviewRepository.findByRevieweeOrderByCreatedAtDesc(user));
    }
}
