package TrustCircle.Project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "peer_reviews")
public class PeerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @Column(nullable = false)
    private Double rating; // 1.0 - 5.0

    private Integer punctualityScore; // 1 - 5
    private Integer friendlinessScore; // 1 - 5
    private Integer safetyScore; // 1 - 5

    @Column(length = 1000)
    private String feedback;

    private String badgeAwarded; // e.g. "Punctual Master", "Friendly Soul", "Rock-Solid Companion"

    private LocalDateTime createdAt;

    public PeerReview() {
        this.createdAt = LocalDateTime.now();
        this.rating = 5.0;
        this.punctualityScore = 5;
        this.friendlinessScore = 5;
        this.safetyScore = 5;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) { this.reviewer = reviewer; }

    public User getReviewee() { return reviewee; }
    public void setReviewee(User reviewee) { this.reviewee = reviewee; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getPunctualityScore() { return punctualityScore; }
    public void setPunctualityScore(Integer punctualityScore) { this.punctualityScore = punctualityScore; }

    public Integer getFriendlinessScore() { return friendlinessScore; }
    public void setFriendlinessScore(Integer friendlinessScore) { this.friendlinessScore = friendlinessScore; }

    public Integer getSafetyScore() { return safetyScore; }
    public void setSafetyScore(Integer safetyScore) { this.safetyScore = safetyScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getBadgeAwarded() { return badgeAwarded; }
    public void setBadgeAwarded(String badgeAwarded) { this.badgeAwarded = badgeAwarded; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
