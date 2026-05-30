package io.crest.security;

import io.crest.utils.PasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全测试：密码编码器
 *
 * 修复漏洞：SAST-03（MD5 密码哈希）
 */
class PasswordEncoderTest {

    @Test
    @DisplayName("应正确编码密码")
    void shouldEncodePassword() {
        String encoded = PasswordEncoder.encode("TestPassword123!");
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
        // 格式应为：iterations:salt:hash
        String[] parts = encoded.split(":");
        assertEquals(3, parts.length);
    }

    @Test
    @DisplayName("相同密码应产生不同的哈希（随机盐）")
    void shouldProduceDifferentHashesForSamePassword() {
        String encoded1 = PasswordEncoder.encode("TestPassword123!");
        String encoded2 = PasswordEncoder.encode("TestPassword123!");
        assertNotEquals(encoded1, encoded2);
    }

    @Test
    @DisplayName("应正确验证密码")
    void shouldVerifyPassword() {
        String password = "TestPassword123!";
        String encoded = PasswordEncoder.encode(password);
        assertTrue(PasswordEncoder.matches(password, encoded));
    }

    @Test
    @DisplayName("错误密码应验证失败")
    void shouldRejectWrongPassword() {
        String encoded = PasswordEncoder.encode("TestPassword123!");
        assertFalse(PasswordEncoder.matches("WrongPassword", encoded));
    }

    @Test
    @DisplayName("null 密码应验证失败")
    void shouldHandleNullPassword() {
        String encoded = PasswordEncoder.encode("TestPassword123!");
        assertFalse(PasswordEncoder.matches(null, encoded));
        assertFalse(PasswordEncoder.matches("Test", null));
    }

    @Test
    @DisplayName("应检测 MD5 格式需要升级")
    void shouldDetectMd5NeedsUpgrade() {
        // MD5 格式：32 位十六进制
        String md5Hash = "e10adc3949ba59abbe56e057f20f883e";
        assertTrue(PasswordEncoder.needsReEncoding(md5Hash));
    }

    @Test
    @DisplayName("应检测新格式不需要升级")
    void shouldDetectNewFormatNoUpgrade() {
        String encoded = PasswordEncoder.encode("TestPassword123!");
        assertFalse(PasswordEncoder.needsReEncoding(encoded));
    }

    @Test
    @DisplayName("null 应需要升级")
    void shouldDetectNullNeedsUpgrade() {
        assertTrue(PasswordEncoder.needsReEncoding(null));
    }

    @Test
    @DisplayName("null 密码编码应抛出异常")
    void shouldThrowOnNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordEncoder.encode(null));
    }
}
