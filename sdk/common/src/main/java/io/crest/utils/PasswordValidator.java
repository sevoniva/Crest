package io.crest.utils;

import io.crest.exception.DEException;
import io.crest.result.ResultCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 密码策略验证器
 *
 * 验证密码是否符合安全策略要求。
 *
 * 修复漏洞：PT-05（弱密码策略）
 *
 * @author security-fix
 */
public class PasswordValidator {

    /**
     * 最小密码长度
     */
    private static final int MIN_LENGTH = 8;

    /**
     * 最大密码长度
     */
    private static final int MAX_LENGTH = 100;

    /**
     * 验证密码是否符合策略
     *
     * @param password 待验证的密码
     * @throws DEException 如果密码不符合策略
     */
    public static void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new DEException(ResultCode.PARAM_IS_INVALID.code(), "密码不能为空");
        }

        List<String> errors = new ArrayList<>();

        // 1. 长度检查
        if (password.length() < MIN_LENGTH) {
            errors.add("密码长度不能少于 " + MIN_LENGTH + " 个字符");
        }
        if (password.length() > MAX_LENGTH) {
            errors.add("密码长度不能超过 " + MAX_LENGTH + " 个字符");
        }

        // 2. 复杂度检查
        if (!password.matches(".*[A-Z].*")) {
            errors.add("密码必须包含至少一个大写字母");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("密码必须包含至少一个小写字母");
        }
        if (!password.matches(".*\\d.*")) {
            errors.add("密码必须包含至少一个数字");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errors.add("密码必须包含至少一个特殊字符");
        }

        // 3. 常见弱密码检查
        if (isCommonPassword(password)) {
            errors.add("密码太常见，请使用更复杂的密码");
        }

        // 4. 连续字符检查
        if (hasSequentialChars(password)) {
            errors.add("密码不能包含连续的相同字符（如 aaa, 111）");
        }

        if (!errors.isEmpty()) {
            throw new DEException(ResultCode.PARAM_IS_INVALID.code(),
                    "密码不符合安全策略: " + String.join("; ", errors));
        }
    }

    /**
     * 检查是否为常见弱密码
     */
    private static boolean isCommonPassword(String password) {
        List<String> commonPasswords = List.of(
                "12345678", "123456789", "1234567890",
                "password", "password1", "password123",
                "admin123", "admin1234", "admin12345",
                "qwerty123", "qwertyuiop",
                "abc123456", "abc12345678",
                "iloveyou", "sunshine", "princess",
                "football", "charlie", "shadow",
                "master", "dragon", "login"
        );
        return commonPasswords.contains(password.toLowerCase());
    }

    /**
     * 检查是否包含连续相同字符（如 aaa, 111）
     */
    private static boolean hasSequentialChars(String password) {
        if (password.length() < 3) {
            return false;
        }

        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            if (c1 == c2 && c2 == c3) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取密码策略描述
     *
     * @return 密码策略描述
     */
    public static String getPolicyDescription() {
        return String.format(
                "密码必须满足以下要求:\n" +
                "- 长度 %d-%d 个字符\n" +
                "- 包含至少一个大写字母\n" +
                "- 包含至少一个小写字母\n" +
                "- 包含至少一个数字\n" +
                "- 包含至少一个特殊字符\n" +
                "- 不能是常见弱密码\n" +
                "- 不能包含连续的相同字符",
                MIN_LENGTH, MAX_LENGTH
        );
    }
}
