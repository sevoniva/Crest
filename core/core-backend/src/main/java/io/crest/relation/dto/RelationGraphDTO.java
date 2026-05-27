package io.crest.relation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "血缘关系图")
public class RelationGraphDTO {
    @Schema(description = "节点列表")
    private List<RelationNodeDTO> nodes = new ArrayList<>();
    @Schema(description = "关系列表")
    private List<RelationEdgeDTO> edges = new ArrayList<>();
    @Schema(description = "统计信息")
    private RelationSummaryDTO summary = new RelationSummaryDTO();
}
