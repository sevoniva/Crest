package io.crest.log;

import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.substitute.permissions.user.CrestUserManage;
import io.crest.substitute.permissions.user.model.CrestUser;
import io.crest.utils.AuthUtils;
import io.crest.utils.CommonBeanFactory;
import io.crest.utils.LogUtil;
import io.crest.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Audit fallback for REST endpoints that have not been annotated with DeLog.
 */
@Aspect
@Component
public class GenericAuditLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Around("within(@org.springframework.web.bind.annotation.RestController *)"
            + " && execution(public * io.crest..*(..))"
            + " && !@annotation(io.crest.log.DeLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (hasDeLog(point)) {
            return point.proceed();
        }
        HttpServletRequest request = ServletUtils.request();
        if (!shouldAudit(request, point.getSignature().getName())) {
            return point.proceed();
        }

        long startTime = System.currentTimeMillis();
        String requestMethod = request.getMethod();
        String requestUrl = request.getRequestURI();
        LogOT operationType = operationType(requestMethod, requestUrl, point.getSignature().getName());
        String resourceType = resourceType(requestUrl).name();
        Operator operator = currentOperator();
        int responseCode = 200;
        String responseMsg = "success";

        try {
            return point.proceed();
        } catch (Throwable e) {
            responseCode = 500;
            responseMsg = e.getMessage();
            throw e;
        } finally {
            auditLogService.log(
                    operationType, resourceType, null, description(operationType, resourceType, requestUrl),
                    requestMethod, requestUrl,
                    operator.id(), operator.name(), operator.account(), clientIp(request),
                    System.currentTimeMillis() - startTime, responseCode, responseMsg
            );
        }
    }

    private boolean shouldAudit(HttpServletRequest request, String methodName) {
        if (request == null || request.getMethod() == null || request.getRequestURI() == null) {
            return false;
        }
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        String uri = request.getRequestURI().toLowerCase(Locale.ROOT);
        if (uri.contains("/auditlog/")) {
            return false;
        }
        if (hasDedicatedAudit(uri)) {
            return false;
        }
        if (!"GET".equals(method)) {
            return true;
        }
        String normalizedName = methodName == null ? "" : methodName.toLowerCase(Locale.ROOT);
        if (containsAny(uri, "delete", "batchdel", "remove", "clear", "retry", "enable", "disable")
                || containsAny(normalizedName, "delete", "remove", "clear", "retry", "enable", "disable")) {
            return true;
        }
        return uri.contains("export") || uri.contains("download") || uri.contains("/sso/login")
                || uri.contains("/sso/callback") || uri.contains("/sso/token/");
    }

    private boolean hasDedicatedAudit(String uri) {
        return uri.contains("/sso/config")
                || uri.contains("/sso/validate")
                || uri.contains("/datasettree/exportdataset")
                || uri.contains("/chartdata/innerexport");
    }

    private boolean hasDeLog(ProceedingJoinPoint point) {
        if (!(point.getSignature() instanceof MethodSignature signature)) {
            return false;
        }
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(DeLog.class)) {
            return true;
        }
        try {
            Method targetMethod = point.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
            return targetMethod.isAnnotationPresent(DeLog.class);
        } catch (Exception ignored) {
            return false;
        }
    }

    private LogOT operationType(String method, String uri, String methodName) {
        String normalizedUri = uri == null ? "" : uri.toLowerCase(Locale.ROOT);
        String normalizedName = methodName == null ? "" : methodName.toLowerCase(Locale.ROOT);
        if (normalizedUri.contains("download") || normalizedName.contains("download")) {
            return LogOT.DOWNLOAD;
        }
        if (normalizedUri.contains("export") || normalizedName.contains("export")) {
            return LogOT.EXPORT;
        }
        if (normalizedUri.contains("/sso/login") || normalizedUri.contains("/sso/callback")
                || normalizedUri.contains("/sso/token/")) {
            return LogOT.LOGIN;
        }
        if ("DELETE".equalsIgnoreCase(method) || containsAny(normalizedUri, "delete", "batchdel", "remove")) {
            return LogOT.DELETE;
        }
        if (containsAny(normalizedUri, "authorize", "permission", "savebusiper", "savemenuper")
                || containsAny(normalizedName, "permission", "authorize")) {
            return LogOT.AUTHORIZE;
        }
        if (containsAny(normalizedUri, "upload", "import") || containsAny(normalizedName, "upload", "import")) {
            return LogOT.UPLOADFILE;
        }
        if (containsAny(normalizedUri, "create", "add") || containsAny(normalizedName, "create", "add")) {
            return LogOT.CREATE;
        }
        if (containsAny(normalizedUri, "clear")) {
            return LogOT.CLEAR;
        }
        return LogOT.MODIFY;
    }

    private LogST resourceType(String uri) {
        String value = uri == null ? "" : uri.toLowerCase(Locale.ROOT);
        if (value.contains("/user/")) return LogST.USER;
        if (value.contains("/role/")) return LogST.ROLE;
        if (value.contains("/org/")) return LogST.ORG;
        if (value.contains("/auth/")) return LogST.MENU;
        if (value.contains("/datasource/")) return LogST.DATASOURCE;
        if (value.contains("/dataset/")) return LogST.DATASET;
        if (value.contains("/datavisualization/")) return value.contains("datav") ? LogST.SCREEN : LogST.PANEL;
        if (value.contains("/chart/")) return LogST.VIEW;
        if (value.contains("/font/")) return LogST.DATA;
        if (value.contains("/exportcenter/")) return LogST.DATA;
        if (value.contains("/sso/") || value.contains("/sysparameter/")) return LogST.DATA;
        return LogST.DATA;
    }

    private String description(LogOT operationType, String resourceType, String requestUrl) {
        return actionDesc(operationType) + resourceDesc(resourceType) + "：" + requestUrl;
    }

    private Operator currentOperator() {
        Long operatorId = AuthUtils.getUser() != null ? AuthUtils.getUser().getUserId() : null;
        if (operatorId == null) {
            return new Operator(null, null, null);
        }
        try {
            CrestUser user = CommonBeanFactory.getBean(CrestUserManage.class).queryById(operatorId);
            if (user != null) {
                return new Operator(operatorId, user.getName(), user.getAccount());
            }
        } catch (Exception e) {
            LogUtil.debug("获取审计操作人失败: " + e.getMessage());
        }
        return new Operator(operatorId, null, null);
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            return index > 0 ? ip.substring(0, index).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip == null ? "unknown" : ip;
    }

    private boolean containsAny(String value, String... parts) {
        for (String part : parts) {
            if (value.contains(part)) {
                return true;
            }
        }
        return false;
    }

    private String resourceDesc(String resourceType) {
        return switch (resourceType) {
            case "USER" -> "用户";
            case "ROLE" -> "角色";
            case "ORG" -> "组织";
            case "MENU" -> "权限";
            case "DATASOURCE" -> "数据源";
            case "DATASET" -> "数据集";
            case "PANEL" -> "仪表盘";
            case "SCREEN" -> "数据大屏";
            case "VIEW" -> "图表";
            default -> "数据";
        };
    }

    private String actionDesc(LogOT operationType) {
        return switch (operationType) {
            case CREATE -> "新建";
            case MODIFY -> "编辑";
            case DELETE -> "删除";
            case EXPORT -> "导出";
            case DOWNLOAD -> "下载";
            case AUTHORIZE -> "授权";
            case UPLOADFILE -> "上传";
            case LOGIN -> "登录";
            case CLEAR -> "清理";
            default -> operationType.name();
        };
    }

    private record Operator(Long id, String name, String account) {
    }
}
