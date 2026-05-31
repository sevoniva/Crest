package io.crest.security;

import io.crest.auth.bo.TokenUserBO;
import io.crest.exception.DEException;
import io.crest.log.AuditLogController;
import io.crest.result.ResultCode;
import io.crest.utils.AuthUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
