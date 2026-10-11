package TrustCircle.Project.controller;

import TrustCircle.Project.dto.AuthRequest;
import TrustCircle.Project.dto.AuthResponse;
import TrustCircle.Project.dto.VerificationFormRequest;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.UserRepository;
import TrustCircle.Project.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        try {
            return ResponseEntity.ok(authService.register(req));
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            return ResponseEntity.ok(authService.login(req));
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(err);
        }
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Google-Forms style verification onboarding endpoint.
     */
    @PostMapping("/verify-form")
    public ResponseEntity<?> submitVerificationForm(@RequestBody VerificationFormRequest form, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            AuthResponse response = authService.submitVerificationForm(currentUser, form);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}
