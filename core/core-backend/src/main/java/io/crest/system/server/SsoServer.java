package io.crest.system.server;

import io.crest.api.system.SsoApi;
import io.crest.api.system.request.SsoConfigRequest;
import io.crest.api.system.vo.SsoConfigVO;
import io.crest.api.system.vo.SsoStatusVO;
import io.crest.auth.vo.TokenVO;
import io.crest.system.manage.SsoManage;
import io.crest.utils.CrestPermissionUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sso")
public class SsoServer implements SsoApi {

    @Resource
    private SsoManage ssoManage;

    @Override
    public SsoStatusVO status() {
        return ssoManage.status();
    }

    @Override
    public SsoConfigVO config(HttpServletRequest request) {
        CrestPermissionUtils.requireAdmin();
        return ssoManage.config(request);
    }

    @Override
    public void save(SsoConfigRequest request) {
        CrestPermissionUtils.requireAdmin();
        ssoManage.save(request);
    }

    @Override
    public void validate(SsoConfigRequest request) {
        CrestPermissionUtils.requireAdmin();
        ssoManage.validate(request);
    }

    @Override
    public void login(String redirect, HttpServletRequest request, HttpServletResponse response) {
        ssoManage.login(redirect, request, response);
    }

    @Override
    public void callback(String code, String state, String error, String errorDescription,
                         HttpServletRequest request, HttpServletResponse response) {
        ssoManage.callback(code, state, error, errorDescription, request, response);
    }

    @Override
    public TokenVO token(String ticket) {
        return ssoManage.token(ticket);
    }
}
