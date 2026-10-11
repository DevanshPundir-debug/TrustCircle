package TrustCircle.Project.config;

import TrustCircle.Project.model.User;
import TrustCircle.Project.repository.UserRepository;
import TrustCircle.Project.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow CORS pre-flight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            authHeader = request.getHeader("X-Auth-Token");
            if (authHeader != null) {
                authHeader = "Bearer " + authHeader;
            }
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            String email = JwtUtil.extractEmail(token);
            if (email != null) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                userOpt.ifPresent(user -> request.setAttribute("currentUser", user));
            }
        }

        return true;
    }
}
