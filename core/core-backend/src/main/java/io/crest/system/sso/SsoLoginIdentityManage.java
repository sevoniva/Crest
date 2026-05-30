package io.crest.system.sso;

import io.crest.substitute.permissions.user.CrestUserManage;
import io.crest.substitute.permissions.user.model.CrestUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SsoLoginIdentityManage {

    private final SsoIdentityBindingResolver resolver = new SsoIdentityBindingResolver();

    @Resource
    private CrestUserManage crestUserManage;

    @Resource
    private SsoIdentityBindingManage bindingManage;

    @Transactional
    public CrestUser resolve(SsoIdentityProfile profile, boolean autoCreateUser, String providerName) {
        bindingManage.upsertProvider(profile.getProviderId(), profile.getProviderType(), providerName);
        Long boundUserId = bindingManage.boundUserId(profile);
        CrestUser boundUser = boundUserId == null ? null : crestUserManage.queryById(boundUserId);
        CrestUser accountUser = crestUserManage.queryByAccount(profile.getAccount());
        SsoIdentityDecision decision = resolver.resolve(profile, boundUser, accountUser, autoCreateUser);
        CrestUser user = crestUserManage.applySsoIdentity(decision);
        bindingManage.upsert(user.getId(), decision.getProfile());
        return user;
    }
}
