package io.crest.utils;

import java.util.regex.Pattern;

/**
 * XSS 防护工具类
 *
 * 提供输入清理和输出编码功能，防止跨站脚本攻击。
 *
 * 修复漏洞：DAST-01（存储型 XSS）
 *
 * @author security-fix
 */
public class XssProtection {

    /**
     * HTML 标签正则表达式
     */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    /**
     * 事件处理器正则表达式（on* 属性）
     */
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
            "(?i)\\s+on\\w+\\s*="
    );

    /**
     * JavaScript 协议正则表达式
     */
    private static final Pattern JS_PROTOCOL_PATTERN = Pattern.compile(
            "(?i)javascript\\s*:"
    );

    /**
     * 清理输入，移除潜在的 XSS 攻击向量
     *
     * @param input 原始输入
     * @return 清理后的输入
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String result = input;

        // 1. 移除 HTML 标签
        result = HTML_TAG_PATTERN.matcher(result).replaceAll("");

        // 2. 移除事件处理器
        result = EVENT_HANDLER_PATTERN.matcher(result).replaceAll(" ");

        // 3. 移除 javascript: 协议
        result = JS_PROTOCOL_PATTERN.matcher(result).replaceAll("");

        // 4. 转义 HTML 实体
        result = encodeForHtml(result);

        return result;
    }

    /**
     * 转义 HTML 特殊字符
     *
     * @param input 原始字符串
     * @return 转义后的字符串
     */
    public static String encodeForHtml(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * 清理文件名，移除路径遍历字符
     *
     * @param filename 原始文件名
     * @return 安全的文件名
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }

        // 移除路径分隔符和遍历字符
        return filename
                .replaceAll("[/\\\\]", "")
                .replaceAll("\\.\\.", "")
                .replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    /**
     * 验证字符串是否包含潜在的 XSS 内容
     *
     * @param input 待验证的字符串
     * @return 如果包含潜在 XSS 内容返回 true
     */
    public static boolean containsXss(String input) {
        if (input == null) {
            return false;
        }

        String lower = input.toLowerCase();

        // 检查常见 XSS 攻击模式
        return lower.contains("<script")
                || lower.contains("</script")
                || lower.contains("javascript:")
                || lower.contains("onerror=")
                || lower.contains("onload=")
                || lower.contains("onclick=")
                || lower.contains("onmouseover=")
                || lower.contains("onfocus=")
                || lower.contains("onblur=")
                || lower.contains("expression(")
                || lower.contains("eval(")
                || lower.contains("document.cookie")
                || lower.contains("document.write");
    }
}
