package io.crest.security;

import io.crest.config.SecurityHeadersConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * 安全测试：HTTP 安全头
 *
 * 修复漏洞：DAST-05（缺少安全头）
 */
@ExtendWith(MockitoExtension.class)
class SecurityHeadersTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("应设置 X-Frame-Options 头")
    void shouldSetXFrameOptions() throws ServletException, IOException {
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader("X-Frame-Options", "DENY");
    }

    @Test
    @DisplayName("应设置 X-Content-Type-Options 头")
    void shouldSetXContentTypeOptions() throws ServletException, IOException {
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader("X-Content-Type-Options", "nosniff");
    }

    @Test
    @DisplayName("应设置 X-XSS-Protection 头")
    void shouldSetXXssProtection() throws ServletException, IOException {
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader("X-XSS-Protection", "1; mode=block");
    }

    @Test
    @DisplayName("应设置 Content-Security-Policy 头")
    void shouldSetCSP() throws ServletException, IOException {
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("Content-Security-Policy"), contains("default-src 'self'"));
    }

    @Test
    @DisplayName("应设置 Referrer-Policy 头")
    void shouldSetReferrerPolicy() throws ServletException, IOException {
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Test
    @DisplayName("应设置 Strict-Transport-Security 头")
    void shouldSetHSTS() throws ServletException, IOException {
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("Strict-Transport-Security"), contains("max-age="));
    }

    @Test
    @DisplayName("应设置缓存控制头")
    void shouldSetCacheControl() throws ServletException, IOException {
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        verify(response).setHeader("Pragma", "no-cache");
    }

    @Test
    @DisplayName("应移除服务器信息")
    void shouldRemoveServerInfo() throws ServletException, IOException {
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader("Server", "");
        verify(response).setHeader("X-Powered-By", "");
    }

    @Test
    @DisplayName("应继续执行过滤器链")
    void shouldContinueFilterChain() throws ServletException, IOException {
        SecurityHeadersConfig filter = new SecurityHeadersConfig();
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
