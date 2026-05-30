package io.crest.security;

import io.crest.exception.DEException;
import io.crest.utils.PasswordValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全测试：密码策略验证器
 *
 * 修复漏洞：PT-05（弱密码策略）
 */
class PasswordValidatorTest {

    @Test
    @DisplayName("强密码应通过验证")
    void shouldAcceptStrongPassword() {
        assertDoesNotThrow(() -> PasswordValidator.validate("MyStr0ng!Pass"));
        assertDoesNotThrow(() -> PasswordValidator.validate("C0mplex@Password"));
        assertDoesNotThrow(() -> PasswordValidator.validate("Test1234!@#$"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123",
            "1234567",
            "password",
            "Pass1!",
            "short"
    })
    @DisplayName("弱密码应被拒绝")
    void shouldRejectWeakPasswords(String password) {
        assertThrows(DEException.class, () -> PasswordValidator.validate(password));
    }

    @Test
    @DisplayName("缺少大写字母应被拒绝")
    void shouldRejectWithoutUppercase() {
        assertThrows(DEException.class, () -> PasswordValidator.validate("mypassword1!"));
    }

    @Test
    @DisplayName("缺少小写字母应被拒绝")
    void shouldRejectWithoutLowercase() {
        assertThrows(DEException.class, () -> PasswordValidator.validate("MYPASSWORD1!"));
    }

    @Test
    @DisplayName("缺少数字应被拒绝")
    void shouldRejectWithoutDigit() {
        assertThrows(DEException.class, () -> PasswordValidator.validate("MyPassword!"));
    }

    @Test
    @DisplayName("缺少特殊字符应被拒绝")
    void shouldRejectWithoutSpecialChar() {
        assertThrows(DEException.class, () -> PasswordValidator.validate("MyPassword1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12345678",
            "password",
            "admin123",
            "qwerty123"
    })
    @DisplayName("常见弱密码应被拒绝")
    void shouldRejectCommonPasswords(String password) {
        assertThrows(DEException.class, () -> PasswordValidator.validate(password));
    }

    @Test
    @DisplayName("连续相同字符应被拒绝")
    void shouldRejectSequentialChars() {
        assertThrows(DEException.class, () -> PasswordValidator.validate("Aaa111!@#"));
    }

    @Test
    @DisplayName("null 密码应抛出异常")
    void shouldRejectNull() {
        assertThrows(DEException.class, () -> PasswordValidator.validate(null));
    }

    @Test
    @DisplayName("空密码应抛出异常")
    void shouldRejectEmpty() {
        assertThrows(DEException.class, () -> PasswordValidator.validate(""));
    }

    @Test
    @DisplayName("应返回策略描述")
    void shouldReturnPolicyDescription() {
        String description = PasswordValidator.getPolicyDescription();
        assertNotNull(description);
        assertTrue(description.contains("8"));
        assertTrue(description.contains("大写"));
    }
}
