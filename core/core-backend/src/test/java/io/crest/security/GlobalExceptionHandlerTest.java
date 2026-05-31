package io.crest.security;

import io.crest.exception.GlobalExceptionHandler;
import io.crest.result.ResultMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("不支持的 HTTP 方法应按 405 处理")
    void shouldHandleUnsupportedMethodAs405() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("GET", List.of("POST"));

        ResultMessage result = handler.methodNotSupportedExceptionHandler(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), result.getCode());
        assertEquals("请求方法不支持", result.getMsg());

        Method method = GlobalExceptionHandler.class.getMethod(
                "methodNotSupportedExceptionHandler", HttpRequestMethodNotSupportedException.class);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, method.getAnnotation(ResponseStatus.class).value());
    }
}
