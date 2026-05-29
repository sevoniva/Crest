package io.crest.api.system.vo;

import io.crest.api.system.request.SsoConfigRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "单点登录配置展示")
@Data
@EqualsAndHashCode(callSuper = true)
public class SsoConfigVO extends SsoConfigRequest {

    @Schema(description = "当前部署回调地址")
    private String callbackUrl;

    @Schema(description = "是否已经保存客户端密钥")
    private Boolean secretConfigured;
}
