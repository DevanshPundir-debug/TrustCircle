package TrustCircle.Project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Column(length = 1000)
    private String bio;

    // Location & Geo Verification
    private String claimedCity;
    private Double verifiedLatitude;
    private Double verifiedLongitude;
    private Double locationAccuracyMeters;
    private boolean isLocationVerified;
    private String locationVerificationNotes;

    // Identity Verification (Google Forms questionnaire result)
    private boolean isIdentityVerified;
    private String verificationStatus; // "VERIFIED", "PENDING", "REJECTED"
    private LocalDateTime verifiedAt;

    // Dynamic Trust Score
    private Integer trustScore; // 0 - 100
    private String trustTier; // "BRONZE", "SILVER", "GOLD", "PLATINUM"

    // Activity & Reputation Metrics
    private int activitiesAttended;
    private int activitiesHosted;
    private Double peerRatingAvg; // 0.0 - 5.0
    private int ratingsCount;

    // Relatable Questionnaire Preferences
    private String interests; // Comma-separated: "Badminton,Study,Travel,Photography"
    private String socialVibe; // e.g., "Active & Outdoorsy", "Chill & Casual", "Laser-Focused"
    private String budgetPreference; // "BUDGET_FRIENDLY", "MODERATE", "FLEXIBLE"
    private String typicalAvailability; // "WEEKENDS", "WEEKDAY_EVENINGS", "ANYTIME"
    private String safetyPactAgreed; // "YES"

    // Badges (comma-separated, e.g. "GPS Verified,Top Punctual,Founding Member")
    private String badges;

    // AI Safety Monitoring
    private boolean isFlagged;
    private String flagReason;
    private String role; // "ROLE_USER", "ROLE_ADMIN"

    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.trustScore = 50;
        this.trustTier = "BRONZE";
        this.peerRatingAvg = 5.0;
        this.ratingsCount = 0;
        this.activitiesAttended = 0;
        this.activitiesHosted = 0;
        this.isFlagged = false;
        this.role = "ROLE_USER";
        this.verificationStatus = "PENDING";
    }

    @PrePersist
    public void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.lastActiveAt == null) {
            this.lastActiveAt = LocalDateTime.now();
        }
        if (this.trustScore == null) {
            this.trustScore = 50;
        }
        if (this.trustTier == null) {
            this.trustTier = "BRONZE";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getClaimedCity() { return claimedCity; }
    public void setClaimedCity(String claimedCity) { this.claimedCity = claimedCity; }

    public Double getVerifiedLatitude() { return verifiedLatitude; }
    public void setVerifiedLatitude(Double verifiedLatitude) { this.verifiedLatitude = verifiedLatitude; }

    public Double getVerifiedLongitude() { return verifiedLongitude; }
    public void setVerifiedLongitude(Double verifiedLongitude) { this.verifiedLongitude = verifiedLongitude; }

    public Double getLocationAccuracyMeters() { return locationAccuracyMeters; }
    public void setLocationAccuracyMeters(Double locationAccuracyMeters) { this.locationAccuracyMeters = locationAccuracyMeters; }

    public boolean isLocationVerified() { return isLocationVerified; }
    public void setLocationVerified(boolean locationVerified) { isLocationVerified = locationVerified; }

    public String getLocationVerificationNotes() { return locationVerificationNotes; }
    public void setLocationVerificationNotes(String notes) { this.locationVerificationNotes = notes; }

    public boolean isIdentityVerified() { return isIdentityVerified; }
    public void setIdentityVerified(boolean identityVerified) { isIdentityVerified = identityVerified; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public Integer getTrustScore() { return trustScore; }
    public void setTrustScore(Integer trustScore) { this.trustScore = trustScore; }

    public String getTrustTier() { return trustTier; }
    public void setTrustTier(String trustTier) { this.trustTier = trustTier; }

    public int getActivitiesAttended() { return activitiesAttended; }
    public void setActivitiesAttended(int activitiesAttended) { this.activitiesAttended = activitiesAttended; }

    public int getActivitiesHosted() { return activitiesHosted; }
    public void setActivitiesHosted(int activitiesHosted) { this.activitiesHosted = activitiesHosted; }

    public Double getPeerRatingAvg() { return peerRatingAvg; }
    public void setPeerRatingAvg(Double peerRatingAvg) { this.peerRatingAvg = peerRatingAvg; }

    public int getRatingsCount() { return ratingsCount; }
    public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public String getSocialVibe() { return socialVibe; }
    public void setSocialVibe(String socialVibe) { this.socialVibe = socialVibe; }

    public String getBudgetPreference() { return budgetPreference; }
    public void setBudgetPreference(String budgetPreference) { this.budgetPreference = budgetPreference; }

    public String getTypicalAvailability() { return typicalAvailability; }
    public void setTypicalAvailability(String typicalAvailability) { this.typicalAvailability = typicalAvailability; }

    public String getSafetyPactAgreed() { return safetyPactAgreed; }
    public void setSafetyPactAgreed(String safetyPactAgreed) { this.safetyPactAgreed = safetyPactAgreed; }

    public String getBadges() { return badges; }
    public void setBadges(String badges) { this.badges = badges; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}
