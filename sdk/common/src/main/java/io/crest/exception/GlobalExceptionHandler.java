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
        LogUtil.info(message);
        if (Strings.CS.contains(message, "Cannot invoke \"io.crest.auth.bo.TokenUserBO.getUserId()\" because \"user\" is null")) {
            return new ResultMessage(ResultCode.USER_NOT_LOGGED_IN.code(), ResultCode.USER_NOT_LOGGED_IN.message());
        }
        return new ResultMessage(ResultCode.PARAM_IS_BLANK.code(), message);
    }

    @ExceptionHandler(Exception.class)
    public ResultMessage exceptionHandler(Exception e) {
        String message = StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName());
        LogUtil.error(message, e);
        return new ResultMessage(ResultCode.SYSTEM_INNER_ERROR.code(), message);
    }

}
