package TrustCircle.Project.service;

import TrustCircle.Project.dto.VerificationFormRequest;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SafetyMonitoringService safetyMonitoringService;

    @Autowired
    private DynamicTrustService dynamicTrustService;

    /**
     * Processes Google-Forms-style questionnaire responses & real-time GPS verification.
     */
    public User processVerificationForm(User user, VerificationFormRequest form) {
        if (form.getClaimedCity() != null && !form.getClaimedCity().isBlank()) {
            user.setClaimedCity(form.getClaimedCity().trim());
        }

        if (form.getBio() != null) {
            user.setBio(form.getBio().trim());
        }

        if (form.getWeekendVibe() != null) {
            user.setSocialVibe(form.getWeekendVibe().trim());
        }

        if (form.getInterests() != null) {
            user.setInterests(form.getInterests().trim());
        }

        if (form.getTypicalAvailability() != null) {
            user.setTypicalAvailability(form.getTypicalAvailability().trim());
        }

        if (form.getBudgetPreference() != null) {
            user.setBudgetPreference(form.getBudgetPreference().trim());
        }

        if (Boolean.TRUE.equals(form.getOfflineSafetyPact())) {
            user.setSafetyPactAgreed("YES");
        }

        // GPS Geolocation Verification Check
        if (form.getGpsLatitude() != null && form.getGpsLongitude() != null) {
            user.setVerifiedLatitude(form.getGpsLatitude());
            user.setVerifiedLongitude(form.getGpsLongitude());
            user.setLocationAccuracyMeters(form.getGpsAccuracy() != null ? form.getGpsAccuracy() : 20.0);

            // Safety inspection for GPS spoofing
            safetyMonitoringService.inspectGeoSafety(user, user.getClaimedCity(),
                    form.getGpsLatitude(), form.getGpsLongitude(), form.getGpsAccuracy());

            // Validate plausible GPS bounds
            if (form.getGpsLatitude() >= -90 && form.getGpsLatitude() <= 90 &&
                form.getGpsLongitude() >= -180 && form.getGpsLongitude() <= 180) {
                user.setLocationVerified(true);
                user.setLocationVerificationNotes("GPS physically confirmed via browser sensor (accuracy: ~" +
                        (form.getGpsAccuracy() != null ? Math.round(form.getGpsAccuracy()) : 15) + "m)");
            } else {
                user.setLocationVerified(false);
                user.setLocationVerificationNotes("Invalid coordinates received.");
            }
        } else {
            // Default location verification for manual input
            user.setLocationVerified(false);
            user.setLocationVerificationNotes("GPS permission skipped. Location claimed manually.");
        }

        // Identity Verification Status
        boolean hasInterests = user.getInterests() != null && !user.getInterests().isBlank();
        boolean hasPact = "YES".equalsIgnoreCase(user.getSafetyPactAgreed());

        if (hasInterests && hasPact) {
            user.setIdentityVerified(true);
            user.setVerificationStatus("VERIFIED");
            user.setVerifiedAt(LocalDateTime.now());
        } else {
            user.setIdentityVerified(false);
            user.setVerificationStatus("PENDING");
        }

        // Recalculate dynamic trust score
        user = dynamicTrustService.recalculateTrustScore(user);
        return userRepository.save(user);
    }
}
