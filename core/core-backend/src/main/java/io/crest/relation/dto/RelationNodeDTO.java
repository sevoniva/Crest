package io.crest.relation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "血缘节点")
public class RelationNodeDTO {
    @Schema(description = "节点 ID")
    private String id;
    @Schema(description = "业务资源 ID")
    private String resourceId;
    @Schema(description = "节点名称")
    private String name;
    @Schema(description = "节点类型")
    private String type;
    @Schema(description = "节点子类型")
    private String subType;
    @Schema(description = "节点说明")
    private String description;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "更新时间")
    private Long updateTime;
    @Schema(description = "布局层级")
    private Integer level;
}
