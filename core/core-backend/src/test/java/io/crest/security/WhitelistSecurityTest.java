package io.crest.security;

import io.crest.utils.WhitelistUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全测试：验证敏感端点已从白名单移除
 *
 * 修复漏洞：SAST-02, PT-01（加密密钥端点泄露）
 */
class WhitelistSecurityTest {

    @Test
    @DisplayName("敏感端点 /dekey 不应在白名单中")
    void dekeyShouldNotBeInWhitelist() {
        assertFalse(WhitelistUtils.WHITE_PATH.contains("/dekey"),
                "/dekey 端点仍在白名单中，会导致 RSA 公钥泄露");
    }

    @Test
    @DisplayName("敏感端点 /symmetricKey 不应在白名单中")
    void symmetricKeyShouldNotBeInWhitelist() {
        assertFalse(WhitelistUtils.WHITE_PATH.contains("/symmetricKey"),
                "/symmetricKey 端点仍在白名单中，会导致 AES 密钥泄露");
    }

    @Test
    @DisplayName("登录端点应在白名单中")
    void loginShouldBeInWhitelist() {
        assertTrue(WhitelistUtils.WHITE_PATH.contains("/login/localLogin"),
                "登录端点应在白名单中");
    }

    @Test
    @DisplayName("健康检查端点应在白名单中")
    void healthShouldBeInWhitelist() {
        assertTrue(WhitelistUtils.WHITE_PATH.contains("/actuator/health"),
                "健康检查端点应在白名单中");
    }
}
