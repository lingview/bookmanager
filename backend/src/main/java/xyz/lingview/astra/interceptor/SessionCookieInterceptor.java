package xyz.lingview.astra.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class SessionCookieInterceptor implements HandlerInterceptor {

    private final CookieSerializer cookieSerializer;
    private final ObjectProvider<SessionRepository<?>> sessionRepositoryProvider;

    public SessionCookieInterceptor(CookieSerializer cookieSerializer,
                                    ObjectProvider<SessionRepository<?>> sessionRepositoryProvider) {
        this.cookieSerializer = cookieSerializer;
        this.sessionRepositoryProvider = sessionRepositoryProvider;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        if (sessionRepositoryProvider.getIfAvailable() == null) {
            return;
        }
        if (modelAndView == null || modelAndView.getViewName() == null
                || modelAndView.getViewName().startsWith("redirect:")) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null || session.getId().equals(request.getRequestedSessionId())) {
            return;
        }
        cookieSerializer.writeCookieValue(
                new CookieSerializer.CookieValue(request, response, session.getId()));
    }
}