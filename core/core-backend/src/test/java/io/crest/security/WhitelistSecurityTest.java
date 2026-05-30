package io.crest.security;

import io.crest.utils.WhitelistUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全测试：验证敏感端点已从白名单移除
 *
 * 修复漏洞：PT-01（/symmetricKey 密钥泄露）
 * 注意：/dekey 保留在白名单中，因为登录流程需要 RSA 公钥
 */
class WhitelistSecurityTest {

    @Test
    @DisplayName("敏感端点 /symmetricKey 不应在白名单中")
    void symmetricKeyShouldNotBeInWhitelist() {
        assertFalse(WhitelistUtils.WHITE_PATH.contains("/symmetricKey"),
                "/symmetricKey 端点仍在白名单中，会导致 AES 密钥泄露");
    }

    @Test
    @DisplayName("/dekey 应在白名单中（登录流程需要）")
    void dekeyShouldBeInWhitelist() {
        assertTrue(WhitelistUtils.WHITE_PATH.contains("/dekey"),
                "/dekey 端点不在白名单中，登录流程将无法工作");
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

    @Test
    @DisplayName("/symmetricKey 不应在白名单中（通过match方法验证）")
    void symmetricKeyShouldNotMatch() {
        assertFalse(WhitelistUtils.match("/de2api/symmetricKey"),
                "/symmetricKey 应该需要认证");
    }
}
