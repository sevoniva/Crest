package io.crest.system.sso;

import lombok.Data;

@Data
public class SsoProviderConfig {
    private Long providerId;
    private String providerKey;
    private SsoProviderType providerType;
    private String name;
    private Boolean enabled;
    private String clientId;
    private String clientSecret;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userInfoEndpoint;
    private String issuer;
    private String scope;
    private String redirectUri;
    private String userIdAttribute;
    private String accountAttribute;
    private String nameAttribute;
    private String emailAttribute;
    private String unionIdAttribute;
    private Boolean autoCreateUser;
    private Boolean requireHttps;
}
