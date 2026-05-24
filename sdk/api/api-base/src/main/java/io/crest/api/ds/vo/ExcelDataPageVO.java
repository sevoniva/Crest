package io.crest.api.ds.vo;

import io.crest.extensions.datasource.dto.TableField;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExcelDataPageVO {
    private List<TableField> fields;
    private List<Map<String, Object>> rows;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
