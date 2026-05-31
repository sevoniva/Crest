package io.crest.utils;

import io.crest.constant.AuthConstant;
import io.crest.exception.DEException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.core.env.Environment;

import java.util.List;

import static io.crest.result.ResultCode.INTERFACE_ADDRESS_INVALID;

public class WhitelistUtils {

    private static String contextPath;


    public static String getContextPath() {
        if (StringUtils.isBlank(contextPath)) {
            Environment environment = CommonBeanFactory.getBean(Environment.class);
            contextPath = environment == null ? "" : environment.getProperty("server.servlet.context-path", String.class);
        }
        return contextPath;
    }

    public static List<String> WHITE_PATH = List.of(
            "/login/localLogin",
            "/apisix/check",
            "/actuator/health",
            "/dekey",
            "/index.html",
            "/model",
            "/swagger-resources",
            "/swagger-ui.html",
            "/doc.html",
            "/panel.html",
            "/mobile.html",
            "/lark/qrinfo",
            "/lark/token",
            "/larksuite/qrinfo",
            "/larksuite/token",
            "/dingtalk/qrinfo",
            "/dingtalk/token",
            "/wecom/qrinfo",
            "/wecom/token",
            "/sysParameter/requestTimeOut",
            "/sysParameter/defaultSettings",
            "/setting/authentication/status",
            "/sysParameter/ui",
            "/sysParameter/defaultLogin",
            "/sso/public/status",
            "/sso/login",
            "/sso/callback",
            "/embedded/initIframe",
            "/sysParameter/i18nOptions",
            "/login/modifyInvalidPwd",
            "/perSetting/hmac/info",
            "/");

    public static boolean match(String requestURI) {
        invalidUrl(requestURI);
        if (Strings.CS.startsWith(requestURI, getContextPath())) {
            requestURI = requestURI.replaceFirst(getContextPath(), "");
        }
        if (Strings.CS.startsWith(requestURI, AuthConstant.DE_API_PREFIX)) {
            requestURI = requestURI.replaceFirst(AuthConstant.DE_API_PREFIX, "");
        }
        if (Strings.CS.startsWith(requestURI, AuthConstant.DE_CASAPI_PREFIX)) {
            requestURI = requestURI.replaceFirst(AuthConstant.DE_CASAPI_PREFIX, "");
        }
        if (Strings.CS.startsWith(requestURI, AuthConstant.DE_OIDCAPI_PREFIX)) {
            requestURI = requestURI.replaceFirst(AuthConstant.DE_OIDCAPI_PREFIX, "");
        }
        return WHITE_PATH.contains(requestURI)
                || Strings.CS.endsWithAny(requestURI, ".gif", ".ico", ".js", ".css", ".svg", ".png", ".jpg", ".jpeg", ".js.map", ".otf", ".ttf", ".woff2")
                || Strings.CS.startsWithAny(requestURI, "data:image")
                || Strings.CS.startsWithAny(requestURI, "/login/platformLogin/")
                // 移除API文档白名单，需要认证才能访问
                // || Strings.CS.startsWithAny(requestURI, "/v3/api-docs")
                // || Strings.CS.startsWithAny(requestURI, "/swagger-ui")
                || Strings.CS.startsWithAny(requestURI, "/webjars/")
                || Strings.CS.startsWithAny(requestURI, "/static-resource/")
                || Strings.CS.startsWithAny(requestURI, "/appearance/image/")
                || Strings.CS.startsWithAny(requestURI, "/share/proxyInfo")
                || Strings.CS.startsWithAny(requestURI, "/websocket")
                || Strings.CS.startsWithAny(requestURI, "/oauth2/")
                || Strings.CS.startsWithAny(requestURI, "/sso/token/")
                || Strings.CS.startsWithAny(requestURI, "/mfa/qr/")
                || Strings.CS.startsWithAny(requestURI, "/mfa/login")
                || Strings.CS.startsWithAny(requestURI, "/typeface/download")
                || Strings.CS.startsWithAny(requestURI, "/typeface/defaultFont")
                || Strings.CS.startsWithAny(requestURI, "/typeface/listFont")
                || Strings.CS.startsWithAny(requestURI, "/exportCenter/download")
                || Strings.CS.startsWithAny(requestURI, "/i18n/")
                || Strings.CS.startsWithAny(requestURI, "/communicate/image/")
                || Strings.CS.startsWithAny(requestURI, "/saml/")
                || Strings.CS.startsWithAny(requestURI, "/communicate/down/");
    }

    public static String getBaseApiUrl(String redirect_uri) {
        if (Strings.CS.endsWith(redirect_uri, "/")) {
            redirect_uri = redirect_uri.substring(0, redirect_uri.length() - 1);
        }
        String contextPath = WhitelistUtils.getContextPath();
        if (StringUtils.isNotBlank(contextPath)) {
            redirect_uri += contextPath;
        }
        return redirect_uri + AuthConstant.DE_API_PREFIX + "/";
    }

    private static void invalidUrl(String requestURI) {
        if (requestURI.contains("./") || requestURI.contains(".%") || requestURI.toLowerCase().contains("%2e") || (requestURI.contains(";") && !requestURI.contains("?"))) {
            DEException.throwException(INTERFACE_ADDRESS_INVALID.code(), String.format("%s [%s]", INTERFACE_ADDRESS_INVALID.message(), requestURI));
        }
    }
}
