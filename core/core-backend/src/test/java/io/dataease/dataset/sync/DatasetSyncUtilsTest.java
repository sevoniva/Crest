package io.dataease.dataset.sync;

import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.dataset.dao.auto.entity.CoreDatasetSyncTask;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.TableField;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatasetSyncUtilsTest {

    @Test
    public void cacheTableNameUsesDatasetIdNamespace() {
        assertEquals("de_sync_dataset_12345", DatasetSyncUtils.cacheTableName(12345L));
        assertEquals("tmp_de_sync_dataset_12345", DatasetSyncUtils.tmpCacheTableName(12345L));
    }

    @Test
    public void toEngineTableFieldsUsesDataeaseNamesAndSkipsCalculatedFields() {
        DatasetTableFieldDTO normal = field(1L, "USER_ID", "用户ID", "f_user_id", 2, ExtFieldConstant.EXT_NORMAL);
        DatasetTableFieldDTO calc = field(2L, "sum([1])", "计算字段", "f_calc", 3, ExtFieldConstant.EXT_CALC);

        List<TableField> fields = DatasetSyncUtils.toEngineTableFields(List.of(normal, calc));

        assertEquals(1, fields.size());
        assertEquals("f_user_id", fields.get(0).getName());
        assertEquals("f_user_id", fields.get(0).getOriginName());
        assertEquals("用户ID", fields.get(0).getFieldType());
        assertEquals(Integer.valueOf(2), fields.get(0).getDeExtractType());
    }

    @Test
    public void buildIncrementalPredicateFormatsValuesByFieldType() {
        DatasetTableFieldDTO numberField = field(1L, "ID", "ID", "f_id", 2, ExtFieldConstant.EXT_NORMAL);
        DatasetTableFieldDTO textField = field(2L, "CODE", "CODE", "f_code", 0, ExtFieldConstant.EXT_NORMAL);
        DatasetTableFieldDTO timeField = field(3L, "UPDATED_AT", "更新时间", "f_updated_at", 1, ExtFieldConstant.EXT_NORMAL);

        assertEquals("\"f_id\" > 100", DatasetSyncUtils.buildIncrementalPredicate(numberField, "100", "\"", "\""));
        assertEquals("\"f_code\" > 'A''01'", DatasetSyncUtils.buildIncrementalPredicate(textField, "A'01", "\"", "\""));
        assertEquals(
                "\"f_updated_at\" > TO_TIMESTAMP('2026-05-14 12:30:01', 'YYYY-MM-DD HH24:MI:SS.FF')",
                DatasetSyncUtils.buildIncrementalPredicate(timeField, "2026-05-14 12:30:01", "\"", "\"")
        );
    }

    @Test
    public void buildIncrementalPredicateCanReplayLastWatermarkBucket() {
        DatasetTableFieldDTO timeField = field(3L, "UPDATED_AT", "更新时间", "f_updated_at", 1, ExtFieldConstant.EXT_NORMAL);

        assertEquals(
                "\"f_updated_at\" >= TO_TIMESTAMP('2026-05-14 12:30:01.0', 'YYYY-MM-DD HH24:MI:SS.FF')",
                DatasetSyncUtils.buildIncrementalPredicate(timeField, "2026-05-14 12:30:01.0", "\"", "\"", true)
        );
        assertEquals(
                "`f_updated_at` >= '2026-05-14 12:30:01.0'",
                DatasetSyncUtils.buildCacheWatermarkPredicate(timeField, "2026-05-14 12:30:01.0", "`", "`", ">=")
        );
        assertEquals(
                "`f_updated_at` < '2026-05-14 12:30:01.0'",
                DatasetSyncUtils.buildCacheWatermarkPredicate(timeField, "2026-05-14 12:30:01.0", "`", "`", "<")
        );
    }

    @Test
    public void schemaHashChangesWhenSelectedFieldShapeChanges() {
        DatasetTableFieldDTO id = field(1L, "ID", "ID", "f_id", 2, ExtFieldConstant.EXT_NORMAL);
        DatasetTableFieldDTO name = field(2L, "NAME", "名称", "f_name", 0, ExtFieldConstant.EXT_NORMAL);
        DatasetTableFieldDTO renamed = field(2L, "NAME", "名称", "f_user_name", 0, ExtFieldConstant.EXT_NORMAL);

        String schemaHash = DatasetSyncUtils.schemaHash(List.of(id, name));

        assertEquals(schemaHash, DatasetSyncUtils.schemaHash(List.of(id, name)));
        assertFalse(schemaHash.equals(DatasetSyncUtils.schemaHash(List.of(id, renamed))));
    }

    @Test
    public void buildCacheSelectSqlReadsDataeaseColumnsFromCacheTable() {
        DatasetTableFieldDTO id = field(1L, "ID", "ID", "f_id", 2, ExtFieldConstant.EXT_NORMAL);
        DatasetTableFieldDTO name = field(2L, "NAME", "名称", "f_name", 0, ExtFieldConstant.EXT_NORMAL);

        String sql = DatasetSyncUtils.buildCacheSelectSql(8L, List.of(id, name), "`", "`", "s_a_1");

        assertEquals(
                "SELECT `f_id` AS `f_id`,`f_name` AS `f_name` FROM `s_a_1`.`de_sync_dataset_8`",
                sql
        );
    }

    @Test
    public void buildOraclePageSqlKeepsOnlySyncableColumnsAndHidesRowNumber() {
        DatasetTableFieldDTO id = field(1L, "ID", "ID", "f_id", 2, ExtFieldConstant.EXT_NORMAL);
        DatasetTableFieldDTO calc = field(2L, "[1] + 1", "计算字段", "f_calc", 2, ExtFieldConstant.EXT_CALC);

        String sql = DatasetSyncUtils.buildOraclePageSql(
                "SELECT t_a_0.\"ID\" AS \"f_id\" FROM \"T_USER\" t_a_0",
                1000,
                2000,
                List.of(id, calc),
                "\"",
                "\""
        );

        assertEquals(
                "SELECT DE_SYNC_PAGE.\"f_id\" FROM (SELECT DE_SYNC_SRC.*, ROWNUM AS DE_ROWNUM FROM (SELECT t_a_0.\"ID\" AS \"f_id\" FROM \"T_USER\" t_a_0) DE_SYNC_SRC WHERE ROWNUM <= 3000) DE_SYNC_PAGE WHERE DE_ROWNUM > 2000",
                sql
        );
    }

    @Test
    public void shouldRouteToCacheOnlyForSingleObOracleExtractDataset() {
        assertTrue(DatasetSyncUtils.shouldRouteToCache(dataset(1, false), sqlMap("obOracle")));

        assertFalse(DatasetSyncUtils.shouldRouteToCache(dataset(0, false), sqlMap("obOracle")));
        assertFalse(DatasetSyncUtils.shouldRouteToCache(dataset(1, true), sqlMap("obOracle")));
        assertFalse(DatasetSyncUtils.shouldRouteToCache(dataset(1, false), sqlMap("Excel")));
        assertFalse(DatasetSyncUtils.shouldRouteToCache(dataset(1, false), sqlMap("API")));
        assertFalse(DatasetSyncUtils.shouldRouteToCache(dataset(1, false), sqlMap("mysql")));
        assertFalse(DatasetSyncUtils.shouldRouteToCache(dataset(1, false), sqlMap("obOracle", "mysql")));
        assertFalse(DatasetSyncUtils.shouldRouteToCache(null, sqlMap("obOracle")));
        assertFalse(DatasetSyncUtils.shouldRouteToCache(dataset(1, false), null));
    }

    @Test
    public void cacheReadyRequiresSuccessfulMaterializedTable() {
        CoreDatasetSyncTask task = new CoreDatasetSyncTask();
        assertFalse(DatasetSyncUtils.isCacheReady(null));
        assertFalse(DatasetSyncUtils.isCacheReady(task));

        task.setCacheReady(0);
        assertFalse(DatasetSyncUtils.isCacheReady(task));

        task.setCacheReady(1);
        assertTrue(DatasetSyncUtils.isCacheReady(task));

        task.setSchemaHash("schema-v1");
        assertTrue(DatasetSyncUtils.isCacheReady(task, "schema-v1"));
        assertFalse(DatasetSyncUtils.isCacheReady(task, "schema-v2"));
    }

    @Test
    public void taskRunnableRequiresExistingActiveTask() {
        CoreDatasetSyncTask task = new CoreDatasetSyncTask();

        assertFalse(DatasetSyncUtils.isTaskRunnable(null));

        task.setTaskStatus(TaskStatus.UnderExecution.name());
        assertTrue(DatasetSyncUtils.isTaskRunnable(task));

        task.setTaskStatus(TaskStatus.Stopped.name());
        assertFalse(DatasetSyncUtils.isTaskRunnable(task));

        task.setTaskStatus(TaskStatus.Suspend.name());
        assertFalse(DatasetSyncUtils.isTaskRunnable(task));
    }

    @Test
    public void incrementalRequiresWatermarkReadyCacheAndMatchingSchema() {
        CoreDatasetSyncTask task = new CoreDatasetSyncTask();
        task.setUpdateType("add_scope");
        task.setIncrementalLastValue("2026-05-14 10:50:00.0");
        task.setCacheReady(1);
        task.setSchemaHash("schema-v1");

        assertTrue(DatasetSyncUtils.canRunIncremental(task, "schema-v1"));

        task.setSchemaHash("schema-v0");
        assertFalse(DatasetSyncUtils.canRunIncremental(task, "schema-v1"));

        task.setSchemaHash("schema-v1");
        task.setCacheReady(0);
        assertFalse(DatasetSyncUtils.canRunIncremental(task, "schema-v1"));

        task.setCacheReady(1);
        task.setIncrementalLastValue(null);
        assertFalse(DatasetSyncUtils.canRunIncremental(task, "schema-v1"));

        task.setIncrementalLastValue("2026-05-14 10:50:00.0");
        task.setUpdateType("all_scope");
        assertFalse(DatasetSyncUtils.canRunIncremental(task, "schema-v1"));
    }

    @Test
    public void fullCalibrationRunsOnlyWhenIncrementalTaskExceedsConfiguredInterval() {
        CoreDatasetSyncTask task = new CoreDatasetSyncTask();
        task.setUpdateType("add_scope");
        task.setFullSyncIntervalHours(24);
        task.setLastFullSyncTime(1_000L);

        assertFalse(DatasetSyncUtils.shouldRunFullCalibration(task, 1_000L + 23 * 60 * 60 * 1000L));
        assertTrue(DatasetSyncUtils.shouldRunFullCalibration(task, 1_000L + 24 * 60 * 60 * 1000L));

        task.setFullSyncIntervalHours(0);
        assertFalse(DatasetSyncUtils.shouldRunFullCalibration(task, 1_000L + 48 * 60 * 60 * 1000L));

        task.setFullSyncIntervalHours(24);
        task.setUpdateType("all_scope");
        assertFalse(DatasetSyncUtils.shouldRunFullCalibration(task, 1_000L + 48 * 60 * 60 * 1000L));
    }

    @Test
    public void cacheExpiresWhenLastSuccessfulSyncIsOlderThanConfiguredThreshold() {
        CoreDatasetSyncTask task = new CoreDatasetSyncTask();
        task.setCacheReady(1);
        task.setCacheExpireHours(26);
        task.setLastExecTime(1_000L);

        assertFalse(DatasetSyncUtils.isCacheExpired(task, 1_000L + 25 * 60 * 60 * 1000L));
        assertTrue(DatasetSyncUtils.isCacheExpired(task, 1_000L + 26 * 60 * 60 * 1000L + 1));

        task.setCacheReady(0);
        assertFalse(DatasetSyncUtils.isCacheExpired(task, 1_000L + 48 * 60 * 60 * 1000L));
    }

    @Test
    public void reconcileStatusReportsRowCountAndWatermarkMismatches() {
        DatasetSyncUtils.ReconcileResult passed = DatasetSyncUtils.reconcile(10L, 10L, "2026-05-15 10:00:00.0", "2026-05-15 10:00:00.0");
        assertEquals("PASSED", passed.status());
        assertEquals("对账通过", passed.message());

        DatasetSyncUtils.ReconcileResult rowMismatch = DatasetSyncUtils.reconcile(10L, 12L, "2026-05-15 10:00:00.0", "2026-05-15 10:00:00.0");
        assertEquals("WARNING", rowMismatch.status());
        assertEquals("源端行数 10 与缓存行数 12 不一致", rowMismatch.message());

        DatasetSyncUtils.ReconcileResult watermarkMismatch = DatasetSyncUtils.reconcile(10L, 10L, "2026-05-15 10:00:00.0", "2026-05-15 09:59:00.0");
        assertEquals("WARNING", watermarkMismatch.status());
        assertEquals("源端最大水位 2026-05-15 10:00:00.0 与缓存最大水位 2026-05-15 09:59:00.0 不一致", watermarkMismatch.message());
    }

    private DatasetTableFieldDTO field(Long id, String originName, String name, String dataeaseName, Integer deExtractType, Integer extField) {
        DatasetTableFieldDTO field = new DatasetTableFieldDTO();
        field.setId(id);
        field.setOriginName(originName);
        field.setName(name);
        field.setDataeaseName(dataeaseName);
        field.setFieldShortName(dataeaseName);
        field.setDeExtractType(deExtractType);
        field.setDeType(deExtractType);
        field.setExtField(extField);
        field.setChecked(true);
        return field;
    }

    private DatasetGroupInfoDTO dataset(Integer mode, Boolean isCross) {
        DatasetGroupInfoDTO dataset = new DatasetGroupInfoDTO();
        dataset.setMode(mode);
        dataset.setIsCross(isCross);
        return dataset;
    }

    private Map<String, Object> sqlMap(String... types) {
        Map<Long, DatasourceSchemaDTO> dsMap = new LinkedHashMap<>();
        long id = 1L;
        for (String type : types) {
            DatasourceSchemaDTO datasource = new DatasourceSchemaDTO();
            datasource.setId(id);
            datasource.setType(type);
            dsMap.put(id, datasource);
            id++;
        }
        Map<String, Object> sqlMap = new LinkedHashMap<>();
        sqlMap.put("dsMap", dsMap);
        return sqlMap;
    }
}
