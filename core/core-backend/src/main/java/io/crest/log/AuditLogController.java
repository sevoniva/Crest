package io.crest.log;

import io.crest.auth.DePermit;
import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.exception.DEException;
import io.crest.result.ResultCode;
import io.crest.utils.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计日志控制器
 *
 * 提供审计日志查询接口
 */
@Tag(name = "审计日志")
@RestController
@RequestMapping("/auditLog")
public class AuditLogController {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int MAX_PAGE_SIZE = 200;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DeLog(ot = LogOT.READ, st = LogST.DATA)
    @Operation(summary = "查询审计日志")
    @DePermit("m:read")
    @PostMapping("/pager/{goPage}/{pageSize}")
    public Map<String, Object> pager(
            @PathVariable("goPage") int goPage,
            @PathVariable("pageSize") int pageSize,
            @RequestBody(required = false) Map<String, Object> request) {

        requireSystemAdmin();
        int safePage = Math.max(goPage, DEFAULT_PAGE);
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        int offset = (safePage - 1) * safePageSize;

        // 构建查询条件
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();

        if (request != null) {
            String operationType = (String) request.get("operationType");
            if (operationType != null && !operationType.isEmpty()) {
                where.append(" AND operation_type = ?");
                params.add(operationType);
            }

            String resourceType = (String) request.get("resourceType");
            if (resourceType != null && !resourceType.isEmpty()) {
                where.append(" AND resource_type = ?");
                params.add(resourceType);
            }

            String operatorAccount = (String) request.get("operatorAccount");
            if (operatorAccount != null && !operatorAccount.isEmpty()) {
                where.append(" AND operator_account LIKE ?");
                params.add("%" + operatorAccount + "%");
            }

            String startTime = (String) request.get("startTime");
            if (startTime != null && !startTime.isEmpty()) {
                where.append(" AND operation_time >= ?");
                params.add(startTime);
            }

            String endTime = (String) request.get("endTime");
            if (endTime != null && !endTime.isEmpty()) {
                where.append(" AND operation_time <= ?");
                params.add(endTime);
            }
        }

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM core_audit_log" + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        // 查询数据
        String dataSql = "SELECT * FROM core_audit_log" + where + " ORDER BY operation_time DESC LIMIT ? OFFSET ?";
        params.add(safePageSize);
        params.add(offset);
        List<Map<String, Object>> records = jdbcTemplate.queryForList(dataSql, params.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total != null ? total : 0);
        result.put("records", records);
        result.put("current", safePage);
        result.put("size", safePageSize);

        return result;
    }

    @Operation(summary = "审计日志统计")
    @DePermit("m:read")
    @GetMapping("/statistics")
    public Map<String, Object> statistics() {
        requireSystemAdmin();
        Map<String, Object> stats = new HashMap<>();

        // 今日操作数
        Long todayCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM core_audit_log WHERE DATE(operation_time) = CURDATE()",
                Long.class);
        stats.put("todayCount", todayCount != null ? todayCount : 0);

        // 今日登录数
        Long todayLogin = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM core_audit_log WHERE operation_type = 'LOGIN' AND DATE(operation_time) = CURDATE()",
                Long.class);
        stats.put("todayLogin", todayLogin != null ? todayLogin : 0);

        // 操作类型统计
        List<Map<String, Object>> typeStats = jdbcTemplate.queryForList(
                "SELECT operation_type, COUNT(*) as count FROM core_audit_log WHERE DATE(operation_time) = CURDATE() GROUP BY operation_type");
        stats.put("typeStats", typeStats);

        return stats;
    }

    @Operation(summary = "获取操作类型列表")
    @DePermit("m:read")
    @GetMapping("/operationTypes")
    public List<Map<String, Object>> operationTypes() {
        requireSystemAdmin();
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT operation_type as value, operation_type as label FROM core_audit_log ORDER BY operation_type");
    }

    private void requireSystemAdmin() {
        if (!AuthUtils.isSysAdmin()) {
            DEException.throwException(ResultCode.PERMISSION_NO_ACCESS.code(), "仅系统管理员可访问审计日志");
        }
    }
}
