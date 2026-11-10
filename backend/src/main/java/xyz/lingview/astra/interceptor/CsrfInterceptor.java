package xyz.lingview.astra.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.lingview.astra.security.CsrfTokenService;

@Component
public class CsrfInterceptor implements HandlerInterceptor {

    private static final String TOKEN_HEADER = "X-CSRF-Token";

    @Autowired
    private CsrfTokenService csrfTokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader(TOKEN_HEADER);
        if (token == null || token.isEmpty()) {
            token = request.getParameter("csrfToken");
        }
        if (!csrfTokenService.isValid(request.getSession(false), token)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}