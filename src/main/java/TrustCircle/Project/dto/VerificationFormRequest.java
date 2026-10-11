package TrustCircle.Project.dto;

public class VerificationFormRequest {
    private String bio;

    // Location Verification fields
    private String claimedCity;
    private Double gpsLatitude;
    private Double gpsLongitude;
    private Double gpsAccuracy;
    private Boolean locationPermissionGranted;

    // Relatable questionnaire responses
    private String weekendVibe;
    private String interests;
    private String typicalAvailability;
    private String budgetPreference;
    private Boolean offlineSafetyPact;

    public VerificationFormRequest() {}

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getClaimedCity() { return claimedCity; }
    public void setClaimedCity(String claimedCity) { this.claimedCity = claimedCity; }

    public Double getGpsLatitude() { return gpsLatitude; }
    public void setGpsLatitude(Double gpsLatitude) { this.gpsLatitude = gpsLatitude; }

    public Double getGpsLongitude() { return gpsLongitude; }
    public void setGpsLongitude(Double gpsLongitude) { this.gpsLongitude = gpsLongitude; }

    public Double getGpsAccuracy() { return gpsAccuracy; }
    public void setGpsAccuracy(Double gpsAccuracy) { this.gpsAccuracy = gpsAccuracy; }

    public Boolean getLocationPermissionGranted() { return locationPermissionGranted; }
    public void setLocationPermissionGranted(Boolean locationPermissionGranted) { this.locationPermissionGranted = locationPermissionGranted; }

    public String getWeekendVibe() { return weekendVibe; }
    public void setWeekendVibe(String weekendVibe) { this.weekendVibe = weekendVibe; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public String getTypicalAvailability() { return typicalAvailability; }
    public void setTypicalAvailability(String typicalAvailability) { this.typicalAvailability = typicalAvailability; }

    public String getBudgetPreference() { return budgetPreference; }
    public void setBudgetPreference(String budgetPreference) { this.budgetPreference = budgetPreference; }

    public Boolean getOfflineSafetyPact() { return offlineSafetyPact; }
    public void setOfflineSafetyPact(Boolean offlineSafetyPact) { this.offlineSafetyPact = offlineSafetyPact; }
}
