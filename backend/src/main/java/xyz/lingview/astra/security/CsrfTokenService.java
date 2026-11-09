package xyz.lingview.astra.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Component
public class CsrfTokenService {

    private static final String SESSION_ATTR = "csrfToken";

    public String getToken(HttpSession session) {
        Object existing = session.getAttribute(SESSION_ATTR);
        if (existing instanceof String token && !token.isEmpty()) {
            return token;
        }
        String token = UUID.randomUUID().toString();
        session.setAttribute(SESSION_ATTR, token);
        return token;
    }

    public boolean isValid(HttpSession session, String submitted) {
        if (session == null || submitted == null) {
            return false;
        }
        Object stored = session.getAttribute(SESSION_ATTR);
        if (!(stored instanceof String expected)) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8));
    }
}