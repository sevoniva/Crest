package io.crest.system.sso;

import lombok.Data;

@Data
public class SsoIdentityProfile {
    private Long providerId;
    private SsoProviderType providerType;
    private String externalSubject;
    private String account;
    private String name;
    private String email;
    private String unionId;
}
