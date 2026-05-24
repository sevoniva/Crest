package io.crest.share.interceptor;

import io.crest.auth.DeLinkPermit;
import io.crest.constant.AuthConstant;
import io.crest.exception.DEException;
import io.crest.utils.ServletUtils;
import io.crest.utils.WhitelistUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;


@Component
public class LinkInterceptor implements HandlerInterceptor {

    private final static String whiteListText = "/user/ipInfo, /datasetData/enumValue, /datasetData/enumValueObj, /datasetData/getFieldTree, /dekey, /symmetricKey, /share/validate";

    private final static String whiteStartListText = "/dataVisualization/findDvType/";

    private boolean isWhiteStart(String url) {
        List<String> whiteStartList = Arrays.stream(StringUtils.split(whiteStartListText, ",")).map(String::trim).toList();
        return whiteStartList.stream().anyMatch(item -> Strings.CS.startsWith(url, item));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String linkToken = ServletUtils.getHead(AuthConstant.LINK_TOKEN_KEY);
        if (linkToken == null) {
            return true;
        }
        if (handler instanceof HandlerMethod handlerMethod) {
            DeLinkPermit deLinkPermit = handlerMethod.getMethodAnnotation(DeLinkPermit.class);
            if (deLinkPermit == null) {

                List<String> whiteList = Arrays.stream(StringUtils.split(whiteListText, ",")).map(String::trim).toList();

                String requestURI = ServletUtils.request().getRequestURI();
                if (Strings.CS.startsWith(requestURI, WhitelistUtils.getContextPath())) {
                    requestURI = requestURI.replaceFirst(WhitelistUtils.getContextPath(), "");
                }
                if (Strings.CS.startsWith(requestURI, AuthConstant.DE_API_PREFIX)) {
                    requestURI = requestURI.replaceFirst(AuthConstant.DE_API_PREFIX, "");
                }
                boolean valid = whiteList.contains(requestURI) || isWhiteStart(requestURI) || WhitelistUtils.match(requestURI);
                if (!valid) {
                    DEException.throwException("分享链接Token不支持访问当前url[" + requestURI + "]");
                }
                return true;
            }
        }
        return true;
    }


}
