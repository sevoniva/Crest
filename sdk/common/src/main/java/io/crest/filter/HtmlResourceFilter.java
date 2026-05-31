package io.crest.filter;

import jakarta.servlet.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlResourceFilter implements Filter, Ordered {

    private static final Pattern VERSIONED_FRONTEND_ASSET = Pattern.compile(
            "^(js|assets/(?:chunk|css|svg|png|jpg|jpeg|gif|ico|woff2?))/(.+)-\\d+\\.\\d+\\.\\d+-(crest.*)$"
    );

    @Value("${crest.http.cache:false}")
    private Boolean httpCache;

    @Value("${crest.version:}")
    private String configuredVersion;

    @Override
    public int getOrder() {
        return 99;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest request
                && servletResponse instanceof HttpServletResponse response
                && forwardStaleFrontendAsset(request, response)) {
            return;
        }

        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        applyNoCache(httpResponse);
        // 继续执行过滤器链
        filterChain.doFilter(servletRequest, httpResponse);
    }

    private boolean forwardStaleFrontendAsset(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getMethod();
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            return false;
        }

        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        requestPath = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;

        Matcher matcher = VERSIONED_FRONTEND_ASSET.matcher(requestPath);
        if (!matcher.matches()) {
            return false;
        }

        if (new ClassPathResource("static/" + requestPath).exists()) {
            return false;
        }

        String currentVersion = currentVersion();
        if (currentVersion == null || currentVersion.isBlank()) {
            return false;
        }

        String currentPath = matcher.group(1) + "/" + matcher.group(2) + "-" + currentVersion + "-" + matcher.group(3);
        if (!new ClassPathResource("static/" + currentPath).exists()) {
            return false;
        }

        applyNoCache(response);
        request.getRequestDispatcher("/" + currentPath).forward(request, response);
        return true;
    }

    private String currentVersion() {
        if (configuredVersion != null && !configuredVersion.isBlank()) {
            return configuredVersion.trim();
        }
        Package pkg = HtmlResourceFilter.class.getPackage();
        return pkg == null ? null : pkg.getImplementationVersion();
    }

    private void applyNoCache(HttpServletResponse response) {
        applySecurityHeaders(response);
        if (httpCache == null || !httpCache) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate");
            response.setHeader("Cache", "no-cache");
            response.setHeader(HttpHeaders.PRAGMA, "no-cache");
            response.setHeader(HttpHeaders.EXPIRES, "0");
        }
    }

    private void applySecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=(), payment=()");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
