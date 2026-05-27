package io.crest.relation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "血缘统计")
public class RelationSummaryDTO {
    @Schema(description = "数据源数量")
    private Integer datasourceCount = 0;
    @Schema(description = "物理表数量")
    private Integer tableCount = 0;
    @Schema(description = "物理字段数量")
    private Integer tableFieldCount = 0;
    @Schema(description = "数据集字段数量")
    private Integer datasetFieldCount = 0;
    @Schema(description = "数据集数量")
    private Integer datasetCount = 0;
    @Schema(description = "图表字段数量")
    private Integer chartFieldCount = 0;
    @Schema(description = "图表数量")
    private Integer chartCount = 0;
    @Schema(description = "仪表板和大屏数量")
    private Integer dvCount = 0;
    @Schema(description = "关系数量")
    private Integer edgeCount = 0;
    @Schema(description = "节点总数")
    private Integer totalCount = 0;
}
