package io.crest.system.sso;

import io.crest.exception.DEException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

public enum SsoProviderType {
    OIDC_GENERIC(true, false),
    CASDOOR(true, false),
    OAUTH2_GENERIC(false, false),
    FEISHU(false, true),
    WECOM(false, true);

    private final boolean oidc;
    private final boolean enterpriseApp;

    SsoProviderType(boolean oidc, boolean enterpriseApp) {
        this.oidc = oidc;
        this.enterpriseApp = enterpriseApp;
    }

    public boolean isOidc() {
        return oidc;
    }

    public boolean isEnterpriseApp() {
        return enterpriseApp;
    }

    public static SsoProviderType fromConfig(String value) {
        if (StringUtils.isBlank(value)) {
            return OIDC_GENERIC;
        }
        for (SsoProviderType type : values()) {
            if (Strings.CI.equals(type.name(), value.trim())) {
                return type;
            }
        }
        DEException.throwException("不支持的身份提供方类型：" + value);
        return OIDC_GENERIC;
    }
}
