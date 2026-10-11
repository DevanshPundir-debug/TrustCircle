package TrustCircle.Project.service;

import TrustCircle.Project.dto.AuthRequest;
import TrustCircle.Project.dto.AuthResponse;
import TrustCircle.Project.dto.VerificationFormRequest;
import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.UserRepository;
import TrustCircle.Project.util.JwtUtil;
import TrustCircle.Project.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private DynamicTrustService dynamicTrustService;

    public AuthResponse register(AuthRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }
        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new RuntimeException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPassword(PasswordUtil.hashPassword(req.getPassword()));
        user.setFullName(req.getFullName() != null ? req.getFullName().trim() : "Verified Member");
        user.setPhone(req.getPhone());
        user.setTrustScore(50);
        user.setTrustTier("BRONZE");

        User saved = userRepository.save(user);
        String token = JwtUtil.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token, saved, "Account created successfully. Please complete identity verification.");
    }

    public AuthResponse login(AuthRequest req) {
        if (req.getEmail() == null || req.getPassword() == null) {
            throw new RuntimeException("Email and password are required");
        }
        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!PasswordUtil.checkPassword(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user, "Login successful");
    }

    public AuthResponse submitVerificationForm(User currentUser, VerificationFormRequest form) {
        if (currentUser == null) {
            throw new RuntimeException("Please sign in before completing identity verification.");
        }

        User verifiedUser = verificationService.processVerificationForm(currentUser, form);
        String token = JwtUtil.generateToken(verifiedUser.getId(), verifiedUser.getEmail());
        return new AuthResponse(token, verifiedUser, "Identity & Location Verification completed successfully! Your Trust Score is now " + verifiedUser.getTrustScore());
    }
}
