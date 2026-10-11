package TrustCircle.Project.service;

import TrustCircle.Project.dto.ActivityRequest;
import TrustCircle.Project.model.Activity;
import TrustCircle.Project.model.ActivityParticipant;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.ActivityParticipantRepository;
import TrustCircle.Project.repository.ActivityRepository;
import TrustCircle.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SafetyMonitoringService safetyMonitoringService;

    @Autowired
    private DynamicTrustService dynamicTrustService;

    public Activity createActivity(User creator, ActivityRequest req) {
        Activity activity = new Activity();
        activity.setCreator(creator);
        activity.setTitle(req.getTitle());
        activity.setDescription(req.getDescription());
        activity.setCategory(req.getCategory() != null ? req.getCategory().toUpperCase() : "SOCIAL");
        activity.setVenue(req.getVenue());
        activity.setCity(req.getCity() != null ? req.getCity() : creator.getClaimedCity());
        activity.setLatitude(req.getLatitude() != null ? req.getLatitude() : creator.getVerifiedLatitude());
        activity.setLongitude(req.getLongitude() != null ? req.getLongitude() : creator.getVerifiedLongitude());
        activity.setActivityDate(req.getActivityDate() != null ? req.getActivityDate() : LocalDateTime.now().plusDays(2));
        activity.setBudget(req.getBudget() != null ? req.getBudget() : "FREE");
        activity.setMinTrustScore(req.getMinTrustScore() != null ? req.getMinTrustScore() : 50);
        activity.setMaxParticipants(req.getMaxParticipants() != null ? req.getMaxParticipants() : 6);
        activity.setStatus("OPEN");

        // Run AI Safety Monitoring check
        boolean isSafe = safetyMonitoringService.checkActivitySafety(activity, creator);
        if (!isSafe) {
            throw new RuntimeException("AI Safety Sentinel Intercept: Activity text contains prohibited keywords or suspicious solicitations. The action has been blocked and flagged for safety moderation.");
        }

        Activity saved = activityRepository.save(activity);

        // Creator automatically joins as host
        participantRepository.save(new ActivityParticipant(saved, creator, "HOST"));

        creator.setActivitiesHosted(creator.getActivitiesHosted() + 1);
        userRepository.save(creator);
        dynamicTrustService.recalculateTrustScore(creator);

        return saved;
    }

    public List<Activity> getActivities(String category, String status) {
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            return activityRepository.findByCategoryAndStatusOrderByActivityDateAsc(category.toUpperCase(), status != null ? status : "OPEN");
        }
        if (status != null && !status.isBlank()) {
            return activityRepository.findByStatusOrderByActivityDateAsc(status);
        }
        return activityRepository.findAllByOrderByCreatedAtDesc();
    }

    public Activity getActivityById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));
    }

    public ActivityParticipant joinActivity(User user, Long activityId) {
        Activity activity = getActivityById(activityId);

        if (activity.getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("You are the host of this activity.");
        }

        // Check if already participant
        if (participantRepository.existsByActivityAndUser(activity, user)) {
            throw new RuntimeException("You have already requested to join or joined this activity.");
        }

        // Trust Score verification check!
        int userTrust = user.getTrustScore() != null ? user.getTrustScore() : 50;
        int requiredTrust = activity.getMinTrustScore() != null ? activity.getMinTrustScore() : 50;

        if (userTrust < requiredTrust) {
            throw new RuntimeException("Trust Score Requirement Not Met: This activity requires a minimum Trust Score of "
                    + requiredTrust + ". Your current score is " + userTrust + ". Complete identity verification or build your reputation to qualify.");
        }

        assertHasCapacity(activity);

        // Host must approve before the seat is confirmed.
        return participantRepository.save(new ActivityParticipant(activity, user, "PENDING"));
    }

    /**
     * Host decides on a pending join request. Approving confirms the seat and
     * credits the requester's participation history.
     */
    public ActivityParticipant respondToJoinRequest(User host, Long participantId, boolean approve) {
        ActivityParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Join request not found."));

        if (!participant.getActivity().getCreator().getId().equals(host.getId())) {
            throw new RuntimeException("Only the host of this activity can respond to join requests.");
        }
        if (!"PENDING".equals(participant.getStatus())) {
            throw new RuntimeException("This request has already been handled.");
        }

        if (!approve) {
            participant.setStatus("REJECTED");
            return participantRepository.save(participant);
        }

        assertHasCapacity(participant.getActivity());
        participant.setStatus("JOINED");
        ActivityParticipant saved = participantRepository.save(participant);

        User requester = participant.getUser();
        requester.setActivitiesAttended(requester.getActivitiesAttended() + 1);
        userRepository.save(requester);
        dynamicTrustService.recalculateTrustScore(requester);

        return saved;
    }

    /**
     * Everything the signed-in member needs for their schedule: activities they
     * host, activities they are on the roster for, and requests awaiting their
     * decision as a host.
     */
    public Map<String, Object> getMySchedule(User user) {
        List<Activity> hosted = activityRepository.findByCreator(user);

        List<ActivityParticipant> myParticipations =
                participantRepository.findByUserAndStatusIn(user, List.of("JOINED", "PENDING"));

        List<ActivityParticipant> incomingRequests = hosted.isEmpty()
                ? List.of()
                : participantRepository.findByActivityInAndStatus(hosted, "PENDING");

        Map<String, Object> schedule = new HashMap<>();
        schedule.put("hosted", hosted);
        schedule.put("participating", myParticipations);
        schedule.put("requests", incomingRequests);
        return schedule;
    }

    private void assertHasCapacity(Activity activity) {
        if (activity.getMaxParticipants() == null) return;
        long confirmed = participantRepository.countByActivityAndStatus(activity, "JOINED") + 1; // + host
        if (confirmed >= activity.getMaxParticipants()) {
            throw new RuntimeException("This activity has reached its maximum participant limit.");
        }
    }

    public List<ActivityParticipant> getParticipants(Long activityId) {
        Activity activity = getActivityById(activityId);
        return participantRepository.findByActivity(activity);
    }

    public Activity completeActivity(Long activityId, User host) {
        Activity activity = getActivityById(activityId);
        if (!activity.getCreator().getId().equals(host.getId())) {
            throw new RuntimeException("Only the host can mark an activity as completed.");
        }
        activity.setStatus("COMPLETED");
        return activityRepository.save(activity);
    }
}
