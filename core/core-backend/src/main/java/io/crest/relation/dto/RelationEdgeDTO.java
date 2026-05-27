package io.crest.relation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "血缘关系边")
public class RelationEdgeDTO {
    @Schema(description = "起点节点 ID")
    private String source;
    @Schema(description = "终点节点 ID")
    private String target;
    @Schema(description = "关系类型")
    private String type;
    @Schema(description = "关系名称")
    private String label;
}
