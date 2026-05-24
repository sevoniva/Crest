package io.crest.relation.dto;

import lombok.Data;

@Data
public class RelationSummaryDTO {
    private Integer datasourceCount = 0;
    private Integer tableCount = 0;
    private Integer tableFieldCount = 0;
    private Integer datasetFieldCount = 0;
    private Integer datasetCount = 0;
    private Integer chartFieldCount = 0;
    private Integer chartCount = 0;
    private Integer dvCount = 0;
    private Integer edgeCount = 0;
    private Integer totalCount = 0;
}
