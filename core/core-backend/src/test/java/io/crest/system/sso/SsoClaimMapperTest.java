package io.crest.system.sso;

import io.crest.exception.DEException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class SsoClaimMapperTest {

    @Test
    public void mapsNestedOidcClaimsIntoIdentityProfile() {
        Map<String, Object> claims = Map.of(
                "sub", "casdoor-user-1",
                "preferred_username", "zhang.san",
                "profile", Map.of("displayName", "张三"),
                "email", "zhang.san@example.com",
                "extra", Map.of("unionId", "union-1")
        );

        SsoIdentityProfile profile = SsoClaimMapper.map(
                100L,
                SsoProviderType.CASDOOR,
                claims,
                "sub",
                "preferred_username",
                "profile.displayName",
                "email",
                "extra.unionId"
        );

        assertEquals(100L, profile.getProviderId().longValue());
        assertEquals(SsoProviderType.CASDOOR, profile.getProviderType());
        assertEquals("casdoor-user-1", profile.getExternalSubject());
        assertEquals("zhang.san", profile.getAccount());
        assertEquals("张三", profile.getName());
        assertEquals("zhang.san@example.com", profile.getEmail());
        assertEquals("union-1", profile.getUnionId());
    }

    @Test
    public void mapsFeishuStyleClaimsWithoutOidcAssumptions() {
        Map<String, Object> claims = Map.of(
                "open_id", "ou_xxx",
                "employee_no", "E1001",
                "name", "李四",
                "email", "lisi@example.com",
                "union_id", "onion_xxx"
        );

        SsoIdentityProfile profile = SsoClaimMapper.map(
                200L,
                SsoProviderType.FEISHU,
                claims,
                "open_id",
                "employee_no",
                "name",
                "email",
                "union_id"
        );

        assertEquals("ou_xxx", profile.getExternalSubject());
        assertEquals("E1001", profile.getAccount());
        assertEquals("李四", profile.getName());
        assertEquals("lisi@example.com", profile.getEmail());
        assertEquals("onion_xxx", profile.getUnionId());
    }

    @Test
    public void mapsWecomStyleClaimsAndFallsBackAccountToSubject() {
        Map<String, Object> claims = Map.of(
                "userid", "wangwu",
                "name", "王五"
        );

        SsoIdentityProfile profile = SsoClaimMapper.map(
                300L,
                SsoProviderType.WECOM,
                claims,
                "userid",
                "missing_account",
                "name",
                "missing_email",
                "missing_union"
        );

        assertEquals("wangwu", profile.getExternalSubject());
        assertEquals("wangwu", profile.getAccount());
        assertEquals("王五", profile.getName());
        assertNull(profile.getEmail());
        assertNull(profile.getUnionId());
    }

    @Test
    public void rejectsMissingExternalSubject() {
        Map<String, Object> claims = Map.of("preferred_username", "zhang.san");

        assertThrows(DEException.class, () -> SsoClaimMapper.map(
                100L,
                SsoProviderType.OIDC_GENERIC,
                claims,
                "sub",
                "preferred_username",
                "name",
                "email",
                "union_id"
        ));
    }

    @Test
    public void rejectsUnsafeMappedAccount() {
        Map<String, Object> claims = Map.of(
                "sub", "user-1",
                "preferred_username", "用户一"
        );

        assertThrows(DEException.class, () -> SsoClaimMapper.map(
                100L,
                SsoProviderType.OIDC_GENERIC,
                claims,
                "sub",
                "preferred_username",
                "name",
                "email",
                "union_id"
        ));
    }

    @Test
    public void rejectsUnsafeMappedDisplayName() {
        Map<String, Object> claims = Map.of(
                "sub", "user-1",
                "preferred_username", "user.one",
                "name", "<script>alert(1)</script>"
        );

        assertThrows(DEException.class, () -> SsoClaimMapper.map(
                100L,
                SsoProviderType.OIDC_GENERIC,
                claims,
                "sub",
                "preferred_username",
                "name",
                "email",
                "union_id"
        ));
    }
}
