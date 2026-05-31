package io.crest.security;

import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.log.AuditLogService;
import io.crest.log.DeLog;
import io.crest.log.DeLogAspect;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeLogAspectTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ProceedingJoinPoint point;

    @Mock
    private MethodSignature signature;

    @Mock
    private DeLog deLog;

    @Mock
    private HttpServletRequest request;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("stExp 应按业务类型记录数据大屏审计资源类型")
    void shouldResolveResourceTypeFromStExp() throws Throwable {
        DeLogAspect aspect = new DeLogAspect();
        injectAuditLogService(aspect);
        VisualizationRequest visualizationRequest = new VisualizationRequest(9L, "dataV");

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/dataVisualization/save");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(signature.getMethod()).thenReturn(TestEndpoint.class.getDeclaredMethod("save", VisualizationRequest.class));
        when(signature.getName()).thenReturn("save");
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[]{visualizationRequest});
        when(point.proceed()).thenReturn("ok");

        when(deLog.id()).thenReturn("#p0.id");
        when(deLog.stExp()).thenReturn("#p0.type");
        when(deLog.ot()).thenReturn(LogOT.MODIFY);

        Object result = aspect.around(point, deLog);

        assertEquals("ok", result);
        ArgumentCaptor<String> resourceType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> resourceId = ArgumentCaptor.forClass(String.class);
        verify(auditLogService).log(eq(LogOT.MODIFY), resourceType.capture(), resourceId.capture(),
                anyString(), eq("POST"), eq("/dataVisualization/save"),
                isNull(), isNull(), isNull(), eq("127.0.0.1"),
                anyLong(), eq(200), eq("success"));
        assertEquals("SCREEN", resourceType.getValue());
        assertEquals("9", resourceId.getValue());
    }

    private void injectAuditLogService(DeLogAspect aspect) throws ReflectiveOperationException {
        Field field = DeLogAspect.class.getDeclaredField("auditLogService");
        field.setAccessible(true);
        field.set(aspect, auditLogService);
    }

    private static class TestEndpoint {
        @SuppressWarnings("unused")
        public Object save(VisualizationRequest request) {
            return null;
        }
    }

    private record VisualizationRequest(Long id, String type) {
    }
}
