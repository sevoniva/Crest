package io.crest.api.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "单点登录配置")
@Data
public class SsoConfigRequest {

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "身份提供方名称")
    private String providerName;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "客户端密钥")
    private String clientSecret;

    @Schema(description = "授权端点")
    private String authorizationEndpoint;

    @Schema(description = "令牌端点")
    private String tokenEndpoint;

    @Schema(description = "用户信息端点")
    private String userInfoEndpoint;

    @Schema(description = "Issuer")
    private String issuer;

    @Schema(description = "授权范围")
    private String scope;

    @Schema(description = "回调地址，留空时按当前访问域名自动生成")
    private String redirectUri;

    @Schema(description = "用户唯一标识字段")
    private String userIdAttribute;

    @Schema(description = "账号字段")
    private String accountAttribute;

    @Schema(description = "姓名字段")
    private String nameAttribute;

    @Schema(description = "邮箱字段")
    private String emailAttribute;

    @Schema(description = "是否自动创建用户")
    private Boolean autoCreateUser;

    @Schema(description = "是否允许本地账号登录")
    private Boolean allowLocalLogin;

    @Schema(description = "是否要求HTTPS端点")
    private Boolean requireHttps;

    @Schema(description = "退出后跳转地址")
    private String logoutRedirectUrl;
}
