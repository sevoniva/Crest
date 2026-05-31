package io.crest.utils;

import io.crest.exception.DEException;
import io.crest.result.ResultCode;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * SSRF 防护工具类
 *
 * 防止服务器端请求伪造攻击，阻止访问内部网络资源。
 *
 * 修复漏洞：DAST-02, PT-02（SSRF 漏洞）
 *
 * @author security-fix
 */
public class SsrfProtection {

    /**
     * 允许的协议
     */
    private static final List<String> ALLOWED_PROTOCOLS = Arrays.asList("http", "https");

    /**
     * 阻止的主机名
     */
    private static final List<String> BLOCKED_HOSTS = Arrays.asList(
            "localhost",
            "127.0.0.1",
            "0.0.0.0",
            "169.254.169.254",
            "metadata.google.internal",
            "metadata.goog"
    );

    /**
     * 验证 URL 是否安全（非内部地址）
     *
     * @param url 待验证的 URL
     * @throws DEException 如果 URL 不安全
     */
    public static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        try {
            URI uri = new URI(url.trim());

            // 1. 检查协议
            String scheme = uri.getScheme();
            if (scheme == null || !ALLOWED_PROTOCOLS.contains(scheme.toLowerCase())) {
                throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                        "不允许的协议: " + scheme);
            }

            // 2. 检查主机名
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                        "无效的主机名");
            }

            // 3. 检查黑名单主机
            String normalizedHost = normalizeHost(host);
            if (BLOCKED_HOSTS.contains(normalizedHost)) {
                throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                        "不允许访问内部地址: " + host);
            }

            // 4. 解析主机名并检查所有解析结果，避免非标准 IP 表示法和 DNS 指向内网的绕过。
            try {
                InetAddress[] addresses = InetAddress.getAllByName(normalizedHost);
                for (InetAddress address : addresses) {
                    if (isBlockedAddress(address)) {
                        throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                                "不允许访问内部网络地址: " + host);
                    }
                }
            } catch (UnknownHostException e) {
                throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                        "无法解析主机名: " + host);
            }

            // 5. 检查端口（可选，阻止常见内部服务端口）
            int port = uri.getPort();
            if (port > 0) {
                List<Integer> blockedPorts = Arrays.asList(
                        3306, 5432, 6379, 27017, 9200, 11211  // 数据库和缓存端口
                );
                if (blockedPorts.contains(port)) {
                    throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                            "不允许访问内部服务端口: " + port);
                }
            }

        } catch (DEException e) {
            throw e;
        } catch (Exception e) {
            throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                    "无效的 URL: " + e.getMessage());
        }
    }

    private static String normalizeHost(String host) {
        String normalized = host.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static boolean isBlockedAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int first = bytes[0] & 0xff;
            int second = bytes[1] & 0xff;
            return first == 0
                    || first == 127
                    || first == 169 && second == 254
                    || first == 100 && second >= 64 && second <= 127;
        }
        return bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
    }
}
