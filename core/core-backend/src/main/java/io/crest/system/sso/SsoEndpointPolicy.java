package io.crest.system.sso;

import io.crest.exception.DEException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.net.URI;
import java.util.Objects;

public final class SsoEndpointPolicy {

    private SsoEndpointPolicy() {
    }

    public static String validateEndpoint(String endpoint, Boolean requireHttps, String name) {
        if (StringUtils.isBlank(endpoint)) {
            DEException.throwException(name + "不能为空");
        }
        String value = endpoint.trim();
        if (!Objects.equals(value, endpoint)) {
            // Trimmed endpoints are accepted because many IdP admin consoles copy a trailing space.
            endpoint = value;
        }
        try {
            URI uri = URI.create(endpoint);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (!Strings.CI.equalsAny(scheme, "https", "http") || StringUtils.isBlank(host)) {
                DEException.throwException(name + "必须是 HTTP 或 HTTPS 绝对地址");
            }
            if (StringUtils.isNotBlank(uri.getUserInfo())) {
                DEException.throwException(name + "不能包含用户名或密码");
            }
            if (endpoint.chars().anyMatch(Character::isISOControl)) {
                DEException.throwException(name + "不能包含控制字符");
            }
            if (Boolean.TRUE.equals(requireHttps)
                    && !Strings.CI.equals(scheme, "https")
                    && !isLocalHost(host)) {
                DEException.throwException(name + "生产环境必须使用 HTTPS");
            }
            return endpoint;
        } catch (IllegalArgumentException e) {
            DEException.throwException(name + "格式无效");
            return null;
        }
    }

    private static boolean isLocalHost(String host) {
        String normalized = host;
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return Strings.CI.equalsAny(normalized, "localhost", "127.0.0.1", "::1");
    }
}
