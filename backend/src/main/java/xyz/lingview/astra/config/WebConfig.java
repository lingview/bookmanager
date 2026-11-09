package xyz.lingview.astra.config;

import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.lingview.astra.interceptor.PageAuthInterceptor;
import xyz.lingview.astra.interceptor.SessionCookieInterceptor;
import xyz.lingview.astra.interceptor.UserAgentSessionFilter;
import xyz.lingview.astra.interceptor.UserPermissionInterceptor;
import xyz.lingview.astra.security.SessionAuthFilter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserAgentSessionFilter userAgentSessionFilter;

    @Bean
    public SessionAuthFilter sessionAuthFilter() {
        return new SessionAuthFilter();
    }

    // 注册 SessionAuthFilter
    @Bean
    public FilterRegistrationBean<SessionAuthFilter> sessionAuthFilterRegistration() {
        FilterRegistrationBean<SessionAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(sessionAuthFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(2);
        return registration;
    }

    // 注册权限拦截器
    @Autowired
    private UserPermissionInterceptor userPermissionInterceptor;

    @Autowired
    private PageAuthInterceptor pageAuthInterceptor;

    @Autowired
    private SessionCookieInterceptor sessionCookieInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionCookieInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(userAgentSessionFilter)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/login", "/api/logout", "/login", "/logout", "/error");

        registry.addInterceptor(pageAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/**", "/login", "/logout", "/error");

        registry.addInterceptor(userPermissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/logout", "/error");
    }

    @Bean
    public ErrorViewResolver errorViewResolver() {
        return (request, status, model) -> {
            String view = switch (status) {
                case FORBIDDEN -> "error/403";
                case NOT_FOUND -> "error/404";
                case INTERNAL_SERVER_ERROR -> "error/500";
                default -> null;
            };
            return view == null ? null : new ModelAndView(view, model);
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}