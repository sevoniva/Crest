package io.crest.system.manage;

import io.crest.api.system.vo.SsoConfigVO;
import io.crest.system.sso.SsoIdentityProfile;
import io.crest.system.sso.SsoProviderType;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SsoManageProfileMappingTest {

    @Test
    public void mapsConfiguredProviderTypeAndUnionIdClaim() throws Exception {
        SsoConfigVO config = new SsoConfigVO();
        config.setProviderType("FEISHU");
        config.setUserIdAttribute("open_id");
        config.setAccountAttribute("employee_no");
        config.setNameAttribute("name");
        config.setEmailAttribute("email");
        config.setUnionIdAttribute("union_id");

        Map<String, Object> claims = Map.of(
                "open_id", "ou_xxx",
                "employee_no", "E1001",
                "name", "李四",
                "email", "lisi@example.com",
                "union_id", "onion_xxx"
        );

        SsoIdentityProfile profile = invokeProfile(config, claims);

        assertEquals(SsoProviderType.FEISHU, profile.getProviderType());
        assertEquals("ou_xxx", profile.getExternalSubject());
        assertEquals("E1001", profile.getAccount());
        assertEquals("李四", profile.getName());
        assertEquals("lisi@example.com", profile.getEmail());
        assertEquals("onion_xxx", profile.getUnionId());
    }

    private SsoIdentityProfile invokeProfile(SsoConfigVO config, Map<String, Object> claims) throws Exception {
        SsoManage manage = new SsoManage();
        Method profileMethod = SsoManage.class.getDeclaredMethod("profile", SsoConfigVO.class, Map.class);
        profileMethod.setAccessible(true);
        return (SsoIdentityProfile) profileMethod.invoke(manage, config, claims);
    }
}
