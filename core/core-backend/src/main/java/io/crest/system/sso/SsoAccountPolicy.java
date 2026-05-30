package io.crest.system.sso;

import io.crest.exception.DEException;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

public final class SsoAccountPolicy {

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[A-Za-z0-9._@-]{1,64}$");
    private static final Pattern UNSAFE_DISPLAY_NAME_PATTERN = Pattern.compile("[<>\\p{Cntrl}]");

    private SsoAccountPolicy() {
    }

    public static String normalizeAccount(String account, String fallbackSubject) {
        String value = StringUtils.defaultIfBlank(account, fallbackSubject);
        if (StringUtils.isBlank(value)) {
            DEException.throwException("单点登录账号不能为空");
        }
        String trimmed = value.trim();
        if (!Objects.equals(value, trimmed) || !ACCOUNT_PATTERN.matcher(trimmed).matches()) {
            DEException.throwException("单点登录账号只支持 64 位以内的字母、数字、点、下划线、横线和 @");
        }
        return trimmed;
    }

    public static String normalizeDisplayName(String displayName, String fallbackAccount) {
        String value = StringUtils.defaultIfBlank(displayName, fallbackAccount);
        if (StringUtils.isBlank(value)) {
            DEException.throwException("单点登录用户姓名不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.length() > 64) {
            DEException.throwException("单点登录用户姓名不能超过 64 个字符");
        }
        if (UNSAFE_DISPLAY_NAME_PATTERN.matcher(trimmed).find()) {
            DEException.throwException("单点登录用户姓名不能包含 HTML 标签或控制字符");
        }
        return trimmed;
    }
}
