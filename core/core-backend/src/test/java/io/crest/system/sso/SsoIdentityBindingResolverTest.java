package io.crest.system.sso;

import io.crest.exception.DEException;
import io.crest.substitute.permissions.user.CrestUserManage;
import io.crest.substitute.permissions.user.model.CrestUser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SsoIdentityBindingResolverTest {

    private final SsoIdentityBindingResolver resolver = new SsoIdentityBindingResolver();

    @Test
    public void updatesUserWhenProviderSubjectIsAlreadyBound() {
        CrestUser bound = user(1L, "old.account", true);

        SsoIdentityDecision decision = resolver.resolve(profile("new.account"), bound, null, true);

        assertEquals(SsoIdentityAction.UPDATE_BOUND_USER, decision.getAction());
        assertEquals(1L, decision.getUserId().longValue());
    }

    @Test
    public void rejectsDisabledBoundUser() {
        CrestUser bound = user(1L, "zhang.san", false);

        assertThrows(DEException.class, () -> resolver.resolve(profile("zhang.san"), bound, null, true));
    }

    @Test
    public void rejectsAccountConflictWhenBoundUserDiffersFromAccountUser() {
        CrestUser bound = user(1L, "old.account", true);
        CrestUser accountUser = user(2L, "zhang.san", true);

        assertThrows(DEException.class, () -> resolver.resolve(profile("zhang.san"), bound, accountUser, true));
    }

    @Test
    public void bindsExistingLocalUserWhenAccountMatches() {
        CrestUser accountUser = user(2L, "zhang.san", true);

        SsoIdentityDecision decision = resolver.resolve(profile("zhang.san"), null, accountUser, true);

        assertEquals(SsoIdentityAction.BIND_EXISTING_USER, decision.getAction());
        assertEquals(2L, decision.getUserId().longValue());
    }

    @Test
    public void rejectsDisabledAccountUserBeforeBinding() {
        CrestUser accountUser = user(2L, "zhang.san", false);

        assertThrows(DEException.class, () -> resolver.resolve(profile("zhang.san"), null, accountUser, true));
    }

    @Test
    public void createsUserWhenNoBindingAndAutoCreateEnabled() {
        SsoIdentityDecision decision = resolver.resolve(profile("zhang.san"), null, null, true);

        assertEquals(SsoIdentityAction.CREATE_USER, decision.getAction());
        assertEquals("zhang.san", decision.getProfile().getAccount());
    }

    @Test
    public void rejectsUnknownUserWhenAutoCreateDisabled() {
        assertThrows(DEException.class, () -> resolver.resolve(profile("zhang.san"), null, null, false));
    }

    @Test
    public void acceptsSameUserForBoundAndAccountMatch() {
        CrestUser bound = user(1L, "zhang.san", true);
        CrestUser accountUser = user(1L, "zhang.san", true);

        SsoIdentityDecision decision = resolver.resolve(profile("zhang.san"), bound, accountUser, true);

        assertEquals(SsoIdentityAction.UPDATE_BOUND_USER, decision.getAction());
        assertEquals(1L, decision.getUserId().longValue());
    }

    @Test
    public void rejectsProfileWithoutProviderId() {
        SsoIdentityProfile profile = profile("zhang.san");
        profile.setProviderId(null);

        assertThrows(DEException.class, () -> resolver.resolve(profile, null, null, true));
    }

    @Test
    public void rejectsProfileWithoutProviderType() {
        SsoIdentityProfile profile = profile("zhang.san");
        profile.setProviderType(null);

        assertThrows(DEException.class, () -> resolver.resolve(profile, null, null, true));
    }

    @Test
    public void rejectsProfileWithoutSubject() {
        SsoIdentityProfile profile = profile("zhang.san");
        profile.setExternalSubject(null);

        assertThrows(DEException.class, () -> resolver.resolve(profile, null, null, true));
    }

    @Test
    public void normalizesAccountAndDisplayNameBeforeDecision() {
        SsoIdentityProfile profile = profile("zhang.san");
        profile.setName(null);

        SsoIdentityDecision decision = resolver.resolve(profile, null, null, true);

        assertEquals("zhang.san", decision.getProfile().getAccount());
        assertEquals("zhang.san", decision.getProfile().getName());
    }

    private SsoIdentityProfile profile(String account) {
        SsoIdentityProfile profile = new SsoIdentityProfile();
        profile.setProviderId(100L);
        profile.setProviderType(SsoProviderType.CASDOOR);
        profile.setExternalSubject("external-1");
        profile.setAccount(account);
        profile.setName("张三");
        profile.setEmail("zhang.san@example.com");
        return profile;
    }

    private CrestUser user(Long id, String account, boolean enable) {
        CrestUser user = new CrestUser();
        user.setId(id);
        user.setAccount(account);
        user.setName(account);
        user.setEnable(enable);
        user.setAuthType(CrestUserManage.AUTH_TYPE_LOCAL);
        return user;
    }
}
