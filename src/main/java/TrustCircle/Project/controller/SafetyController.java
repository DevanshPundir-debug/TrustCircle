package TrustCircle.Project.controller;

import TrustCircle.Project.model.Activity;
import TrustCircle.Project.model.SafetyAlert;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.ActivityRepository;
import TrustCircle.Project.repository.UserRepository;
import TrustCircle.Project.service.DynamicTrustService;
import TrustCircle.Project.service.SafetyMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/safety")
public class SafetyController {

    @Autowired
    private SafetyMonitoringService safetyMonitoringService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DynamicTrustService dynamicTrustService;

    @GetMapping("/alerts")
    public ResponseEntity<List<SafetyAlert>> getAlerts() {
        return ResponseEntity.ok(safetyMonitoringService.getAllAlerts());
    }

    @PostMapping("/alerts/{id}/resolve")
    public ResponseEntity<?> resolveAlert(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String notes = (body != null && body.containsKey("notes")) ? body.get("notes") : "Reviewed and resolved by community safety moderator.";
        try {
            SafetyAlert resolved = safetyMonitoringService.resolveAlert(id, notes);
            return ResponseEntity.ok(resolved);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/report")
    public ResponseEntity<?> reportSuspiciousEntity(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User reporter = (User) request.getAttribute("currentUser");
        Long targetUserId = body.get("targetUserId") != null ? Long.valueOf(body.get("targetUserId").toString()) : null;
        Long activityId = body.get("activityId") != null ? Long.valueOf(body.get("activityId").toString()) : null;
        String reason = body.get("reason") != null ? body.get("reason").toString() : "Community peer report filed.";

        if (targetUserId == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "targetUserId is required");
            return ResponseEntity.badRequest().body(err);
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        Activity activity = activityId != null ? activityRepository.findById(activityId).orElse(null) : null;

        SafetyAlert alert = safetyMonitoringService.createAlert(
                targetUser,
                activity,
                "PEER_REPORT",
                "HIGH",
                "Reported by member (ID: " + (reporter != null ? reporter.getId() : "Anon") + "): " + reason
        );

        // Recalculate trust score to apply deduction
        dynamicTrustService.recalculateTrustScore(targetUser);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Report submitted. Safety monitoring layer has flagged this account for immediate review.");
        resp.put("alertId", alert.getId());
        return ResponseEntity.ok(resp);
    }
}
