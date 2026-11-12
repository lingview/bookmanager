package xyz.lingview.astra.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.lingview.astra.service.UserAdminService;

@Component
public class AdminNavInterceptor implements HandlerInterceptor {

    @Autowired
    private UserAdminService userAdminService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object username = session.getAttribute("username");
            if (username != null) {
                Object cached = session.getAttribute("isAdmin");
                if (!(cached instanceof Boolean)) {
                    cached = userAdminService.isAdmin((String) username);
                    session.setAttribute("isAdmin", cached);
                }
                request.setAttribute("isAdmin", cached);
            }
        }
        return true;
    }
}
