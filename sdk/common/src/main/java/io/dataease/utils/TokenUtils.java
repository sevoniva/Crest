package io.dataease.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.auth.config.SubstituleLoginConfig;
import io.dataease.exception.DEException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class TokenUtils {


    public static TokenUserBO userBOByToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(Md5Utils.md5(SubstituleLoginConfig.getPwd()));
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


    public static TokenUserBO validateLinkToken(String linkToken) {
        DEException.throwException("link token must be verified by TokenFilter");
        return null;
    }
}
