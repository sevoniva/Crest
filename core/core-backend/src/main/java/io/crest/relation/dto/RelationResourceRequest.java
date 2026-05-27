package io.crest.relation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "血缘资源查询条件")
public class RelationResourceRequest {
    @Schema(description = "资源名称关键字")
    private String keyword;
}
