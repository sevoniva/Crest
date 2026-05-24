package io.crest.relation.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.crest.api.permissions.relation.api.RelationApi;
import io.crest.chart.dao.auto.entity.CoreChartView;
import io.crest.chart.dao.auto.mapper.CoreChartViewMapper;
import io.crest.dataset.dao.auto.entity.CoreDatasetGroup;
import io.crest.dataset.dao.auto.entity.CoreDatasetTable;
import io.crest.dataset.dao.auto.entity.CoreDatasetTableField;
import io.crest.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.crest.dataset.dao.auto.mapper.CoreDatasetTableMapper;
import io.crest.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.crest.datasource.dao.auto.entity.CoreDatasource;
import io.crest.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.crest.exception.DEException;
import io.crest.extensions.view.dto.ChartViewFieldDTO;
import io.crest.relation.dto.RelationEdgeDTO;
import io.crest.relation.dto.RelationGraphDTO;
import io.crest.relation.dto.RelationNodeDTO;
import io.crest.relation.dto.RelationResourceDTO;
import io.crest.relation.dto.RelationResourceRequest;
import io.crest.relation.dto.RelationSummaryDTO;
import io.crest.utils.CrestPermissionUtils;
import io.crest.utils.JsonUtil;
import io.crest.visualization.dao.auto.entity.DataVisualizationInfo;
import io.crest.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class RelationManage implements RelationApi {

    private static final String TYPE_DATASOURCE = "datasource";
    private static final String TYPE_TABLE = "table";
    private static final String TYPE_TABLE_FIELD = "table_field";
    private static final String TYPE_DATASET_FIELD = "dataset_field";
    private static final String TYPE_CHART_FIELD = "chart_field";
    private static final String TYPE_DATASET = "dataset";
    private static final String TYPE_CHART = "chart";
    private static final String TYPE_DV = "dv";
    private static final Pattern FIELD_REF_PATTERN = Pattern.compile("\\[(\\d+)]");
    private static final Pattern JSON_FIELD_ID_PATTERN = Pattern.compile("\"(?:id|fieldId)\"\\s*:\\s*\"?(\\d+)\"?");
    private static final TypeReference<List<ChartViewFieldDTO>> CHART_FIELD_LIST_TYPE = new TypeReference<>() {
    };

    @Resource
    private CoreDatasourceMapper coreDatasourceMapper;
    @Resource
    private CoreDatasetTableMapper coreDatasetTableMapper;
    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;
    @Resource
    private CoreDatasetGroupMapper coreDatasetGroupMapper;
    @Resource
    private CoreChartViewMapper coreChartViewMapper;
    @Resource
    private DataVisualizationInfoMapper dataVisualizationInfoMapper;

    public RelationGraphDTO overview() {
        RelationContext context = loadContext();
        GraphBuilder builder = new GraphBuilder();
        context.datasources.values().forEach(datasource -> addDatasourceNode(builder, datasource));
        context.tablesByDataset.values().stream().flatMap(List::stream).forEach(table -> addPhysicalTableNode(builder, table));
        context.datasets.values().forEach(dataset -> addDatasetNode(builder, dataset));
        context.charts.values().forEach(chart -> addChartNode(builder, chart));
        context.visualizations.values().forEach(dv -> addDvNode(builder, dv));
        addAllEdges(builder, context, null, null, null, false);
        return builder.build();
    }

    public RelationGraphDTO datasource(Long id) {
        RelationContext context = loadContext();
        GraphBuilder builder = new GraphBuilder();
        CoreDatasource datasource = context.datasources.get(id);
        addDatasourceNode(builder, datasource);
        addAllEdges(builder, context, id, null, null, true);
        return builder.build();
    }

    public RelationGraphDTO dataset(Long id) {
        RelationContext context = loadContext();
        GraphBuilder builder = new GraphBuilder();
        CoreDatasetGroup dataset = context.datasets.get(id);
        addDatasetNode(builder, dataset);
        addAllEdges(builder, context, null, id, null, true);
        return builder.build();
    }

    public RelationGraphDTO dv(Long id) {
        RelationContext context = loadContext();
        GraphBuilder builder = new GraphBuilder();
        DataVisualizationInfo dv = context.visualizations.get(id);
        addDvNode(builder, dv);
        addAllEdges(builder, context, null, null, id, true);
        return builder.build();
    }

    public List<RelationResourceDTO> resources(String type, RelationResourceRequest request) {
        RelationContext context = loadContext();
        String keyword = request == null ? null : StringUtils.trimToNull(request.getKeyword());
        List<RelationResourceDTO> result = new ArrayList<>();
        if (StringUtils.isBlank(type) || "all".equals(type) || TYPE_DATASOURCE.equals(type)) {
            context.datasources.values().forEach(item -> result.add(toResource(item)));
        }
        if (StringUtils.isBlank(type) || "all".equals(type) || TYPE_DATASET.equals(type)) {
            context.datasets.values().forEach(item -> result.add(toResource(item)));
        }
        if (StringUtils.isBlank(type) || "all".equals(type) || TYPE_DV.equals(type)) {
            context.visualizations.values().forEach(item -> result.add(toResource(item)));
        }
        return result.stream()
                .filter(item -> StringUtils.isBlank(keyword) || Strings.CI.contains(item.getName(), keyword))
                .sorted(Comparator.comparing(RelationResourceDTO::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(RelationResourceDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(200)
                .toList();
    }

    @Override
    public Long getDsResource(Long id) {
        RelationGraphDTO graph = datasource(id);
        return graph.getNodes().stream()
                .filter(node -> !Objects.equals(node.getType(), TYPE_DATASOURCE))
                .count();
    }

    @Override
    public Long getDatasetResource(Long id) {
        RelationGraphDTO graph = dataset(id);
        return graph.getNodes().stream()
                .filter(node -> TYPE_CHART.equals(node.getType()) || TYPE_DV.equals(node.getType()))
                .count();
    }

    @Override
    public void checkAuth() throws DEException {
        // Community build uses the same local resource metadata and does not require extra auth.
    }

    private void addAllEdges(GraphBuilder builder, RelationContext context, Long datasourceId, Long datasetId, Long dvId, boolean includeFields) {
        Set<Long> datasetScope = new HashSet<>();
        Set<Long> chartScope = new HashSet<>();

        if (datasourceId != null) {
            List<CoreDatasetTable> tables = context.tablesByDatasource.getOrDefault(datasourceId, List.of());
            for (CoreDatasetTable table : tables) {
                if (table.getDatasetGroupId() != null) {
                    datasetScope.add(table.getDatasetGroupId());
                }
            }
        } else if (datasetId != null) {
            datasetScope.add(datasetId);
        } else if (dvId != null) {
            List<CoreChartView> charts = context.chartsByDv.getOrDefault(dvId, List.of());
            for (CoreChartView chart : charts) {
                chartScope.add(chart.getId());
                if (chart.getTableId() != null) {
                    datasetScope.add(chart.getTableId());
                }
            }
        } else {
            datasetScope.addAll(context.datasets.keySet());
            chartScope.addAll(context.charts.keySet());
        }

        for (Long currentDatasetId : datasetScope) {
            CoreDatasetGroup dataset = context.datasets.get(currentDatasetId);
            addDatasetNode(builder, dataset);
            for (CoreDatasetTable table : context.tablesByDataset.getOrDefault(currentDatasetId, List.of())) {
                CoreDatasource datasource = context.datasources.get(table.getDatasourceId());
                addDatasourceNode(builder, datasource);
                addPhysicalTableNode(builder, table);
                if (datasource != null && dataset != null) {
                    builder.addEdge(nodeId(TYPE_DATASOURCE, datasource.getId()), tableNodeId(table), "datasource_table", "包含数据表");
                    builder.addEdge(tableNodeId(table), nodeId(TYPE_DATASET, dataset.getId()), "table_dataset", safeTableLabel(table));
                }
                if (includeFields) {
                    for (CoreDatasetTableField field : context.fieldsByDatasetTable.getOrDefault(table.getId(), List.of())) {
                        if (Objects.equals(field.getDatasetGroupId(), currentDatasetId)) {
                            addDatasetFieldLineage(builder, context, field, new HashSet<>());
                        }
                    }
                }
            }
            addDatasetJoinLineage(builder, context, dataset, includeFields);

            for (CoreChartView chart : context.chartsByDataset.getOrDefault(currentDatasetId, List.of())) {
                if (dvId == null || Objects.equals(chart.getSceneId(), dvId)) {
                    chartScope.add(chart.getId());
                }
            }
        }

        for (Long chartId : chartScope) {
            CoreChartView chart = context.charts.get(chartId);
            addChartNode(builder, chart);
            if (chart == null) {
                continue;
            }
            CoreDatasetGroup dataset = context.datasets.get(chart.getTableId());
            addDatasetNode(builder, dataset);
            if (dataset != null) {
                builder.addEdge(nodeId(TYPE_DATASET, dataset.getId()), nodeId(TYPE_CHART, chart.getId()), "dataset_chart", "使用数据集");
            }
            if (includeFields) {
                addChartFieldLineage(builder, context, chart);
            }
            DataVisualizationInfo dv = context.visualizations.get(chart.getSceneId());
            addDvNode(builder, dv);
            if (dv != null) {
                builder.addEdge(nodeId(TYPE_CHART, chart.getId()), nodeId(TYPE_DV, dv.getId()), "chart_dv", "归属资源");
            }
        }
    }

    private RelationContext loadContext() {
        RelationContext context = new RelationContext();
        context.datasources = coreDatasourceMapper.selectList(null).stream()
                .filter(item -> item.getId() != null)
                .filter(item -> !"folder".equalsIgnoreCase(item.getType()))
                .filter(item -> CrestPermissionUtils.canAccessCreator(item.getCreateBy()))
                .collect(Collectors.toMap(CoreDatasource::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        context.datasets = coreDatasetGroupMapper.selectList(new QueryWrapper<CoreDatasetGroup>().eq("node_type", TYPE_DATASET)).stream()
                .filter(item -> item.getId() != null)
                .filter(item -> CrestPermissionUtils.canAccessCreator(item.getCreateBy()))
                .collect(Collectors.toMap(CoreDatasetGroup::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        List<CoreDatasetTable> tables = coreDatasetTableMapper.selectList(null).stream()
                .filter(item -> item.getDatasetGroupId() != null)
                .filter(item -> context.datasets.containsKey(item.getDatasetGroupId()))
                .filter(item -> item.getDatasourceId() == null || context.datasources.containsKey(item.getDatasourceId()))
                .toList();
        context.tables = tables.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(CoreDatasetTable::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        context.tablesByDataset = tables.stream().collect(Collectors.groupingBy(CoreDatasetTable::getDatasetGroupId, LinkedHashMap::new, Collectors.toList()));
        context.tablesByDatasource = tables.stream()
                .filter(item -> item.getDatasourceId() != null)
                .collect(Collectors.groupingBy(CoreDatasetTable::getDatasourceId, LinkedHashMap::new, Collectors.toList()));
        List<CoreDatasetTableField> fields = coreDatasetTableFieldMapper.selectList(null).stream()
                .filter(item -> item.getId() != null)
                .filter(item -> item.getDatasetGroupId() != null)
                .filter(item -> context.datasets.containsKey(item.getDatasetGroupId()))
                .filter(item -> item.getChecked() == null || item.getChecked())
                .toList();
        context.fields = fields.stream()
                .collect(Collectors.toMap(CoreDatasetTableField::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        context.fieldsByDataset = fields.stream()
                .collect(Collectors.groupingBy(CoreDatasetTableField::getDatasetGroupId, LinkedHashMap::new, Collectors.toList()));
        context.fieldsByDatasetTable = fields.stream()
                .filter(item -> item.getDatasetTableId() != null)
                .collect(Collectors.groupingBy(CoreDatasetTableField::getDatasetTableId, LinkedHashMap::new, Collectors.toList()));
        context.visualizations = dataVisualizationInfoMapper.selectList(null).stream()
                .filter(item -> item.getId() != null)
                .filter(item -> item.getDeleteFlag() == null || !item.getDeleteFlag())
                .filter(item -> !"folder".equalsIgnoreCase(item.getNodeType()))
                .filter(item -> CrestPermissionUtils.canAccessCreator(item.getCreateBy()))
                .collect(Collectors.toMap(DataVisualizationInfo::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        context.charts = coreChartViewMapper.selectList(null).stream()
                .filter(item -> item.getId() != null)
                .filter(item -> item.getTableId() != null)
                .filter(item -> CrestPermissionUtils.canAccessCreator(item.getCreateBy())
                        || context.datasets.containsKey(item.getTableId())
                        || context.visualizations.containsKey(item.getSceneId()))
                .collect(Collectors.toMap(CoreChartView::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        context.chartsByDataset = context.charts.values().stream()
                .filter(item -> item.getTableId() != null)
                .collect(Collectors.groupingBy(CoreChartView::getTableId, LinkedHashMap::new, Collectors.toList()));
        context.chartsByDv = context.charts.values().stream()
                .filter(item -> item.getSceneId() != null)
                .collect(Collectors.groupingBy(CoreChartView::getSceneId, LinkedHashMap::new, Collectors.toList()));
        return context;
    }

    private void addDatasourceNode(GraphBuilder builder, CoreDatasource datasource) {
        if (datasource == null) {
            return;
        }
        builder.addNode(nodeId(TYPE_DATASOURCE, datasource.getId()), datasource.getId(), datasource.getName(), TYPE_DATASOURCE, datasource.getType(), datasource.getDescription(), datasource.getCreateTime(), datasource.getUpdateTime(), 0);
    }

    private void addPhysicalTableNode(GraphBuilder builder, CoreDatasetTable table) {
        if (table == null) {
            return;
        }
        String label = safeTableLabel(table);
        Long datasourceId = table.getDatasourceId();
        String resourceId = datasourceId == null ? String.valueOf(table.getId()) : datasourceId + ":" + label;
        builder.addNode(tableNodeId(table), resourceId, label, TYPE_TABLE, table.getType(), null, null, null, 1);
    }

    private void addDatasetFieldLineage(GraphBuilder builder, RelationContext context, CoreDatasetTableField field, Set<Long> visiting) {
        if (field == null || field.getId() == null) {
            return;
        }
        if (!visiting.add(field.getId())) {
            return;
        }
        addDatasetFieldNode(builder, context, field);

        CoreDatasetGroup dataset = context.datasets.get(field.getDatasetGroupId());
        addDatasetNode(builder, dataset);
        if (dataset != null) {
            builder.addEdge(datasetFieldNodeId(field.getId()), nodeId(TYPE_DATASET, dataset.getId()), "dataset_field_dataset", "组成数据集");
        }

        CoreDatasetTable table = context.tables.get(field.getDatasetTableId());
        if (table != null) {
            addPhysicalTableNode(builder, table);
            addTableFieldNode(builder, table, field);
            builder.addEdge(tableNodeId(table), tableFieldNodeId(table, field), "table_table_field", "包含字段");
            builder.addEdge(tableFieldNodeId(table, field), datasetFieldNodeId(field.getId()), "table_field_dataset_field", "字段映射");
        }

        for (Long sourceFieldId : extractFieldRefs(field.getOriginName(), field.getParams())) {
            if (Objects.equals(sourceFieldId, field.getId())) {
                continue;
            }
            CoreDatasetTableField source = context.fields.get(sourceFieldId);
            if (source == null) {
                continue;
            }
            addDatasetFieldLineage(builder, context, source, visiting);
            builder.addEdge(datasetFieldNodeId(source.getId()), datasetFieldNodeId(field.getId()), "dataset_field_calc_field", "计算字段");
        }
        visiting.remove(field.getId());
    }

    private void addChartFieldLineage(GraphBuilder builder, RelationContext context, CoreChartView chart) {
        if (chart == null || chart.getId() == null) {
            return;
        }
        for (ChartFieldUsage usage : collectChartFieldUsages(chart)) {
            if (usage.fieldId() == null || usage.fieldId() <= 0) {
                continue;
            }
            CoreDatasetTableField datasetField = context.fields.get(usage.fieldId());
            if (datasetField != null) {
                addDatasetFieldLineage(builder, context, datasetField, new HashSet<>());
            } else {
                addDatasetFieldNode(builder, usage, chart.getTableId());
            }
            addChartFieldNode(builder, chart, usage, datasetField);
            builder.addEdge(datasetFieldNodeId(usage.fieldId()), chartFieldNodeId(chart, usage), "dataset_field_chart_field", usage.role());
            builder.addEdge(chartFieldNodeId(chart, usage), nodeId(TYPE_CHART, chart.getId()), "chart_field_chart", "图表字段");
        }
    }

    private void addTableFieldNode(GraphBuilder builder, CoreDatasetTable table, CoreDatasetTableField field) {
        if (field == null) {
            return;
        }
        builder.addNode(tableFieldNodeId(table, field), tableFieldResourceId(table, field), safeOriginName(field), TYPE_TABLE_FIELD, field.getType(), field.getDescription(), null, field.getLastSyncTime(), 2);
    }

    private void addDatasetFieldNode(GraphBuilder builder, RelationContext context, CoreDatasetTableField field) {
        if (field == null || field.getId() == null) {
            return;
        }
        builder.addNode(datasetFieldNodeId(field.getId()), field.getId(), safeDatasetFieldLabel(field), TYPE_DATASET_FIELD, fieldSubType(field), datasetFieldDescription(field, context), null, field.getLastSyncTime(), 3);
    }

    private void addDatasetFieldNode(GraphBuilder builder, ChartFieldUsage usage, Long datasetId) {
        if (usage.fieldId() == null) {
            return;
        }
        String resourceId = datasetId == null ? String.valueOf(usage.fieldId()) : datasetId + ":" + usage.fieldId();
        builder.addNode(datasetFieldNodeId(usage.fieldId()), resourceId, StringUtils.defaultIfBlank(usage.name(), "字段 " + usage.fieldId()), TYPE_DATASET_FIELD, usage.groupType(), null, null, null, 3);
    }

    private void addChartFieldNode(GraphBuilder builder, CoreChartView chart, ChartFieldUsage usage, CoreDatasetTableField datasetField) {
        if (chart == null || usage.fieldId() == null) {
            return;
        }
        String fieldName = StringUtils.defaultIfBlank(usage.name(), datasetField == null ? null : safeDatasetFieldLabel(datasetField));
        builder.addNode(chartFieldNodeId(chart, usage), chart.getId() + ":" + usage.fieldId(), fieldName, TYPE_CHART_FIELD, usage.role(), chartFieldDescription(usage), chart.getCreateTime(), chart.getUpdateTime(), 5);
    }

    private void addDatasetNode(GraphBuilder builder, CoreDatasetGroup dataset) {
        if (dataset == null) {
            return;
        }
        builder.addNode(nodeId(TYPE_DATASET, dataset.getId()), dataset.getId(), dataset.getName(), TYPE_DATASET, dataset.getType(), null, dataset.getCreateTime(), dataset.getLastUpdateTime(), 4);
    }

    private void addChartNode(GraphBuilder builder, CoreChartView chart) {
        if (chart == null) {
            return;
        }
        builder.addNode(nodeId(TYPE_CHART, chart.getId()), chart.getId(), chart.getTitle(), TYPE_CHART, chart.getType(), null, chart.getCreateTime(), chart.getUpdateTime(), 6);
    }

    private void addDvNode(GraphBuilder builder, DataVisualizationInfo dv) {
        if (dv == null) {
            return;
        }
        builder.addNode(nodeId(TYPE_DV, dv.getId()), dv.getId(), dv.getName(), TYPE_DV, dv.getType(), dv.getRemark(), dv.getCreateTime(), dv.getUpdateTime(), 7);
    }

    private RelationResourceDTO toResource(CoreDatasource datasource) {
        RelationResourceDTO dto = new RelationResourceDTO();
        dto.setId(String.valueOf(datasource.getId()));
        dto.setName(datasource.getName());
        dto.setType(TYPE_DATASOURCE);
        dto.setSubType(datasource.getType());
        dto.setUpdateTime(datasource.getUpdateTime());
        return dto;
    }

    private RelationResourceDTO toResource(CoreDatasetGroup dataset) {
        RelationResourceDTO dto = new RelationResourceDTO();
        dto.setId(String.valueOf(dataset.getId()));
        dto.setName(dataset.getName());
        dto.setType(TYPE_DATASET);
        dto.setSubType(dataset.getType());
        dto.setUpdateTime(dataset.getLastUpdateTime());
        return dto;
    }

    private RelationResourceDTO toResource(DataVisualizationInfo dv) {
        RelationResourceDTO dto = new RelationResourceDTO();
        dto.setId(String.valueOf(dv.getId()));
        dto.setName(dv.getName());
        dto.setType(TYPE_DV);
        dto.setSubType(dv.getType());
        dto.setUpdateTime(dv.getUpdateTime());
        return dto;
    }

    private String safeTableLabel(CoreDatasetTable table) {
        if (StringUtils.isNotBlank(table.getName())) {
            return table.getName();
        }
        if (StringUtils.isNotBlank(table.getTableName())) {
            return table.getTableName();
        }
        return "数据表";
    }

    private String safeOriginName(CoreDatasetTableField field) {
        return StringUtils.defaultIfBlank(field.getOriginName(), StringUtils.defaultIfBlank(field.getName(), "字段"));
    }

    private String safeDatasetFieldLabel(CoreDatasetTableField field) {
        String originName = safeOriginName(field);
        String displayName = StringUtils.trimToNull(field.getName());
        if (Objects.equals(field.getExtField(), 2) && StringUtils.isNotBlank(displayName)) {
            return displayName;
        }
        if (StringUtils.isBlank(displayName) || Strings.CS.equals(displayName, originName)) {
            return originName;
        }
        return originName + " (" + displayName + ")";
    }

    private String fieldSubType(CoreDatasetTableField field) {
        String groupType = Strings.CI.equals(field.getGroupType(), "q") ? "指标" : "维度";
        return StringUtils.isBlank(field.getType()) ? groupType : groupType + " / " + field.getType();
    }

    private String datasetFieldDescription(CoreDatasetTableField field, RelationContext context) {
        if (field == null) {
            return null;
        }
        if (Objects.equals(field.getExtField(), 2) && StringUtils.isNotBlank(field.getOriginName())) {
            String formula = "公式：" + formatFieldFormula(field.getOriginName(), context);
            return StringUtils.isBlank(field.getDescription()) ? formula : field.getDescription() + "\n" + formula;
        }
        if (StringUtils.isNotBlank(field.getDescription())) {
            return field.getDescription();
        }
        return null;
    }

    private String chartFieldDescription(ChartFieldUsage usage) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.isNotBlank(usage.role())) {
            parts.add("用途：" + usage.role());
        }
        if (StringUtils.isNotBlank(usage.summary())) {
            parts.add("聚合：" + summaryLabel(usage.summary()));
        }
        return String.join("；", parts);
    }

    private String formatFieldFormula(String formula, RelationContext context) {
        if (StringUtils.isBlank(formula)) {
            return formula;
        }
        Matcher matcher = FIELD_REF_PATTERN.matcher(formula);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String replacement = "[" + matcher.group(1) + "]";
            try {
                CoreDatasetTableField source = context.fields.get(Long.valueOf(matcher.group(1)));
                if (source != null) {
                    replacement = "[" + safeDatasetFieldLabel(source) + "]";
                }
            } catch (NumberFormatException ignored) {
                // Keep the original token when the historical formula is malformed.
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String summaryLabel(String summary) {
        if (StringUtils.isBlank(summary)) {
            return summary;
        }
        return switch (summary.toLowerCase()) {
            case "sum" -> "求和";
            case "count" -> "计数";
            case "avg" -> "平均";
            case "max" -> "最大值";
            case "min" -> "最小值";
            case "count_distinct" -> "去重计数";
            default -> summary;
        };
    }

    private List<ChartFieldUsage> collectChartFieldUsages(CoreChartView chart) {
        Map<String, ChartFieldUsage> usages = new LinkedHashMap<>();
        addChartFieldUsages(usages, chart.getxAxis(), "横轴");
        addChartFieldUsages(usages, chart.getxAxisExt(), "横轴扩展");
        addChartFieldUsages(usages, chart.getyAxis(), "纵轴");
        addChartFieldUsages(usages, chart.getyAxisExt(), "副轴");
        addChartFieldUsages(usages, chart.getExtStack(), "堆叠");
        addChartFieldUsages(usages, chart.getExtBubble(), "气泡");
        addChartFieldUsages(usages, chart.getExtLabel(), "标签");
        addChartFieldUsages(usages, chart.getExtTooltip(), "提示");
        addChartFieldUsages(usages, chart.getFlowMapStartName(), "流向起点");
        addChartFieldUsages(usages, chart.getFlowMapEndName(), "流向终点");
        addChartFieldUsages(usages, chart.getExtColor(), "颜色");
        addChartFieldUsages(usages, chart.getDrillFields(), "钻取");
        for (Long fieldId : extractFieldRefs(chart.getCustomFilter(), chart.getSenior(), chart.getSortPriority())) {
            usages.putIfAbsent("条件:" + fieldId, new ChartFieldUsage(fieldId, "字段 " + fieldId, "条件", null, null));
        }
        return new ArrayList<>(usages.values());
    }

    private void addChartFieldUsages(Map<String, ChartFieldUsage> usages, String json, String role) {
        List<ChartViewFieldDTO> fields = JsonUtil.parseList(json, CHART_FIELD_LIST_TYPE);
        if (fields == null) {
            return;
        }
        int index = 0;
        for (ChartViewFieldDTO field : fields) {
            if (field == null || field.getId() == null || field.getId() <= 0) {
                continue;
            }
            String key = role + ":" + field.getId() + ":" + index++;
            usages.putIfAbsent(key, new ChartFieldUsage(field.getId(), StringUtils.defaultIfBlank(field.getChartShowName(), field.getName()), role, field.getGroupType(), field.getSummary()));
        }
    }

    private void addDatasetJoinLineage(GraphBuilder builder, RelationContext context, CoreDatasetGroup dataset, boolean includeFields) {
        if (dataset == null || StringUtils.isBlank(dataset.getInfo())) {
            return;
        }
        JsonNode root = JsonUtil.parseObject(dataset.getInfo(), JsonNode.class);
        if (root == null) {
            return;
        }
        if (root.isArray()) {
            root.forEach(node -> addDatasetJoinLineageNode(builder, context, node, includeFields));
        } else {
            addDatasetJoinLineageNode(builder, context, root, includeFields);
        }
    }

    private void addDatasetJoinLineageNode(GraphBuilder builder, RelationContext context, JsonNode node, boolean includeFields) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }
        JsonNode unionToParent = node.path("unionToParent");
        if (!unionToParent.isMissingNode() && !unionToParent.isNull()) {
            CoreDatasetTable parent = findDatasetTable(context, unionToParent.path("parentDs"));
            CoreDatasetTable current = findDatasetTable(context, unionToParent.path("currentDs"));
            if (current == null) {
                current = findDatasetTable(context, node.path("currentDs"));
            }
            if (parent != null && current != null) {
                addPhysicalTableNode(builder, parent);
                addPhysicalTableNode(builder, current);
                builder.addEdge(tableNodeId(parent), tableNodeId(current), "dataset_table_join", joinLabel(unionToParent));
                if (includeFields) {
                    addJoinFieldLineage(builder, context, parent, current, unionToParent);
                }
            }
        }

        JsonNode children = node.path("childrenDs");
        if (children.isArray()) {
            children.forEach(child -> addDatasetJoinLineageNode(builder, context, child, includeFields));
        }
    }

    private CoreDatasetTable findDatasetTable(RelationContext context, JsonNode dsNode) {
        Long id = readLong(dsNode, "id");
        if (id != null && context.tables.containsKey(id)) {
            return context.tables.get(id);
        }
        Long datasourceId = readLong(dsNode, "datasourceId");
        String tableName = readText(dsNode, "tableName");
        if (datasourceId == null || StringUtils.isBlank(tableName)) {
            return null;
        }
        return context.tables.values().stream()
                .filter(table -> Objects.equals(table.getDatasourceId(), datasourceId))
                .filter(table -> Strings.CI.equals(table.getTableName(), tableName))
                .findFirst()
                .orElse(null);
    }

    private void addJoinFieldLineage(GraphBuilder builder, RelationContext context, CoreDatasetTable parent, CoreDatasetTable current, JsonNode unionToParent) {
        JsonNode unionFields = unionToParent.path("unionFields");
        if (!unionFields.isArray()) {
            return;
        }
        String label = joinTypeLabel(readText(unionToParent, "unionType")) + " 关联键";
        for (JsonNode item : unionFields) {
            CoreDatasetTableField parentField = context.fields.get(readLong(item.path("parentField"), "id"));
            CoreDatasetTableField currentField = context.fields.get(readLong(item.path("currentField"), "id"));
            if (parentField == null || currentField == null) {
                continue;
            }
            addDatasetFieldLineage(builder, context, parentField, new HashSet<>());
            addDatasetFieldLineage(builder, context, currentField, new HashSet<>());
            builder.addEdge(tableFieldNodeId(parent, parentField), tableFieldNodeId(current, currentField), "table_field_join", label);
        }
    }

    private String joinLabel(JsonNode unionToParent) {
        String joinType = joinTypeLabel(readText(unionToParent, "unionType"));
        JsonNode unionFields = unionToParent.path("unionFields");
        List<String> fields = new ArrayList<>();
        if (unionFields.isArray()) {
            for (JsonNode item : unionFields) {
                String parentField = readFieldLabel(item.path("parentField"));
                String currentField = readFieldLabel(item.path("currentField"));
                if (StringUtils.isNoneBlank(parentField, currentField)) {
                    fields.add(parentField + " = " + currentField);
                }
                if (fields.size() >= 3) {
                    break;
                }
            }
        }
        return fields.isEmpty() ? joinType : joinType + "：" + String.join("、", fields);
    }

    private String readFieldLabel(JsonNode node) {
        return StringUtils.defaultIfBlank(readText(node, "name"), readText(node, "originName"));
    }

    private String joinTypeLabel(String joinType) {
        if (StringUtils.isBlank(joinType)) {
            return "JOIN";
        }
        return switch (joinType.toLowerCase()) {
            case "left" -> "LEFT JOIN";
            case "right" -> "RIGHT JOIN";
            case "inner" -> "INNER JOIN";
            case "full" -> "FULL JOIN";
            default -> joinType.toUpperCase() + " JOIN";
        };
    }

    private Long readLong(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.longValue();
        }
        if (value.isTextual() && StringUtils.isNumeric(value.asText())) {
            return Long.valueOf(value.asText());
        }
        return null;
    }

    private String readText(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private Set<Long> extractFieldRefs(String... texts) {
        Set<Long> result = new LinkedHashSet<>();
        if (texts == null) {
            return result;
        }
        for (String text : texts) {
            if (StringUtils.isBlank(text)) {
                continue;
            }
            collectFieldRefs(result, FIELD_REF_PATTERN.matcher(text));
            collectFieldRefs(result, JSON_FIELD_ID_PATTERN.matcher(text));
        }
        return result;
    }

    private void collectFieldRefs(Set<Long> result, Matcher matcher) {
        while (matcher.find()) {
            try {
                result.add(Long.valueOf(matcher.group(1)));
            } catch (NumberFormatException ignored) {
                // Ignore malformed historical chart payloads.
            }
        }
    }

    private static String nodeId(String type, Long id) {
        return type + ":" + id;
    }

    private static String tableNodeId(CoreDatasetTable table) {
        if (table == null) {
            return null;
        }
        String tableName = StringUtils.defaultIfBlank(table.getTableName(), table.getName());
        if (table.getDatasourceId() != null && StringUtils.isNotBlank(tableName)) {
            return TYPE_TABLE + ":" + table.getDatasourceId() + ":" + tableName;
        }
        return TYPE_TABLE + ":" + table.getId();
    }

    private static String tableFieldNodeId(CoreDatasetTable table, CoreDatasetTableField field) {
        String originName = StringUtils.defaultIfBlank(field.getOriginName(), field.getName());
        if (table != null && table.getDatasourceId() != null && StringUtils.isNotBlank(originName)) {
            return TYPE_TABLE_FIELD + ":" + table.getDatasourceId() + ":" + safeTableNameForId(table) + ":" + originName;
        }
        String tableId = field.getDatasetTableId() == null ? String.valueOf(field.getId()) : String.valueOf(field.getDatasetTableId());
        return TYPE_TABLE_FIELD + ":" + tableId + ":" + originName;
    }

    private static String tableFieldResourceId(CoreDatasetTable table, CoreDatasetTableField field) {
        if (table == null) {
            return String.valueOf(field.getId());
        }
        return table.getDatasourceId() + ":" + safeTableNameForId(table) + ":" + StringUtils.defaultIfBlank(field.getOriginName(), field.getName());
    }

    private static String safeTableNameForId(CoreDatasetTable table) {
        return StringUtils.defaultIfBlank(table.getTableName(), StringUtils.defaultIfBlank(table.getName(), String.valueOf(table.getId())));
    }

    private static String datasetFieldNodeId(Long fieldId) {
        return TYPE_DATASET_FIELD + ":" + fieldId;
    }

    private static String chartFieldNodeId(CoreChartView chart, ChartFieldUsage usage) {
        return TYPE_CHART_FIELD + ":" + chart.getId() + ":" + usage.role() + ":" + usage.fieldId();
    }

    private record ChartFieldUsage(Long fieldId, String name, String role, String groupType, String summary) {
    }

    private static class RelationContext {
        private Map<Long, CoreDatasource> datasources = Map.of();
        private Map<Long, CoreDatasetTable> tables = Map.of();
        private Map<Long, CoreDatasetGroup> datasets = Map.of();
        private Map<Long, CoreDatasetTableField> fields = Map.of();
        private Map<Long, CoreChartView> charts = Map.of();
        private Map<Long, DataVisualizationInfo> visualizations = Map.of();
        private Map<Long, List<CoreDatasetTable>> tablesByDataset = Map.of();
        private Map<Long, List<CoreDatasetTable>> tablesByDatasource = Map.of();
        private Map<Long, List<CoreDatasetTableField>> fieldsByDataset = Map.of();
        private Map<Long, List<CoreDatasetTableField>> fieldsByDatasetTable = Map.of();
        private Map<Long, List<CoreChartView>> chartsByDataset = Map.of();
        private Map<Long, List<CoreChartView>> chartsByDv = Map.of();
    }

    private static class GraphBuilder {
        private final Map<String, RelationNodeDTO> nodes = new LinkedHashMap<>();
        private final Map<String, RelationEdgeDTO> edges = new LinkedHashMap<>();

        private void addNode(String id, Long resourceId, String name, String type, String subType, String description, Long createTime, Long updateTime, Integer level) {
            addNode(id, resourceId == null ? null : String.valueOf(resourceId), name, type, subType, description, createTime, updateTime, level);
        }

        private void addNode(String id, String resourceId, String name, String type, String subType, String description, Long createTime, Long updateTime, Integer level) {
            if (StringUtils.isBlank(id) || nodes.containsKey(id)) {
                return;
            }
            RelationNodeDTO node = new RelationNodeDTO();
            node.setId(id);
            node.setResourceId(resourceId);
            node.setName(StringUtils.defaultIfBlank(name, "未命名"));
            node.setType(type);
            node.setSubType(subType);
            node.setDescription(description);
            node.setCreateTime(createTime);
            node.setUpdateTime(updateTime);
            node.setLevel(level);
            nodes.put(id, node);
        }

        private void addEdge(String source, String target, String type, String label) {
            if (StringUtils.isAnyBlank(source, target)) {
                return;
            }
            String key = source + "->" + target + ":" + StringUtils.defaultString(type);
            if (edges.containsKey(key)) {
                return;
            }
            RelationEdgeDTO edge = new RelationEdgeDTO();
            edge.setSource(source);
            edge.setTarget(target);
            edge.setType(type);
            edge.setLabel(label);
            edges.put(key, edge);
        }

        private RelationGraphDTO build() {
            RelationGraphDTO graph = new RelationGraphDTO();
            graph.setNodes(new ArrayList<>(nodes.values()));
            graph.setEdges(new ArrayList<>(edges.values()));
            RelationSummaryDTO summary = new RelationSummaryDTO();
            summary.setDatasourceCount((int) graph.getNodes().stream().filter(node -> TYPE_DATASOURCE.equals(node.getType())).count());
            summary.setTableCount((int) graph.getNodes().stream().filter(node -> TYPE_TABLE.equals(node.getType())).count());
            summary.setTableFieldCount((int) graph.getNodes().stream().filter(node -> TYPE_TABLE_FIELD.equals(node.getType())).count());
            summary.setDatasetFieldCount((int) graph.getNodes().stream().filter(node -> TYPE_DATASET_FIELD.equals(node.getType())).count());
            summary.setDatasetCount((int) graph.getNodes().stream().filter(node -> TYPE_DATASET.equals(node.getType())).count());
            summary.setChartFieldCount((int) graph.getNodes().stream().filter(node -> TYPE_CHART_FIELD.equals(node.getType())).count());
            summary.setChartCount((int) graph.getNodes().stream().filter(node -> TYPE_CHART.equals(node.getType())).count());
            summary.setDvCount((int) graph.getNodes().stream().filter(node -> TYPE_DV.equals(node.getType())).count());
            summary.setEdgeCount(graph.getEdges().size());
            summary.setTotalCount(graph.getNodes().size());
            graph.setSummary(summary);
            return graph;
        }
    }
}
