package io.crest.api.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "单点登录公开状态")
@Data
public class SsoStatusVO {

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "身份提供方名称")
    private String providerName;

    @Schema(description = "登录按钮文案")
    private String loginButtonText;

    @Schema(description = "是否允许本地账号登录")
    private Boolean allowLocalLogin;
}
