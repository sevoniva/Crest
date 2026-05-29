package io.crest.commons.utils;

import io.crest.utils.BeanUtils;
import io.crest.utils.ConfigUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class EncryptUtils extends CodingUtil {

    private static final String AES_KEY_PROPERTY = "crest.crypto.aes-key";
    private static final String AES_IV_PROPERTY = "crest.crypto.aes-iv";

    private static String configuredSecretKey() {
        String value = ConfigUtils.getConfig(AES_KEY_PROPERTY, null);
        if (!isValidAesValue(value)) {
            throw new IllegalStateException(AES_KEY_PROPERTY + " must be 16, 24, or 32 characters");
        }
        return value;
    }

    private static String iv() {
        String value = ConfigUtils.getConfig(AES_IV_PROPERTY, null);
        if (StringUtils.length(value) != 16) {
            throw new IllegalStateException(AES_IV_PROPERTY + " must be 16 characters");
        }
        return value;
    }

    private static boolean isValidAesValue(String value) {
        int length = StringUtils.length(value);
        return length == 16 || length == 24 || length == 32;
    }


    public static Object aesEncrypt(Object o) {
        if (o == null) {
            return null;
        }
        return aesEncrypt(o.toString(), configuredSecretKey(), iv());
    }

    public static Object aesDecrypt(Object o) {
        if (o == null) {
            return null;
        }
        return aesDecrypt(o.toString(), configuredSecretKey(), iv());
    }

    public static <T> Object aesDecrypt(List<T> o, String attrName) {
        if (o == null) {
            return null;
        }
        return o.stream()
                .filter(element -> BeanUtils.getFieldValueByName(attrName, element) != null)
                .peek(element -> BeanUtils.setFieldValueByName(element, attrName, aesDecrypt(BeanUtils.getFieldValueByName(attrName, element).toString(), configuredSecretKey(), iv()), String.class))
                .collect(Collectors.toList());
    }

    public static Object md5Encrypt(Object o) {
        if (o == null) {
            return null;
        }
        return md5(o.toString());
    }
}
