package io.crest.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.crest.auth.bo.TokenUserBO;
import io.crest.auth.config.SubstituleLoginConfig;
import io.crest.exception.DEException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class TokenUtils {


    public static TokenUserBO userBOByToken(String token) {
        String secret = userSecret(token);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(token);
        Long userId = jwt.getClaim("uid").asLong();
        Long oid = jwt.getClaim("oid").asLong();
        if (ObjectUtils.isEmpty(userId)) {
            DEException.throwException("token格式错误！");
        }
        return new TokenUserBO(userId, oid);
    }

    public static TokenUserBO validate(String token) {
        if (StringUtils.isBlank(token)) {
            String uri = ServletUtils.request().getRequestURI();
            DEException.throwException("token is empty for uri {" + uri + "}");
        }
        if (StringUtils.length(token) < 100) {
            DEException.throwException("token is invalid");
        }
        return userBOByToken(token);
    }

    private static String userSecret(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            Long uid = jwt.getClaim("uid").asLong();
            Object userManage = CommonBeanFactory.getBean("crestUserManage");
            Method method = DeReflectUtil.findMethod(userManage.getClass(), "secretByUid");
            Object secret = ReflectionUtils.invokeMethod(method, userManage, uid);
            if (secret != null && StringUtils.isNotBlank(secret.toString())) {
                return secret.toString();
            }
        } catch (Exception ignored) {
        }
        return Md5Utils.md5(SubstituleLoginConfig.getPwd());
    }


    public static TokenUserBO validateLinkToken(String linkToken) {
        DEException.throwException("link token must be verified by TokenFilter");
        return null;
    }
}
