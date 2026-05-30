package io.crest.log;

import io.crest.constant.LogOT;
import io.crest.constant.LogST;
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
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

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
        LogST sourceType = deLog.st();
        String resourceType = sourceType.name();

        // 获取操作类型
        LogOT operationType = deLog.ot();

        Object result = null;
        int responseCode = 200;
        String responseMsg = "success";

        try {
            result = point.proceed();
            return result;
        } catch (Exception e) {
            responseCode = 500;
            responseMsg = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 获取资源名称（从方法名推断）
            String resourceName = getOperationDescription(operationType, resourceType, point);

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
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable("p" + i, args[i]);
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

    /**
     * 获取操作描述
     */
    private String getOperationDescription(LogOT operationType, String resourceType, ProceedingJoinPoint point) {
        String methodName = point.getSignature().getName();
        String typeName = operationType.name();

        // 根据操作类型和资源类型生成描述
        String resourceDesc = getResourceDesc(resourceType);
        String actionDesc = getActionDesc(typeName);

        return actionDesc + resourceDesc;
    }

    private String getResourceDesc(String resourceType) {
        switch (resourceType) {
            case "USER": return "用户";
            case "DATASOURCE": return "数据源";
            case "DATASET": return "数据集";
            case "PANEL": return "仪表板";
            case "SCREEN": return "数据大屏";
            case "VIEW": return "图表";
            case "ROLE": return "角色";
            case "ORG": return "组织";
            default: return resourceType;
        }
    }

    private String getActionDesc(String operationType) {
        switch (operationType) {
            case "CREATE": return "创建";
            case "MODIFY": return "修改";
            case "DELETE": return "删除";
            case "READ": return "查看";
            case "LOGIN": return "登录";
            case "EXPORT": return "导出";
            case "DOWNLOAD": return "下载";
            default: return operationType;
        }
    }
}
