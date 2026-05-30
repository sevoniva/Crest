package io.crest.security;

import io.crest.utils.XssProtection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全测试：XSS 防护
 *
 * 修复漏洞：DAST-01（存储型 XSS）
 */
class XssProtectionTest {

    @Test
    @DisplayName("应移除 script 标签")
    void shouldRemoveScriptTags() {
        String input = "<script>alert(1)</script>Hello";
        String result = XssProtection.sanitize(input);
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("</script>"));
        assertTrue(result.contains("Hello"));
    }

    @Test
    @DisplayName("应移除事件处理器")
    void shouldRemoveEventHandlers() {
        String input = "<img src=x onerror=alert(1)>";
        String result = XssProtection.sanitize(input);
        assertFalse(result.toLowerCase().contains("onerror"));
    }

    @Test
    @DisplayName("应移除 javascript: 协议")
    void shouldRemoveJavascriptProtocol() {
        String input = "<a href=\"javascript:alert(1)\">Click</a>";
        String result = XssProtection.sanitize(input);
        assertFalse(result.toLowerCase().contains("javascript:"));
    }

    @Test
    @DisplayName("应转义 HTML 实体")
    void shouldEncodeHtmlEntities() {
        String input = "<div>Test & \"quotes\"</div>";
        String result = XssProtection.encodeForHtml(input);
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
        assertTrue(result.contains("&amp;"));
        assertTrue(result.contains("&quot;"));
    }

    @Test
    @DisplayName("正常文本应保持不变")
    void shouldPreserveNormalText() {
        String input = "Hello World 123";
        String result = XssProtection.sanitize(input);
        assertEquals("Hello World 123", result);
    }

    @Test
    @DisplayName("应检测 XSS 内容")
    void shouldDetectXssContent() {
        assertTrue(XssProtection.containsXss("<script>alert(1)</script>"));
        assertTrue(XssProtection.containsXss("onerror=alert(1)"));
        assertTrue(XssProtection.containsXss("javascript:alert(1)"));
        assertTrue(XssProtection.containsXss("document.cookie"));
        assertFalse(XssProtection.containsXss("Hello World"));
        assertFalse(XssProtection.containsXss(null));
    }

    @Test
    @DisplayName("应清理文件名")
    void shouldSanitizeFilename() {
        assertEquals("test.txt", XssProtection.sanitizeFilename("test.txt"));
        assertEquals("test.txt", XssProtection.sanitizeFilename("../../../test.txt"));
        assertEquals("test.txt", XssProtection.sanitizeFilename("..\\..\\test.txt"));
        assertNull(XssProtection.sanitizeFilename(null));
    }

    @Test
    @DisplayName("null 输入应返回 null")
    void shouldHandleNullInput() {
        assertNull(XssProtection.sanitize(null));
        assertNull(XssProtection.encodeForHtml(null));
        assertFalse(XssProtection.containsXss(null));
    }
}
