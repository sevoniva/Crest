package io.crest.system.sso;

import io.crest.exception.DEException;
import io.crest.substitute.permissions.user.model.CrestUser;
import org.apache.commons.lang3.StringUtils;

public class SsoIdentityBindingResolver {

    public SsoIdentityDecision resolve(SsoIdentityProfile profile,
                                       CrestUser boundUser,
                                       CrestUser accountUser,
                                       boolean autoCreateUser) {
        SsoIdentityProfile normalized = normalize(profile);
        if (boundUser != null) {
            ensureEnabled(boundUser);
            if (accountUser != null && !boundUser.getId().equals(accountUser.getId())) {
                DEException.throwException("单点登录账号已被其他用户占用");
            }
            return new SsoIdentityDecision(SsoIdentityAction.UPDATE_BOUND_USER, boundUser.getId(), normalized);
        }
        if (accountUser != null) {
            ensureEnabled(accountUser);
            return new SsoIdentityDecision(SsoIdentityAction.BIND_EXISTING_USER, accountUser.getId(), normalized);
        }
        if (!autoCreateUser) {
            DEException.throwException("用户不存在，且未启用自动创建用户");
        }
        return new SsoIdentityDecision(SsoIdentityAction.CREATE_USER, null, normalized);
    }

    private SsoIdentityProfile normalize(SsoIdentityProfile profile) {
        if (profile == null) {
            DEException.throwException("单点登录用户信息不能为空");
        }
        if (profile.getProviderId() == null) {
            DEException.throwException("身份提供方 ID 不能为空");
        }
        if (profile.getProviderType() == null) {
            DEException.throwException("身份提供方类型不能为空");
        }
        if (StringUtils.isBlank(profile.getExternalSubject())) {
            DEException.throwException("身份提供方用户唯一标识不能为空");
        }
        String account = SsoAccountPolicy.normalizeAccount(profile.getAccount(), profile.getExternalSubject());
        String name = SsoAccountPolicy.normalizeDisplayName(profile.getName(), account);

        SsoIdentityProfile normalized = new SsoIdentityProfile();
        normalized.setProviderId(profile.getProviderId());
        normalized.setProviderType(profile.getProviderType());
        normalized.setExternalSubject(profile.getExternalSubject().trim());
        normalized.setAccount(account);
        normalized.setName(name);
        normalized.setEmail(StringUtils.trimToNull(profile.getEmail()));
        normalized.setUnionId(StringUtils.trimToNull(profile.getUnionId()));
        return normalized;
    }

    private void ensureEnabled(CrestUser user) {
        if (Boolean.FALSE.equals(user.getEnable())) {
            DEException.throwException("用户已停用");
        }
    }
}
