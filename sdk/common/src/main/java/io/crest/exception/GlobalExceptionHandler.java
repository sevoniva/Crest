package io.crest.exception;


import io.crest.i18n.Translator;
import io.crest.result.ResultCode;
import io.crest.result.ResultMessage;
import io.crest.utils.LogUtil;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@SuppressWarnings("deprecation")
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultMessage MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        String msg = objectError.getDefaultMessage();
        msg = Translator.get(msg);
        LogUtil.info(msg);
        return new ResultMessage(ResultCode.PARAM_IS_INVALID.code(), msg);
    }

    @ExceptionHandler(DEException.class)
    public ResultMessage deExceptionHandler(DEException e) {
        LogUtil.info(e.getMessage());
        return new ResultMessage(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ClientAbortException.class)
    public void clientAbortExceptionHandler(ClientAbortException e) {
        LogUtil.debug(StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultMessage noResourceFoundExceptionHandler(NoResourceFoundException e) {
        String message = StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName());
        LogUtil.debug(message);
        return new ResultMessage(ResultCode.RESOURCE_NOT_FOUND.code(), message);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResultMessage noUserExceptionHandler(Exception e) {
        String message = e.getMessage();
        LogUtil.info("NullPointerException: " + message);
        if (Strings.CS.contains(message, "Cannot invoke \"io.crest.auth.bo.TokenUserBO.getUserId()\" because \"user\" is null")) {
            return new ResultMessage(ResultCode.USER_NOT_LOGGED_IN.code(), ResultCode.USER_NOT_LOGGED_IN.message());
        }
        // 不泄露内部错误信息
        return new ResultMessage(ResultCode.PARAM_IS_BLANK.code(), "参数错误");
    }

    @ExceptionHandler(Exception.class)
    public ResultMessage exceptionHandler(Exception e) {
        // 记录完整错误日志
        LogUtil.error("系统内部错误: " + e.getMessage(), e);
        // 返回通用错误消息，不泄露内部信息
        return new ResultMessage(ResultCode.SYSTEM_INNER_ERROR.code(), "系统内部错误，请联系管理员");
    }

}
