package io.crest.utils;

import io.crest.exception.DEException;
import io.crest.result.ResultCode;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
     * 私有 IP 正则表达式
     */
    private static final Pattern PRIVATE_IP_PATTERN = Pattern.compile(
            "^(10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.)"
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
            String hostLower = host.toLowerCase();
            if (BLOCKED_HOSTS.contains(hostLower)) {
                throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                        "不允许访问内部地址: " + host);
            }

            // 4. 检查是否为 IP 地址
            if (isIpAddress(host)) {
                // 检查私有 IP
                if (PRIVATE_IP_PATTERN.matcher(host).matches()) {
                    throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                            "不允许访问私有网络地址: " + host);
                }

                // 解析 IP 并检查是否为回环/链路本地地址
                try {
                    InetAddress address = InetAddress.getByName(host);
                    if (address.isLoopbackAddress()) {
                        throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                                "不允许访问回环地址");
                    }
                    if (address.isLinkLocalAddress()) {
                        throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                                "不允许访问链路本地地址");
                    }
                    if (address.isSiteLocalAddress()) {
                        throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                                "不允许访问站点本地地址");
                    }
                } catch (UnknownHostException e) {
                    throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                            "无法解析主机名: " + host);
                }
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

    /**
     * 判断字符串是否为 IP 地址
     */
    private static boolean isIpAddress(String host) {
        return host.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    }
}
