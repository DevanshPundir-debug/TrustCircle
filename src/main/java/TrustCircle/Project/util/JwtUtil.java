package TrustCircle.Project.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class JwtUtil {

    private static final String SECRET = "TrustCircleSecureJWTKey2026PlatformVerificationAISecretKey987654321";
    private static final long EXPIRATION_MS = 86400000L * 7L; // 7 days

    public static String generateToken(Long userId, String email) {
        long exp = System.currentTimeMillis() + EXPIRATION_MS;
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payloadJson = String.format("{\"id\":%d,\"sub\":\"%s\",\"exp\":%d}", userId, email, exp);
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = sign(header + "." + payload, SECRET);
        return header + "." + payload + "." + signature;
    }

    public static String extractEmail(String token) {
        if (token == null || !token.contains(".")) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            if (!verifySignature(parts[0] + "." + parts[1], parts[2], SECRET)) {
                return null;
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            int subIdx = payloadJson.indexOf("\"sub\":\"");
            if (subIdx == -1) return null;
            int start = subIdx + 7;
            int end = payloadJson.indexOf("\"", start);
            return payloadJson.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long extractUserId(String token) {
        if (token == null || !token.contains(".")) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            if (!verifySignature(parts[0] + "." + parts[1], parts[2], SECRET)) {
                return null;
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            int idIdx = payloadJson.indexOf("\"id\":");
            if (idIdx == -1) return null;
            int start = idIdx + 5;
            int end = payloadJson.indexOf(",", start);
            if (end == -1) end = payloadJson.indexOf("}", start);
            return Long.parseLong(payloadJson.substring(start, end).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static String sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error signing JWT", e);
        }
    }

    private static boolean verifySignature(String data, String signature, String secret) {
        String expectedSig = sign(data, secret);
        return expectedSig.equals(signature);
    }
}
