package io.crest.system.sso;

import io.crest.exception.DEException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public final class SsoClaimMapper {

    private SsoClaimMapper() {
    }

    public static SsoIdentityProfile map(Long providerId,
                                         SsoProviderType providerType,
                                         Map<String, Object> claims,
                                         String subjectAttribute,
                                         String accountAttribute,
                                         String nameAttribute,
                                         String emailAttribute,
                                         String unionIdAttribute) {
        if (providerId == null) {
            DEException.throwException("身份提供方 ID 不能为空");
        }
        if (providerType == null) {
            DEException.throwException("身份提供方类型不能为空");
        }
        String subject = claim(claims, subjectAttribute);
        if (StringUtils.isBlank(subject)) {
            DEException.throwException("身份提供方未返回用户唯一标识：" + subjectAttribute);
        }
        String account = SsoAccountPolicy.normalizeAccount(claim(claims, accountAttribute), subject);
        String name = SsoAccountPolicy.normalizeDisplayName(claim(claims, nameAttribute), account);

        SsoIdentityProfile profile = new SsoIdentityProfile();
        profile.setProviderId(providerId);
        profile.setProviderType(providerType);
        profile.setExternalSubject(subject.trim());
        profile.setAccount(account);
        profile.setName(name);
        profile.setEmail(StringUtils.trimToNull(claim(claims, emailAttribute)));
        profile.setUnionId(StringUtils.trimToNull(claim(claims, unionIdAttribute)));
        return profile;
    }

    @SuppressWarnings("unchecked")
    static String claim(Map<String, Object> claims, String name) {
        if (claims == null || StringUtils.isBlank(name)) {
            return null;
        }
        Object current = claims;
        for (String part : name.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = ((Map<String, Object>) map).get(part);
        }
        return current == null ? null : current.toString();
    }
}
