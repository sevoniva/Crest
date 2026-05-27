package io.crest.relation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "血缘资源选项")
public class RelationResourceDTO {
    @Schema(description = "资源 ID")
    private String id;
    @Schema(description = "资源名称")
    private String name;
    @Schema(description = "资源类型")
    private String type;
    @Schema(description = "资源子类型")
    private String subType;
    @Schema(description = "更新时间")
    private Long updateTime;
}
