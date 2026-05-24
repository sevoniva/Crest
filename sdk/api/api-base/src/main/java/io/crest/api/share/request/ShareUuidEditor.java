package io.crest.api.share.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "分享UUID编辑器")
@Data
public class ShareUuidEditor implements Serializable {

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long resourceId;
    @Schema(description = "分享UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uuid;
}
