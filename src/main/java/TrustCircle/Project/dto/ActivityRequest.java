package TrustCircle.Project.dto;

import java.time.LocalDateTime;

public class ActivityRequest {
    private String title;
    private String description;
    private String category;
    private String venue;
    private String city;
    private Double latitude;
    private Double longitude;
    private LocalDateTime activityDate;
    private String budget;
    private Integer minTrustScore;
    private Integer maxParticipants;

    public ActivityRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDateTime activityDate) { this.activityDate = activityDate; }

    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }

    public Integer getMinTrustScore() { return minTrustScore; }
    public void setMinTrustScore(Integer minTrustScore) { this.minTrustScore = minTrustScore; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
}
