package TrustCircle.Project.dto;

public class PeerReviewRequest {
    private Long activityId;
    private Long revieweeId;
    private Double rating; // 1.0 - 5.0
    private Integer punctualityScore;
    private Integer friendlinessScore;
    private Integer safetyScore;
    private String feedback;
    private String badgeAwarded;

    public PeerReviewRequest() {}

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }

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
}
