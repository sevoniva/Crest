package io.crest.system.sso;

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
}
