package io.crest.substitute.permissions.auth;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class PlatformPermissionPolicyTest {

    @Test
    public void permitsSystemAdminWithoutResourceGrant() {
        PlatformPermissionPolicy policy = new PlatformPermissionPolicy(true, 2L, Set.of(), Set.of(), 99L);

        Assert.assertTrue(policy.canAccess(1L, Set.of(), "manage"));
    }

    @Test
    public void permitsCreatorWithoutExplicitGrant() {
        PlatformPermissionPolicy policy = new PlatformPermissionPolicy(false, 7L, Set.of(), Set.of(), 99L);

        Assert.assertTrue(policy.canAccess(7L, Set.of(), "read"));
    }

    @Test
    public void permitsDirectUserGrant() {
        PlatformPermissionPolicy policy = new PlatformPermissionPolicy(false, 7L, Set.of("read"), Set.of(), 99L);

        Assert.assertTrue(policy.canAccess(8L, Set.of(), "read"));
    }

    @Test
    public void permitsRoleGrant() {
        PlatformPermissionPolicy policy = new PlatformPermissionPolicy(false, 7L, Set.of(), Set.of("manage"), 99L);

        Assert.assertTrue(policy.canAccess(8L, Set.of(3L), "manage"));
    }

    @Test
    public void deniesUserOutsideCreatorAndGrants() {
        PlatformPermissionPolicy policy = new PlatformPermissionPolicy(false, 7L, Set.of(), Set.of(), 99L);

        Assert.assertFalse(policy.canAccess(8L, Set.of(3L), "read"));
    }
}
