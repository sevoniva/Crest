package io.dataease.dataset.sync;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.dataease.api.dataset.dto.DatasetSyncTaskDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.entity.CoreDatasetSyncTask;
import io.dataease.dataset.dao.auto.entity.CoreDatasetSyncTaskLog;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.dataset.manage.DatasetGroupManage;
import io.dataease.dataset.manage.DatasetSQLManage;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.provider.EngineProvider;
import io.dataease.datasource.provider.ProviderUtil;
import io.dataease.datasource.request.EngineRequest;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.datasource.server.DatasourceTaskServer;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.utils.CommonThreadPool;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DatasetSyncManage {

    private static final int PAGE_SIZE = 1000;
    private static final int FETCH_RETRY_TIMES = 3;

    @Resource
    private DatasetSyncTaskManage taskManage;
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private EngineManage engineManage;
    @Resource
    private CalciteProvider calciteProvider;
    @Resource
    private CoreDatasetGroupMapper datasetGroupMapper;
    @Resource
    private CommonThreadPool commonThreadPool;

    public DatasetSyncTaskDTO executeNow(Long datasetGroupId) {
        CoreDatasetSyncTask task = taskManage.selectByDatasetGroupId(datasetGroupId);
        if (task == null) {
            DatasetSyncTaskDTO dto = new DatasetSyncTaskDTO();
            dto.setDatasetGroupId(datasetGroupId);
            dto.setUpdateType(DatasetSyncTaskManage.DEFAULT_UPDATE_TYPE);
            dto.setSyncRate(DatasourceTaskServer.ScheduleType.RIGHTNOW.name());
            task = taskManage.selectById(taskManage.save(dto).getId());
        }
        Long taskId = task.getId();
        commonThreadPool.addTask(() -> {
            try {
                execute(datasetGroupId, taskId, DatasourceTaskServer.ScheduleType.MANUAL.name(), null, true);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        });
        return taskManage.task(datasetGroupId);
    }

    public void execute(Long datasetGroupId, Long taskId, JobExecutionContext context) {
        try {
            CoreDatasetSyncTask task = taskManage.selectById(taskId);
            if (task == null) {
                task = taskManage.selectByDatasetGroupId(datasetGroupId);
            }
            if (task == null) {
                return;
            }
            execute(task.getDatasetGroupId(), task.getId(), task.getSyncRate(), context, false);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    private void execute(Long datasetGroupId, Long taskId, String triggerType, JobExecutionContext context, boolean manual) throws Exception {
        CoreDatasetSyncTask task = taskManage.selectById(taskId);
        if (task == null) {
            return;
        }
        if (!manual && StringUtils.equalsAnyIgnoreCase(task.getTaskStatus(), TaskStatus.Stopped.name(), TaskStatus.Suspend.name())) {
            return;
        }
        if (taskManage.markUnderExecution(task)) {
            LogUtil.info("Skip dataset sync task due to exist another running task, dataset ID: " + datasetGroupId);
            return;
        }

        CoreDatasetSyncTaskLog log = taskManage.initLog(task, triggerType);
        TaskStatus status = TaskStatus.Completed;
        SyncResult result = new SyncResult();
        try {
            SyncContext syncContext = prepareContext(datasetGroupId);
            String schemaHash = DatasetSyncUtils.schemaHash(syncContext.checkedFields);
            result.schemaHash = schemaHash;
            DatasourceServer.UpdateType updateType = DatasourceServer.UpdateType.valueOf(
                    StringUtils.defaultIfBlank(task.getUpdateType(), DatasetSyncTaskManage.DEFAULT_UPDATE_TYPE)
            );
            if (updateType == DatasourceServer.UpdateType.add_scope
                    && DatasetSyncUtils.canRunIncremental(task, schemaHash)
                    && cacheTableExists(syncContext, DatasetSyncUtils.cacheTableName(datasetGroupId))) {
                result = syncIncremental(syncContext, task);
            } else {
                result = syncFull(syncContext, task);
            }
            result.schemaHash = schemaHash;
            log.setInfo("同步完成");
            updateDatasetSyncStatus(datasetGroupId, TaskStatus.Completed, context);
        } catch (Exception e) {
            status = TaskStatus.Error;
            log.setInfo(StringUtils.defaultString(e.getMessage()));
            updateDatasetSyncStatus(datasetGroupId, TaskStatus.Error, context);
            if (manual) {
                throw e;
            }
        } finally {
            log.setTaskStatus(status.name());
            log.setEndTime(System.currentTimeMillis());
            log.setRowCount(result.rowCount);
            taskManage.updateLog(log);
            taskManage.finishTask(task, status, result.incrementalLastValue, result.schemaHash);
        }
    }

    @SuppressWarnings("unchecked")
    private SyncContext prepareContext(Long datasetGroupId) throws Exception {
        DatasetGroupInfoDTO dataset = datasetGroupManage.getForCount(datasetGroupId);
        if (dataset == null || !StringUtils.equalsIgnoreCase(dataset.getNodeType(), "dataset")) {
            DEException.throwException("数据集不存在");
        }
        if (!Objects.equals(dataset.getMode(), 1)) {
            DEException.throwException("请先开启数据集同步模式");
        }
        if (Objects.equals(dataset.getIsCross(), true)) {
            DEException.throwException("OB Oracle 数据集同步暂不支持跨源数据集");
        }
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(dataset, null);
        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        if (dsMap == null || dsMap.size() != 1) {
            DEException.throwException("OB Oracle 数据集同步仅支持单一数据源");
        }
        DatasourceSchemaDTO datasource = dsMap.entrySet().iterator().next().getValue();
        if (!StringUtils.equalsIgnoreCase(datasource.getType(), DatasourceConfiguration.DatasourceType.obOracle.name())) {
            DEException.throwException("当前同步能力仅支持 OB Oracle 数据源");
        }
        String sourceSql = Utils.replaceSchemaAlias((String) sqlMap.get("sql"), dsMap);
        List<DatasetTableFieldDTO> checkedFields = (List<DatasetTableFieldDTO>) sqlMap.get("field");
        List<DatasetTableFieldDTO> syncFields = checkedFields.stream()
                .filter(field -> Objects.equals(field.getChecked(), true))
                .filter(field -> Objects.equals(field.getExtField(), ExtFieldConstant.EXT_NORMAL))
                .collect(Collectors.toList());
        List<TableField> tableFields = DatasetSyncUtils.toEngineTableFields(syncFields);
        if (tableFields.isEmpty()) {
            DEException.throwException("数据集没有可同步字段");
        }
        CoreDeEngine engine = engineManage.info();
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        Provider sourceProvider = ProviderFactory.getProvider(datasource.getType());
        return new SyncContext(dataset, sourceSql, dsMap, sourceProvider, engine, engineProvider, syncFields, tableFields);
    }

    private SyncResult syncFull(SyncContext context, CoreDatasetSyncTask task) throws Exception {
        String tableName = DatasetSyncUtils.cacheTableName(context.dataset.getId());
        createEngineTable(context, tableName);
        dropEngineTable(context, DatasetSyncUtils.tmpCacheTableName(context.dataset.getId()));
        createEngineTable(context, DatasetSyncUtils.tmpCacheTableName(context.dataset.getId()));

        DatasetTableFieldDTO incrementalField = incrementalField(context.dataset, task);
        int incrementalIndex = incrementalIndex(context.checkedFields, incrementalField);
        SyncResult result = new SyncResult();
        int offset = 0;
        try {
            while (true) {
                assertTaskRunnable(task.getId());
                taskManage.touchHeartbeat(task.getId());
                List<String[]> rows = fetchSourcePage(context, context.sourceSql, offset);
                insertRows(context, tableName, DatasourceServer.UpdateType.all_scope, rows);
                result.rowCount += rows.size();
                result.incrementalLastValue = maxWatermark(result.incrementalLastValue, rows, incrementalIndex, incrementalField);
                if (rows.size() < PAGE_SIZE) {
                    break;
                }
                offset += PAGE_SIZE;
            }
            replaceTable(context, tableName);
            return result;
        } catch (Exception e) {
            dropEngineTable(context, DatasetSyncUtils.tmpCacheTableName(context.dataset.getId()));
            throw e;
        }
    }

    private SyncResult syncIncremental(SyncContext context, CoreDatasetSyncTask task) throws Exception {
        DatasetTableFieldDTO incrementalField = incrementalField(context.dataset, task);
        if (incrementalField == null) {
            return syncFull(context, task);
        }
        int incrementalIndex = incrementalIndex(context.checkedFields, incrementalField);
        if (incrementalIndex < 0) {
            DEException.throwException("增量字段必须是数据集已选中的普通字段");
        }

        String tableName = DatasetSyncUtils.cacheTableName(context.dataset.getId());
        createEngineTable(context, tableName);
        dropEngineTable(context, DatasetSyncUtils.tmpCacheTableName(context.dataset.getId()));
        createEngineTable(context, DatasetSyncUtils.tmpCacheTableName(context.dataset.getId()));
        DatasourceSchemaDTO datasource = context.dsMap.entrySet().iterator().next().getValue();
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasource.getType());
        String fieldName = StringUtils.defaultIfBlank(incrementalField.getDataeaseName(), incrementalField.getFieldShortName());
        copyRetainedCacheRows(context, tableName, incrementalField, task.getIncrementalLastValue());
        String predicate = DatasetSyncUtils.buildIncrementalPredicate(incrementalField, task.getIncrementalLastValue(), datasourceType.getPrefix(), datasourceType.getSuffix(), true);
        String incrementalSql = "SELECT * FROM (" + context.sourceSql + ") de_sync_src WHERE " + predicate
                + " ORDER BY " + incrementalOrderBy(context.checkedFields, fieldName, datasourceType);

        SyncResult result = new SyncResult();
        result.incrementalLastValue = task.getIncrementalLastValue();
        int offset = 0;
        try {
            while (true) {
                assertTaskRunnable(task.getId());
                taskManage.touchHeartbeat(task.getId());
                List<String[]> rows = fetchSourcePage(context, incrementalSql, offset);
                insertRows(context, tableName, DatasourceServer.UpdateType.all_scope, rows);
                result.rowCount += rows.size();
                result.incrementalLastValue = maxWatermark(result.incrementalLastValue, rows, incrementalIndex, incrementalField);
                if (rows.size() < PAGE_SIZE) {
                    break;
                }
                offset += PAGE_SIZE;
            }
            replaceTable(context, tableName);
            return result;
        } catch (Exception e) {
            dropEngineTable(context, DatasetSyncUtils.tmpCacheTableName(context.dataset.getId()));
            throw e;
        }
    }

    private List<String[]> fetchSourcePage(SyncContext context, String sql, int offset) {
        for (int attempt = 1; attempt <= FETCH_RETRY_TIMES; attempt++) {
            try {
                DatasourceSchemaDTO datasource = context.dsMap.entrySet().iterator().next().getValue();
                DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasource.getType());
                String pageSql = DatasetSyncUtils.buildOraclePageSql(sql, PAGE_SIZE, offset, context.checkedFields, datasourceType.getPrefix(), datasourceType.getSuffix());
                DatasourceRequest request = new DatasourceRequest();
                request.setDsList(context.dsMap);
                request.setIsCross(false);
                request.setQuery(pageSql);
                Map<String, Object> data = context.sourceProvider.fetchResultField(request);
                return (List<String[]>) data.get("data");
            } catch (DEException e) {
                if (attempt >= FETCH_RETRY_TIMES || !isTransientDatasourceConnection(e)) {
                    throw e;
                }
                LogUtil.warn("Retry dataset sync source fetch after datasource connection exception, attempt " + attempt + ": " + e.getMessage());
                sleepBeforeRetry(attempt);
            }
        }
        return List.of();
    }

    private boolean isTransientDatasourceConnection(DEException e) {
        return StringUtils.containsIgnoreCase(e.getMessage(), "Datasource connection exception");
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(attempt * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            DEException.throwException("数据集同步重试被中断");
        }
    }

    private void insertRows(SyncContext context, String tableName, DatasourceServer.UpdateType updateType, List<String[]> rows) throws Exception {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        EngineRequest request = new EngineRequest();
        request.setEngine(context.engine);
        request.setQuery(context.engineProvider.insertSql(
                DatasourceConfiguration.DatasourceType.obOracle.name(),
                tableName,
                updateType,
                rows,
                1,
                rows.size(),
                context.tableFields
        ));
        calciteProvider.exec(request);
    }

    private void copyRetainedCacheRows(SyncContext context, String tableName, DatasetTableFieldDTO incrementalField, String lastValue) throws Exception {
        DatasourceConfiguration.DatasourceType engineType = DatasourceConfiguration.DatasourceType.valueOf(context.engine.getType());
        String predicate = DatasetSyncUtils.buildCacheWatermarkPredicate(
                incrementalField,
                lastValue,
                engineType.getPrefix(),
                engineType.getSuffix(),
                "<"
        );
        EngineRequest request = new EngineRequest();
        request.setEngine(context.engine);
        request.setQuery("INSERT INTO " + DatasetSyncUtils.quote(DatasetSyncUtils.tmpCacheTableName(context.dataset.getId()), engineType.getPrefix(), engineType.getSuffix())
                + " SELECT * FROM " + DatasetSyncUtils.quote(tableName, engineType.getPrefix(), engineType.getSuffix()) + " WHERE " + predicate);
        calciteProvider.exec(request);
    }

    private void createEngineTable(SyncContext context, String tableName) throws Exception {
        EngineRequest request = new EngineRequest();
        request.setEngine(context.engine);
        request.setQuery(context.engineProvider.createTableSql(tableName, context.tableFields, context.engine));
        calciteProvider.exec(request);
    }

    private boolean cacheTableExists(SyncContext context, String tableName) {
        try {
            DatasourceConfiguration.DatasourceType engineType = DatasourceConfiguration.DatasourceType.valueOf(context.engine.getType());
            EngineRequest request = new EngineRequest();
            request.setEngine(context.engine);
            request.setQuery("SELECT 1 FROM " + DatasetSyncUtils.quote(tableName, engineType.getPrefix(), engineType.getSuffix()) + " WHERE 1 = 0");
            calciteProvider.exec(request);
            return true;
        } catch (Exception e) {
            LogUtil.warn("Dataset sync cache table is not available, fallback to full sync: " + tableName + ", " + e.getMessage());
            return false;
        }
    }

    private void dropEngineTable(SyncContext context, String tableName) throws Exception {
        EngineRequest request = new EngineRequest();
        request.setEngine(context.engine);
        request.setQuery(context.engineProvider.dropTable(tableName));
        calciteProvider.exec(request);
    }

    private void replaceTable(SyncContext context, String tableName) throws Exception {
        String[] sqls = context.engineProvider.replaceTable(tableName).split(";");
        for (String sql : sqls) {
            if (StringUtils.isBlank(sql)) {
                continue;
            }
            EngineRequest request = new EngineRequest();
            request.setEngine(context.engine);
            request.setQuery(sql);
            calciteProvider.exec(request);
        }
    }

    private DatasetTableFieldDTO incrementalField(DatasetGroupInfoDTO dataset, CoreDatasetSyncTask task) {
        if (!StringUtils.equalsIgnoreCase(task.getUpdateType(), DatasourceServer.UpdateType.add_scope.name())
                || task.getIncrementalFieldId() == null) {
            return null;
        }
        return dataset.getAllFields().stream()
                .filter(field -> Objects.equals(field.getId(), task.getIncrementalFieldId()))
                .filter(field -> Objects.equals(field.getExtField(), ExtFieldConstant.EXT_NORMAL))
                .findFirst()
                .orElse(null);
    }

    private int incrementalIndex(List<DatasetTableFieldDTO> fields, DatasetTableFieldDTO incrementalField) {
        if (incrementalField == null) {
            return -1;
        }
        for (int i = 0; i < fields.size(); i++) {
            if (Objects.equals(fields.get(i).getId(), incrementalField.getId())) {
                return i;
            }
        }
        return -1;
    }

    private String incrementalOrderBy(List<DatasetTableFieldDTO> fields, String firstFieldName, DatasourceConfiguration.DatasourceType datasourceType) {
        LinkedHashSet<String> orderFields = new LinkedHashSet<>();
        orderFields.add(firstFieldName);
        for (DatasetTableFieldDTO field : fields) {
            String fieldName = StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName());
            if (StringUtils.isNotBlank(fieldName)) {
                orderFields.add(fieldName);
            }
        }
        List<String> quotedFields = new ArrayList<>();
        for (String field : orderFields) {
            quotedFields.add(DatasetSyncUtils.quote(field, datasourceType.getPrefix(), datasourceType.getSuffix()));
        }
        return StringUtils.join(quotedFields, ",");
    }

    private void assertTaskRunnable(Long taskId) {
        CoreDatasetSyncTask latest = taskManage.selectById(taskId);
        if (latest != null && StringUtils.equalsAnyIgnoreCase(latest.getTaskStatus(), TaskStatus.Stopped.name(), TaskStatus.Suspend.name())) {
            DEException.throwException("数据集同步任务已停止");
        }
    }

    private String maxWatermark(String current, List<String[]> rows, int index, DatasetTableFieldDTO field) {
        if (index < 0 || field == null || rows == null) {
            return current;
        }
        String max = current;
        for (String[] row : rows) {
            if (row.length <= index) {
                continue;
            }
            max = maxWatermark(max, row[index], field);
        }
        return max;
    }

    private String maxWatermark(String current, String candidate, DatasetTableFieldDTO field) {
        if (StringUtils.isBlank(candidate)) {
            return current;
        }
        if (StringUtils.isBlank(current)) {
            return candidate;
        }
        if (Objects.equals(field.getDeExtractType(), 2) || Objects.equals(field.getDeExtractType(), 3)
                || Objects.equals(field.getDeType(), 2) || Objects.equals(field.getDeType(), 3)) {
            return new BigDecimal(candidate).compareTo(new BigDecimal(current)) > 0 ? candidate : current;
        }
        return candidate.compareTo(current) > 0 ? candidate : current;
    }

    private void updateDatasetSyncStatus(Long datasetGroupId, TaskStatus status, JobExecutionContext context) {
        CoreDatasetGroup record = new CoreDatasetGroup();
        record.setSyncStatus(status.name());
        record.setLastUpdateTime(System.currentTimeMillis());
        if (context != null) {
            record.setQrtzInstance(context.getFireInstanceId());
        }
        UpdateWrapper<CoreDatasetGroup> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", datasetGroupId);
        datasetGroupMapper.update(record, wrapper);
    }

    private static class SyncContext {
        private final DatasetGroupInfoDTO dataset;
        private final String sourceSql;
        private final Map<Long, DatasourceSchemaDTO> dsMap;
        private final Provider sourceProvider;
        private final CoreDeEngine engine;
        private final EngineProvider engineProvider;
        private final List<DatasetTableFieldDTO> checkedFields;
        private final List<TableField> tableFields;

        private SyncContext(DatasetGroupInfoDTO dataset, String sourceSql, Map<Long, DatasourceSchemaDTO> dsMap,
                            Provider sourceProvider, CoreDeEngine engine, EngineProvider engineProvider,
                            List<DatasetTableFieldDTO> checkedFields, List<TableField> tableFields) {
            this.dataset = dataset;
            this.sourceSql = sourceSql;
            this.dsMap = dsMap;
            this.sourceProvider = sourceProvider;
            this.engine = engine;
            this.engineProvider = engineProvider;
            this.checkedFields = checkedFields;
            this.tableFields = tableFields;
        }
    }

    private static class SyncResult {
        private long rowCount = 0L;
        private String incrementalLastValue;
        private String schemaHash;
    }
}
