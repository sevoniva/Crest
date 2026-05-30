package io.crest.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全头配置
 *
 * 添加 HTTP 安全响应头，增强应用安全性。
 *
 * 修复漏洞：DAST-05（缺少安全头）
 *
 * @author security-fix
 */
@Component
@Order(1)
public class SecurityHeadersConfig implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. 防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // 2. 防止 MIME 类型嗅探
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // 3. XSS 防护（旧版浏览器兼容）
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // 4. 内容安全策略
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: blob:; " +
                "font-src 'self' data:; " +
                "connect-src 'self' ws: wss:; " +
                "frame-ancestors 'none'");

        // 5. 引用策略
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // 6. 权限策略
        httpResponse.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=(), payment=()");

        // 7. 严格传输安全（HTTPS 环境生效）
        httpResponse.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains; preload");

        // 8. 缓存控制（敏感数据不缓存）
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        // 9. 移除服务器信息
        httpResponse.setHeader("Server", "");
        httpResponse.setHeader("X-Powered-By", "");

        chain.doFilter(request, response);
    }
}
