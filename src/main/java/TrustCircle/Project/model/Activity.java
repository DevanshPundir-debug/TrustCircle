package TrustCircle.Project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String description;

    @Column(nullable = false)
    private String category; // "SPORTS", "STUDY", "TRAVEL", "EVENTS", "SOCIAL"

    @Column(nullable = false)
    private String venue;

    private String city;
    private Double latitude;
    private Double longitude;

    private LocalDateTime activityDate;

    private String budget; // "FREE", "LOW", "MEDIUM", "HIGH"

    private Integer minTrustScore; // e.g. 50, 70, 80

    private Integer maxParticipants;

    private String status; // "OPEN", "FULL", "COMPLETED", "CANCELLED"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private LocalDateTime createdAt;

    public Activity() {
        this.createdAt = LocalDateTime.now();
        this.status = "OPEN";
        this.minTrustScore = 50;
        this.maxParticipants = 5;
        this.budget = "FREE";
    }

    @PrePersist
    public void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "OPEN";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
