package io.crest.api.ds.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ExcelDataSaveRequest {
    private Long datasourceId;
    private String tableName;
    private List<Map<String, Object>> updates = new ArrayList<>();
    private List<Map<String, Object>> inserts = new ArrayList<>();
    private List<String> deletes = new ArrayList<>();
}
