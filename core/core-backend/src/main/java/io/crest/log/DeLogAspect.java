package io.crest.log;

import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.utils.AuthUtils;
import io.crest.utils.CommonBeanFactory;
import io.crest.utils.LogUtil;
import io.crest.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * DeLog 注解 AOP 切面
 *
 * 拦截 @DeLog 注解的方法，自动记录审计日志
 */
@Aspect
@Component
public class DeLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(deLog)")
    public Object around(ProceedingJoinPoint point, DeLog deLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        HttpServletRequest request = ServletUtils.request();
        String requestMethod = request != null ? request.getMethod() : "UNKNOWN";
        String requestUrl = request != null ? request.getRequestURI() : "UNKNOWN";
        String clientIp = getClientIp(request);

        // 获取操作人信息
        Long operatorId = AuthUtils.getUser() != null ? AuthUtils.getUser().getUserId() : null;
        String operatorName = null;
        String operatorAccount = null;
        if (operatorId != null) {
            try {
                // 从数据库查询用户信息
                io.crest.substitute.permissions.user.model.CrestUser user =
                    CommonBeanFactory.getBean(io.crest.substitute.permissions.user.CrestUserManage.class).queryById(operatorId);
                if (user != null) {
                    operatorName = user.getName();
                    operatorAccount = user.getAccount();
                }
            } catch (Exception e) {
                LogUtil.debug("获取用户信息失败: " + e.getMessage());
            }
        }

        // 解析资源ID
        String resourceId = null;
        try {
            resourceId = parseExpression(deLog.id(), point);
        } catch (Exception e) {
            LogUtil.debug("解析资源ID失败: " + e.getMessage());
        }

        // 获取资源类型
        LogST sourceType = resolveResourceType(deLog, point);
        String resourceType = sourceType.name();

        // 获取操作类型
        LogOT operationType = deLog.ot();

        Object result = null;
        int responseCode = 200;
        String responseMsg = "success";

        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            responseCode = 500;
            responseMsg = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            HttpServletResponse response = ServletUtils.response();
            if (response != null && response.getStatus() >= 400) {
                responseCode = response.getStatus();
                responseMsg = responseMsg == null || "success".equals(responseMsg) ? "failed" : responseMsg;
            }

            // 获取资源名称（从方法名推断）
            String resourceName = AuditLogText.description(operationType, resourceType, requestUrl);

            // 异步记录审计日志
            auditLogService.log(
                    operationType, resourceType, resourceId, resourceName,
                    requestMethod, requestUrl,
                    operatorId, operatorName, operatorAccount, clientIp,
                    duration, responseCode, responseMsg
            );
        }
    }

    /**
     * 解析 SpEL 表达式
     */
    private String parseExpression(String expression, ProceedingJoinPoint point) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }

        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = discoverer.getParameterNames(method);
            Object[] args = point.getArgs();

            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < args.length; i++) {
                context.setVariable("p" + i, args[i]);
            }
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            Object value = parser.parseExpression(expression).getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            LogUtil.debug("SpEL表达式解析失败: " + expression + ", " + e.getMessage());
            return null;
        }
    }

    private LogST resolveResourceType(DeLog deLog, ProceedingJoinPoint point) {
        String stExpression = deLog.stExp();
        if (stExpression != null && !stExpression.isBlank()) {
            LogST resolved = toLogST(parseExpression(stExpression, point));
            if (resolved != null) {
                return resolved;
            }
        }
        return deLog.st();
    }

    private LogST toLogST(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        switch (normalized.toLowerCase(Locale.ROOT)) {
            case "datav":
            case "screen":
                return LogST.SCREEN;
            case "dashboard":
            case "panel":
            case "panelmobile":
                return LogST.PANEL;
            case "dataset":
                return LogST.DATASET;
            case "datasource":
                return LogST.DATASOURCE;
            default:
                break;
        }
        try {
            return LogST.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";

        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP，取第一个
            int index = ip.indexOf(',');
            if (index > 0) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getRemoteAddr();
        // 将 IPv6 本地地址转换为 IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

}
