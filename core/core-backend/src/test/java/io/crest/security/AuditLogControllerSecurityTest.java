package io.crest.security;

import io.crest.auth.bo.TokenUserBO;
import io.crest.exception.DEException;
import io.crest.log.AuditLogController;
import io.crest.result.ResultCode;
import io.crest.utils.AuthUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogControllerSecurityTest {

    @AfterEach
    void tearDown() {
        AuthUtils.remove();
    }

    @Test
    @DisplayName("审计日志接口应仅允许系统管理员访问")
    void auditLogApisShouldRequireSystemAdmin() {
        AuthUtils.setUser(new TokenUserBO(2L, 1L));
        AuditLogController controller = new AuditLogController();

        DEException pagerError = assertThrows(DEException.class,
                () -> controller.pager(1, 15, Map.of()));
        DEException statisticsError = assertThrows(DEException.class, controller::statistics);
        DEException operationTypesError = assertThrows(DEException.class, controller::operationTypes);

        assertEquals(ResultCode.PERMISSION_NO_ACCESS.code().intValue(), pagerError.getCode());
        assertEquals(ResultCode.PERMISSION_NO_ACCESS.code().intValue(), statisticsError.getCode());
        assertEquals(ResultCode.PERMISSION_NO_ACCESS.code().intValue(), operationTypesError.getCode());
    }

    @Test
    @DisplayName("审计日志分页参数应做边界保护")
    void auditLogPagerShouldClampPageArguments() throws Exception {
        AuthUtils.setUser(new TokenUserBO(1L, 1L));
        AuditLogController controller = new AuditLogController();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        injectJdbcTemplate(controller, jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(0L);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(java.util.List.of());

        Map<String, Object> result = controller.pager(-10, 1000, Map.of());

        assertEquals(1, result.get("current"));
        assertEquals(200, result.get("size"));
        verify(jdbcTemplate).queryForList(anyString(), aryEq(new Object[]{200, 0}));
    }

    private void injectJdbcTemplate(AuditLogController controller, JdbcTemplate jdbcTemplate) throws Exception {
        Field field = AuditLogController.class.getDeclaredField("jdbcTemplate");
        field.setAccessible(true);
        field.set(controller, jdbcTemplate);
    }
}
