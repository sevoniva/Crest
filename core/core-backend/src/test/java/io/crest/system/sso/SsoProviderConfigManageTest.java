package io.crest.system.sso;

import io.crest.api.system.vo.SsoConfigVO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SsoProviderConfigManageTest {

    @Test
    public void buildsDefaultProviderConfigFromCurrentSsoSettings() {
        SsoConfigVO config = new SsoConfigVO();
        config.setEnabled(true);
        config.setProviderName("飞书");
        config.setProviderType("FEISHU");
        config.setClientId("client-1");
        config.setAuthorizationEndpoint("https://open.feishu.cn/open-apis/authen/v1/authorize");
        config.setTokenEndpoint("https://open.feishu.cn/open-apis/authen/v1/access_token");
        config.setUserInfoEndpoint("https://open.feishu.cn/open-apis/authen/v1/user_info");
        config.setIssuer("https://open.feishu.cn");
        config.setScope("openid profile email");
        config.setRedirectUri("https://crest.example.com/de2api/sso/callback");
        config.setUserIdAttribute("open_id");
        config.setAccountAttribute("employee_no");
        config.setNameAttribute("name");
        config.setEmailAttribute("email");
        config.setUnionIdAttribute("union_id");
        config.setAutoCreateUser(false);
        config.setRequireHttps(true);

        SsoProviderConfig providerConfig = SsoProviderConfigManage.defaultProvider(config, "encrypted-secret");

        assertEquals(1L, providerConfig.getProviderId().longValue());
        assertEquals("default", providerConfig.getProviderKey());
        assertEquals(SsoProviderType.FEISHU, providerConfig.getProviderType());
        assertEquals("飞书", providerConfig.getName());
        assertTrue(providerConfig.getEnabled());
        assertEquals("client-1", providerConfig.getClientId());
        assertEquals("encrypted-secret", providerConfig.getClientSecret());
        assertEquals("union_id", providerConfig.getUnionIdAttribute());
        assertEquals(false, providerConfig.getAutoCreateUser());
        assertEquals(true, providerConfig.getRequireHttps());
    }
}
