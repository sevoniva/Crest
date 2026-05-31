package io.crest.security;

import io.crest.constant.LogOT;
import io.crest.log.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditLogServiceTest {

    @Test
    @DisplayName("审计日志字段入库前应按表结构截断")
    void shouldTruncateLongAuditFieldsBeforeInsert() throws Exception {
        AuditLogService service = new AuditLogService();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        injectJdbcTemplate(service, jdbcTemplate);
        String longText = "x".repeat(1000);

        service.log(LogOT.READ, "DATA", longText, longText, longText, longText,
                1L, longText, longText, longText, 12L, 500, longText);

        ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(anyString(), args.capture());
        Object[] values = args.getValue();
        assertTrue(Arrays.stream(values).noneMatch(value -> value instanceof String text && text.length() > 500));
    }

    private void injectJdbcTemplate(AuditLogService service, JdbcTemplate jdbcTemplate) throws Exception {
        Field field = AuditLogService.class.getDeclaredField("jdbcTemplate");
        field.setAccessible(true);
        field.set(service, jdbcTemplate);
    }
}
