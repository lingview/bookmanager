package xyz.lingview.astra.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class StaticCacheHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/assets/")) {
            response.setHeader("Cache-Control", "no-cache");
        } else if (path.equals("/echarts.min.js")) {
            response.setHeader("Cache-Control", "public, max-age=2592000, immutable");
        } else if (path.startsWith("/file/")) {
            response.setHeader("Cache-Control", "private, max-age=2592000, immutable");
        } else {
            response.setHeader("Cache-Control", "no-cache");
        }
        chain.doFilter(request, response);
    }
}
