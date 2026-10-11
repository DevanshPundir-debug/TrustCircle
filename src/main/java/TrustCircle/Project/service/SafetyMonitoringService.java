package TrustCircle.Project.service;

import TrustCircle.Project.model.Activity;
import TrustCircle.Project.model.SafetyAlert;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.SafetyAlertRepository;
import TrustCircle.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SafetyMonitoringService {

    @Autowired
    private SafetyAlertRepository safetyAlertRepository;

    @Autowired
    private UserRepository userRepository;

    private static final List<String> SUSPICIOUS_KEYWORDS = Arrays.asList(
            "telegram", "whatsapp", "crypto", "investment", "cash transfer",
            "wire money", "escort", "drug", "hack", "pyramid", "venmo me first",
            "paypal me first", "bank transfer", "free money"
    );

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(?!trustcircle\\.)[\\w\\.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=.]+", Pattern.CASE_INSENSITIVE);

    /**
     * Inspects activity title and description for scam/phishing patterns.
     */
    public boolean checkActivitySafety(Activity activity, User creator) {
        String combinedText = ((activity.getTitle() != null ? activity.getTitle() : "") + " " +
                (activity.getDescription() != null ? activity.getDescription() : "")).toLowerCase();

        for (String keyword : SUSPICIOUS_KEYWORDS) {
            if (combinedText.contains(keyword)) {
                createAlert(creator, activity, "SUSPICIOUS_CONTENT", "HIGH",
                        "Activity contains flagged suspicious keyword: '" + keyword + "'");
                return false;
            }
        }

        if (URL_PATTERN.matcher(combinedText).find()) {
            createAlert(creator, activity, "SUSPICIOUS_CONTENT", "MEDIUM",
                    "Activity contains external hyperlink. Flagged for anti-phishing review.");
        }

        return true;
    }

    /**
     * Inspects GPS verification data for spoofing or inconsistencies.
     */
    public void inspectGeoSafety(User user, String claimedCity, Double lat, Double lon, Double accuracy) {
        if (accuracy != null && accuracy > 10000.0) {
            createAlert(user, null, "GPS_SPOOFING", "MEDIUM",
                    "Unusually high GPS inaccuracy radius (" + Math.round(accuracy) + "m). Possible VPN or network proxy emulation.");
        }

        if (lat == null || lon == null || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            createAlert(user, null, "GPS_SPOOFING", "HIGH",
                    "Invalid GPS coordinates provided: (" + lat + ", " + lon + ").");
        }
    }

    public SafetyAlert createAlert(User user, Activity activity, String alertType, String riskLevel, String details) {
        Activity persistentActivity = (activity != null && activity.getId() != null) ? activity : null;
        SafetyAlert alert = new SafetyAlert(user, persistentActivity, alertType, riskLevel, details);
        SafetyAlert saved = safetyAlertRepository.save(alert);

        if ("HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel)) {
            user.setFlagged(true);
            user.setFlagReason(details);
            userRepository.save(user);
        }

        return saved;
    }

    public List<SafetyAlert> getAllAlerts() {
        return safetyAlertRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SafetyAlert> getUnresolvedAlerts() {
        return safetyAlertRepository.findByIsResolvedFalseOrderByCreatedAtDesc();
    }

    public SafetyAlert resolveAlert(Long alertId, String notes) {
        SafetyAlert alert = safetyAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));
        alert.setResolved(true);
        alert.setResolutionNotes(notes);

        // Check if user has other unresolved critical alerts
        User user = alert.getUser();
        List<SafetyAlert> userAlerts = safetyAlertRepository.findByUser(user);
        boolean hasUnresolvedHighRisk = userAlerts.stream()
                .anyMatch(a -> !a.isResolved() && ("HIGH".equalsIgnoreCase(a.getRiskLevel()) || "CRITICAL".equalsIgnoreCase(a.getRiskLevel())));

        if (!hasUnresolvedHighRisk) {
            user.setFlagged(false);
            user.setFlagReason(null);
            userRepository.save(user);
        }

        return safetyAlertRepository.save(alert);
    }
}
