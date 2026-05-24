package io.crest.api.ds.vo;

import lombok.Data;

@Data
public class ExcelDataPageRequest {
    private Long datasourceId;
    private String tableName;
    private Integer page = 1;
    private Integer pageSize = 100;
}
