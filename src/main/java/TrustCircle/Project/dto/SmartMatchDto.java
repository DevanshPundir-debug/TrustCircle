package TrustCircle.Project.dto;

import TrustCircle.Project.model.User;
import java.util.List;

public class SmartMatchDto {
    private User user;
    private int matchPercentage;
    private Double distanceKm;
    private List<String> sharedInterests;
    private String aiExplanation;
    private String trustStatus;

    public SmartMatchDto() {}

    public SmartMatchDto(User user, int matchPercentage, Double distanceKm, List<String> sharedInterests, String aiExplanation, String trustStatus) {
        this.user = user;
        this.matchPercentage = matchPercentage;
        this.distanceKm = distanceKm;
        this.sharedInterests = sharedInterests;
        this.aiExplanation = aiExplanation;
        this.trustStatus = trustStatus;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public List<String> getSharedInterests() { return sharedInterests; }
    public void setSharedInterests(List<String> sharedInterests) { this.sharedInterests = sharedInterests; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    public String getTrustStatus() { return trustStatus; }
    public void setTrustStatus(String trustStatus) { this.trustStatus = trustStatus; }
}
