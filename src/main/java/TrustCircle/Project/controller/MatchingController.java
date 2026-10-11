package TrustCircle.Project.controller;

import TrustCircle.Project.dto.SmartMatchDto;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.UserRepository;
import TrustCircle.Project.service.SmartMatchingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    @Autowired
    private SmartMatchingService matchingService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/companions")
    public ResponseEntity<?> getSmartMatches(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            // If visitor is exploring without login, pick the first active verified user as demo reference
            List<User> verified = userRepository.findByIsIdentityVerifiedTrue();
            if (!verified.isEmpty()) {
                currentUser = verified.get(0);
            } else {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Please complete verification to unlock AI Smart Matching");
                return ResponseEntity.status(401).body(err);
            }
        }

        List<SmartMatchDto> matches = matchingService.findMatches(currentUser);
        return ResponseEntity.ok(matches);
    }
}
