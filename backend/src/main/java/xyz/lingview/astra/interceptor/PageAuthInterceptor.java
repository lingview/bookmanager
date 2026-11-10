package xyz.lingview.astra.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.lingview.astra.security.AuthType;
import xyz.lingview.astra.security.AuthenticatedUser;
import xyz.lingview.astra.security.UserContextHolder;
import xyz.lingview.astra.util.HandlerUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class PageAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            Object username = session.getAttribute("username");
            if (username != null) {
                UserContextHolder.set(new AuthenticatedUser((String) username, null, AuthType.SESSION));
                return true;
            }
        }

        if (!HandlerUtil.isViewHandler(handler)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String target = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            target = target + "?" + query;
        }

        log.info("页面未登录或会话已过期: {}", target);
        response.sendRedirect(request.getContextPath() + "/login?redirect="
                + URLEncoder.encode(target, StandardCharsets.UTF_8));
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}