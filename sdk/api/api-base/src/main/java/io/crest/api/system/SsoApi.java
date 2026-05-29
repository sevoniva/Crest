package io.crest.api.system;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.crest.api.system.request.SsoConfigRequest;
import io.crest.api.system.vo.SsoConfigVO;
import io.crest.api.system.vo.SsoStatusVO;
import io.crest.auth.vo.TokenVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "系统设置:单点登录")
@ApiSupport(order = 798)
public interface SsoApi {

    @GetMapping("/public/status")
    @Operation(summary = "查询单点登录公开状态")
    SsoStatusVO status();

    @GetMapping("/config")
    @Operation(summary = "查询单点登录配置")
    SsoConfigVO config(HttpServletRequest request);

    @PostMapping("/config")
    @Operation(summary = "保存单点登录配置")
    void save(@RequestBody SsoConfigRequest request);

    @PostMapping("/validate")
    @Operation(summary = "校验单点登录配置")
    void validate(@RequestBody SsoConfigRequest request);

    @GetMapping("/login")
    @Operation(summary = "发起单点登录")
    void login(@RequestParam(value = "redirect", required = false) String redirect,
               HttpServletRequest request,
               HttpServletResponse response);

    @GetMapping("/callback")
    @Operation(summary = "单点登录回调")
    void callback(@RequestParam(value = "code", required = false) String code,
                  @RequestParam(value = "state", required = false) String state,
                  @RequestParam(value = "error", required = false) String error,
                  @RequestParam(value = "error_description", required = false) String errorDescription,
                  HttpServletRequest request,
                  HttpServletResponse response);

    @GetMapping("/token/{ticket}")
    @Operation(summary = "用一次性票据换取登录令牌")
    TokenVO token(@PathVariable("ticket") String ticket);
}
