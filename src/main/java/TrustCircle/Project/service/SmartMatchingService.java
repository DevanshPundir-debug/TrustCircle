package TrustCircle.Project.service;

import TrustCircle.Project.dto.SmartMatchDto;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SmartMatchingService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Finds smart matching companions for the given user.
     */
    public List<SmartMatchDto> findMatches(User currentUser) {
        List<User> allUsers = userRepository.findAll();
        List<SmartMatchDto> matches = new ArrayList<>();

        Set<String> myInterests = parseTags(currentUser.getInterests());

        for (User other : allUsers) {
            // Exclude self and suspended/flagged users
            if (other.getId().equals(currentUser.getId()) || other.isFlagged()) {
                continue;
            }

            Set<String> otherInterests = parseTags(other.getInterests());
            List<String> sharedInterests = new ArrayList<>();
            for (String interest : myInterests) {
                if (otherInterests.contains(interest.toLowerCase())) {
                    sharedInterests.add(interest);
                }
            }

            // 1. Interest Score (0 - 35)
            double interestScore = 0;
            if (!myInterests.isEmpty() && !otherInterests.isEmpty()) {
                Set<String> union = new HashSet<>(myInterests);
                union.addAll(otherInterests);
                double jaccard = (double) sharedInterests.size() / union.size();
                interestScore = jaccard * 35.0;
            }

            // 2. Geo Proximity Score (0 - 30)
            Double distanceKm = null;
            double proximityScore = 15; // default fallback if either coords missing
            if (currentUser.getVerifiedLatitude() != null && currentUser.getVerifiedLongitude() != null &&
                other.getVerifiedLatitude() != null && other.getVerifiedLongitude() != null) {
                distanceKm = calculateDistanceKm(
                        currentUser.getVerifiedLatitude(), currentUser.getVerifiedLongitude(),
                        other.getVerifiedLatitude(), other.getVerifiedLongitude()
                );
                distanceKm = Math.round(distanceKm * 10.0) / 10.0;

                if (distanceKm <= 5.0) {
                    proximityScore = 30.0;
                } else if (distanceKm <= 15.0) {
                    proximityScore = 24.0;
                } else if (distanceKm <= 35.0) {
                    proximityScore = 18.0;
                } else if (distanceKm <= 75.0) {
                    proximityScore = 12.0;
                } else {
                    proximityScore = 5.0;
                }
            } else if (currentUser.getClaimedCity() != null &&
                       currentUser.getClaimedCity().equalsIgnoreCase(other.getClaimedCity())) {
                proximityScore = 22.0;
                distanceKm = 4.5;
            }

            // 3. Availability Compatibility (0 - 15)
            double availabilityScore = 5.0;
            if (currentUser.getTypicalAvailability() != null && other.getTypicalAvailability() != null) {
                if (currentUser.getTypicalAvailability().equalsIgnoreCase(other.getTypicalAvailability()) ||
                    "FLEXIBLE".equalsIgnoreCase(currentUser.getTypicalAvailability()) ||
                    "FLEXIBLE".equalsIgnoreCase(other.getTypicalAvailability())) {
                    availabilityScore = 15.0;
                } else {
                    availabilityScore = 8.0;
                }
            }

            // 4. Budget Compatibility (0 - 10)
            double budgetScore = 5.0;
            if (currentUser.getBudgetPreference() != null && other.getBudgetPreference() != null) {
                if (currentUser.getBudgetPreference().equalsIgnoreCase(other.getBudgetPreference())) {
                    budgetScore = 10.0;
                } else {
                    budgetScore = 7.0;
                }
            }

            // 5. Trust Score Bonus (0 - 10)
            double trustBonus = 0.0;
            int otherTrust = other.getTrustScore() != null ? other.getTrustScore() : 50;
            if (otherTrust >= 85) {
                trustBonus = 10.0;
            } else if (otherTrust >= 70) {
                trustBonus = 7.0;
            } else {
                trustBonus = 4.0;
            }

            int matchPercentage = (int) Math.round(interestScore + proximityScore + availabilityScore + budgetScore + trustBonus);
            matchPercentage = Math.max(35, Math.min(99, matchPercentage));

            // Generate AI explanation
            String explanation = buildAiExplanation(currentUser, other, matchPercentage, distanceKm, sharedInterests);

            matches.add(new SmartMatchDto(
                    other,
                    matchPercentage,
                    distanceKm,
                    sharedInterests,
                    explanation,
                    other.getTrustTier() != null ? other.getTrustTier() : "BRONZE"
            ));
        }

        // Sort descending by match percentage
        matches.sort((a, b) -> Integer.compare(b.getMatchPercentage(), a.getMatchPercentage()));
        return matches;
    }

    private String buildAiExplanation(User me, User other, int matchPct, Double distKm, List<String> shared) {
        StringBuilder sb = new StringBuilder();
        sb.append(matchPct).append("% Match: ");

        if (!shared.isEmpty()) {
            sb.append("Both share passion for ").append(String.join(" & ", shared)).append(". ");
        }

        if (distKm != null) {
            sb.append("Located within ~").append(distKm).append(" km in ").append(other.getClaimedCity() != null ? other.getClaimedCity() : "your area").append(". ");
        } else if (other.getClaimedCity() != null) {
            sb.append("Based in ").append(other.getClaimedCity()).append(". ");
        }

        if (other.getTrustScore() != null && other.getTrustScore() >= 75) {
            sb.append("High ").append(other.getTrustTier()).append(" Trust Score (").append(other.getTrustScore()).append("/100) ensures a verified, reliable meetup.");
        } else {
            sb.append("Active verified member with compatible schedule and budget.");
        }

        return sb.toString();
    }

    private Set<String> parseTags(String str) {
        if (str == null || str.isBlank()) return Collections.emptySet();
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Haversine formula to compute great-circle distance between two GPS coordinates in kilometers.
     */
    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
