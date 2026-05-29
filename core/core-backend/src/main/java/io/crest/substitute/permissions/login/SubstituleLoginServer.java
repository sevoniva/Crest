package io.crest.substitute.permissions.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import io.crest.api.permissions.login.dto.PwdLoginDTO;
import io.crest.auth.bo.TokenUserBO;
import io.crest.auth.vo.TokenVO;
import io.crest.exception.DEException;
import io.crest.substitute.permissions.user.CrestUserManage;
import io.crest.substitute.permissions.user.model.CrestUser;
import io.crest.system.manage.SsoManage;
import io.crest.utils.LogUtil;
import io.crest.utils.RsaUtils;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@ConditionalOnMissingBean(name = "loginServer")
@RestController
@RequestMapping
public class SubstituleLoginServer {

    @Resource
    private CrestUserManage crestUserManage;

    @Resource
    private SsoManage ssoManage;

    @PostMapping("/login/localLogin")
    public TokenVO localLogin(@RequestBody PwdLoginDTO dto) {

        String name = dto.getName();
        name = RsaUtils.decryptStr(name);
        String pwd = dto.getPwd();
        pwd = RsaUtils.decryptStr(pwd);

        dto.setName(name);
        dto.setPwd(pwd);

        CrestUser user = crestUserManage.queryByAccount(name);
        if (user == null || !crestUserManage.passwordMatches(user, pwd)) {
            DEException.throwException("用户名或密码错误");
        }
        if (Boolean.FALSE.equals(user.getEnable())) {
            DEException.throwException("用户已停用");
        }
        boolean emergencyAdmin = Boolean.TRUE.equals(dto.getEmergency()) && Boolean.TRUE.equals(user.getAdmin());
        if (CrestUserManage.AUTH_TYPE_SSO.equalsIgnoreCase(user.getAuthType()) && !emergencyAdmin) {
            DEException.throwException("单点登录用户请使用企业账号登录");
        }
        if (!ssoManage.localLoginAllowed() && !emergencyAdmin) {
            DEException.throwException("当前已启用单点登录，请使用企业账号登录");
        }
        TokenUserBO tokenUserBO = new TokenUserBO();
        tokenUserBO.setUserId(user.getId());
        tokenUserBO.setDefaultOid(1L);
        crestUserManage.markLoginSuccess(user.getId());
        return generate(tokenUserBO, user.getPasswordHash());
    }


    @GetMapping("/logout")
    public void logout() {
        LogUtil.info("substitule logout");
    }

    private TokenVO generate(TokenUserBO bo, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Long userId = bo.getUserId();
        Long defaultOid = bo.getDefaultOid();
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("uid", userId).withClaim("oid", defaultOid);
        String token = builder.sign(algorithm);
        return new TokenVO(token, 0L);
    }
}
