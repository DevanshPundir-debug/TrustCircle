package TrustCircle.Project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "safety_alerts")
public class SafetyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @Column(nullable = false)
    private String alertType; // "GPS_SPOOFING", "SUSPICIOUS_CONTENT", "RAPID_BEHAVIOR", "LOW_TRUST_THRESHOLD", "PEER_REPORT"

    @Column(nullable = false)
    private String riskLevel; // "LOW", "MEDIUM", "HIGH", "CRITICAL"

    @Column(length = 2000, nullable = false)
    private String details;

    private boolean isResolved;
    private String resolutionNotes;

    private LocalDateTime createdAt;

    public SafetyAlert() {
        this.createdAt = LocalDateTime.now();
        this.isResolved = false;
        this.riskLevel = "MEDIUM";
    }

    public SafetyAlert(User user, Activity activity, String alertType, String riskLevel, String details) {
        this.user = user;
        this.activity = activity;
        this.alertType = alertType;
        this.riskLevel = riskLevel;
        this.details = details;
        this.createdAt = LocalDateTime.now();
        this.isResolved = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { isResolved = resolved; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
