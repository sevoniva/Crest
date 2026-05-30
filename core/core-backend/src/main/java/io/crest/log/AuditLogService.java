package io.crest.log;

import io.crest.constant.LogOT;
import io.crest.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务
 *
 * 异步记录操作日志到数据库
 */
@Service
public class AuditLogService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 异步记录审计日志
     */
    @Async("auditLogExecutor")
    public void log(LogOT operationType, String resourceType, String resourceId,
                    String resourceName, String requestMethod, String requestUrl,
                    Long operatorId, String operatorName, String operatorAccount,
                    String operatorIp, long duration, int responseCode, String responseMsg) {
        try {
            // 敏感数据脱敏
            if (resourceName != null) {
                resourceName = maskSensitive(resourceName);
            }

            // 处理operatorId为null的情况
            if (operatorId == null) {
                operatorId = 0L;
            }

            String sql = """
                INSERT INTO core_audit_log
                (operation_type, resource_type, resource_id, resource_name,
                 request_method, request_url,
                 operator_id, operator_name, operator_account, operator_ip,
                 duration, response_code, response_msg, operation_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;

            jdbcTemplate.update(sql,
                    operationType.name(), resourceType, resourceId, resourceName,
                    requestMethod, requestUrl,
                    operatorId, operatorName, operatorAccount, operatorIp,
                    duration, responseCode, responseMsg);

        } catch (Exception e) {
            // 审计日志记录失败不应影响业务
            LogUtil.error("审计日志记录失败: " + e.getMessage());
        }
    }

    /**
     * 记录登录日志
     */
    @Async("auditLogExecutor")
    public void logLogin(String account, Long userId, String ip, boolean success, String msg) {
        try {
            String sql = """
                INSERT INTO core_audit_log
                (operation_type, resource_type, resource_id, resource_name,
                 operator_id, operator_name, operator_account, operator_ip,
                 response_code, response_msg, operation_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;

            jdbcTemplate.update(sql,
                    "LOGIN", "USER", userId != null ? userId.toString() : null, account,
                    userId != null ? userId : 0L, account, account, ip,
                    success ? 200 : 401, msg);

        } catch (Exception e) {
            LogUtil.error("登录日志记录失败: " + e.getMessage());
        }
    }

    /**
     * 敏感数据脱敏
     */
    private String maskSensitive(String data) {
        if (data == null) return null;
        // 密码脱敏
        data = data.replaceAll("(?i)password[\"']?\\s*[:=]\\s*[\"']?[^\"',\\s}]*", "password\":\"***\"");
        data = data.replaceAll("(?i)pwd[\"']?\\s*[:=]\\s*[\"']?[^\"',\\s}]*", "pwd\":\"***\"");
        // Token脱敏
        data = data.replaceAll("(?i)token[\"']?\\s*[:=]\\s*[\"']?[^\"',\\s}]{20,}", "token\":\"***\"");
        return data;
    }
}
