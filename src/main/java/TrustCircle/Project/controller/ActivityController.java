package TrustCircle.Project.controller;

import TrustCircle.Project.dto.ActivityRequest;
import TrustCircle.Project.model.Activity;
import TrustCircle.Project.model.ActivityParticipant;
import TrustCircle.Project.model.User;
import TrustCircle.Project.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping
    public ResponseEntity<List<Activity>> getActivities(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(activityService.getActivities(category, status));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMySchedule(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Authentication required");
            return ResponseEntity.status(401).body(err);
        }
        return ResponseEntity.ok(activityService.getMySchedule(currentUser));
    }

    @PostMapping("/requests/{participantId}/{decision}")
    public ResponseEntity<?> respondToJoinRequest(@PathVariable Long participantId,
                                                  @PathVariable String decision,
                                                  HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Authentication required");
            return ResponseEntity.status(401).body(err);
        }
        if (!"approve".equals(decision) && !"reject".equals(decision)) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Decision must be either 'approve' or 'reject'");
            return ResponseEntity.badRequest().body(err);
        }
        try {
            return ResponseEntity.ok(
                    activityService.respondToJoinRequest(currentUser, participantId, "approve".equals(decision)));
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(activityService.getActivityById(id));
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(404).body(err);
        }
    }

    @PostMapping
    public ResponseEntity<?> createActivity(@RequestBody ActivityRequest req, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Authentication required to create an activity");
            return ResponseEntity.status(401).body(err);
        }
        if (currentUser.isFlagged()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Account is currently under safety review. Activity creation restricted.");
            return ResponseEntity.status(403).body(err);
        }
        try {
            Activity created = activityService.createActivity(currentUser, req);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinActivity(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Authentication required to join an activity");
            return ResponseEntity.status(401).body(err);
        }
        try {
            ActivityParticipant participant = activityService.joinActivity(currentUser, id);
            return ResponseEntity.ok(participant);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ActivityParticipant>> getParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getParticipants(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeActivity(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Authentication required");
            return ResponseEntity.status(401).body(err);
        }
        try {
            Activity activity = activityService.completeActivity(id, currentUser);
            return ResponseEntity.ok(activity);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}
