package io.crest.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码编码器
 *
 * 使用 PBKDF2 算法进行密码哈希，替代不安全的 MD5。
 *
 * 修复漏洞：SAST-03（MD5 密码哈希）
 *
 * @author security-fix
 */
public class PasswordEncoder {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 编码密码
     *
     * @param rawPassword 原始密码
     * @return 编码后的密码（格式：iterations:base64(salt):base64(hash)）
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("密码不能为 null");
        }

        // 生成随机盐
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        // 计算哈希
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        // 返回格式：iterations:base64(salt):base64(hash)
        return ITERATIONS + ":" +
                Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 编码后的密码
     * @return 如果密码匹配返回 true
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        try {
            // 解析编码后的密码
            String[] parts = encodedPassword.split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

            // 计算实际哈希
            byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt, iterations, expectedHash.length * 8);

            // 比较哈希（常量时间比较，防止时序攻击）
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断密码是否需要重新编码（升级）
     *
     * @param encodedPassword 编码后的密码
     * @return 如果需要重新编码返回 true
     */
    public static boolean needsReEncoding(String encodedPassword) {
        if (encodedPassword == null) {
            return true;
        }

        // 如果是 MD5 格式（32 位十六进制），需要升级
        if (encodedPassword.matches("^[0-9a-f]{32}$")) {
            return true;
        }

        try {
            String[] parts = encodedPassword.split(":");
            if (parts.length != 3) {
                return true;
            }

            int iterations = Integer.parseInt(parts[0]);
            return iterations < ITERATIONS;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * PBKDF2 密钥派生
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持的算法: " + ALGORITHM, e);
        } catch (Exception e) {
            throw new RuntimeException("密码编码失败", e);
        }
    }

    /**
     * 常量时间比较，防止时序攻击
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
