package io.crest.security;

import io.crest.exception.DEException;
import io.crest.utils.SsrfProtection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全测试：SSRF 防护
 *
 * 修复漏洞：DAST-02, PT-02（SSRF 漏洞）
 */
class SsrfProtectionTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://127.0.0.1/",
            "http://127.1/",
            "http://2130706433/",
            "http://[::1]/",
            "http://localhost/",
            "http://0.0.0.0/",
            "http://169.254.169.254/latest/meta-data/",
            "http://10.0.0.1/",
            "http://172.16.0.1/",
            "http://192.168.1.1/",
            "file:///etc/passwd",
            "gopher://127.0.0.1/"
    })
    @DisplayName("应阻止访问内部地址和危险协议")
    void shouldBlockInternalAddresses(String url) {
        assertThrows(DEException.class, () -> SsrfProtection.validateUrl(url),
                "应阻止访问: " + url);
    }

    @Test
    @DisplayName("应允许访问外部 HTTP 地址")
    void shouldAllowExternalHttp() {
        assertDoesNotThrow(() -> SsrfProtection.validateUrl("http://93.184.216.34/"));
    }

    @Test
    @DisplayName("应允许访问外部 HTTPS 地址")
    void shouldAllowExternalHttps() {
        assertDoesNotThrow(() -> SsrfProtection.validateUrl("https://93.184.216.34/"));
    }

    @Test
    @DisplayName("应阻止访问数据库端口")
    void shouldBlockDatabasePorts() {
        assertThrows(DEException.class,
                () -> SsrfProtection.validateUrl("http://example.com:3306/"));
        assertThrows(DEException.class,
                () -> SsrfProtection.validateUrl("http://example.com:5432/"));
        assertThrows(DEException.class,
                () -> SsrfProtection.validateUrl("http://example.com:6379/"));
    }

    @Test
    @DisplayName("空 URL 不应抛出异常")
    void shouldAllowNullUrl() {
        assertDoesNotThrow(() -> SsrfProtection.validateUrl(null));
        assertDoesNotThrow(() -> SsrfProtection.validateUrl(""));
        assertDoesNotThrow(() -> SsrfProtection.validateUrl("  "));
    }

    @Test
    @DisplayName("应阻止 Google Cloud 元数据地址")
    void shouldBlockGoogleMetadata() {
        assertThrows(DEException.class,
                () -> SsrfProtection.validateUrl("http://metadata.google.internal/"));
    }
}
