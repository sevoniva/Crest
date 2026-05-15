package io.dataease.dataset.sync;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.dataset.dao.auto.entity.CoreDatasetSyncTask;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DatasetSyncUtils {

    public static final String CACHE_TABLE_PREFIX = "de_sync_dataset_";
    public static final int DEFAULT_FULL_SYNC_INTERVAL_HOURS = 24;
    public static final int DEFAULT_CACHE_EXPIRE_HOURS = 26;
    public static final int DEFAULT_TASK_TIMEOUT_MINUTES = 360;
    public static final int DEFAULT_FAILURE_WARN_THRESHOLD = 1;

    private DatasetSyncUtils() {
    }

    public static String cacheTableName(Long datasetGroupId) {
        return CACHE_TABLE_PREFIX + datasetGroupId;
    }

    public static String tmpCacheTableName(Long datasetGroupId) {
        return "tmp_" + cacheTableName(datasetGroupId);
    }

    public static boolean shouldRouteToCache(DatasetGroupInfoDTO dataset, Map<String, Object> sqlMap) {
        if (dataset == null || sqlMap == null || !Objects.equals(dataset.getMode(), 1)
                || Objects.equals(dataset.getIsCross(), true)) {
            return false;
        }
        Object dsMapObject = sqlMap.get("dsMap");
        if (!(dsMapObject instanceof Map<?, ?> dsMap) || dsMap.size() != 1) {
            return false;
        }
        Object datasourceObject = dsMap.values().iterator().next();
        if (!(datasourceObject instanceof DatasourceSchemaDTO datasource)) {
            return false;
        }
        return StringUtils.equalsIgnoreCase(datasource.getType(), DatasourceConfiguration.DatasourceType.obOracle.name());
    }

    public static boolean isCacheReady(CoreDatasetSyncTask task) {
        return task != null && Objects.equals(task.getCacheReady(), 1);
    }

    public static boolean isCacheReady(CoreDatasetSyncTask task, String schemaHash) {
        return isCacheReady(task) && StringUtils.equals(task.getSchemaHash(), schemaHash);
    }

    public static boolean canRunIncremental(CoreDatasetSyncTask task, String schemaHash) {
        return task != null
                && StringUtils.equalsIgnoreCase(task.getUpdateType(), "add_scope")
                && StringUtils.isNotBlank(task.getIncrementalLastValue())
                && isCacheReady(task, schemaHash);
    }

    public static boolean shouldRunFullCalibration(CoreDatasetSyncTask task, long now) {
        if (task == null || !StringUtils.equalsIgnoreCase(task.getUpdateType(), "add_scope")) {
            return false;
        }
        int intervalHours = Objects.requireNonNullElse(task.getFullSyncIntervalHours(), DEFAULT_FULL_SYNC_INTERVAL_HOURS);
        if (intervalHours <= 0) {
            return false;
        }
        Long lastFullSyncTime = task.getLastFullSyncTime();
        if (lastFullSyncTime == null || lastFullSyncTime <= 0) {
            return true;
        }
        return now - lastFullSyncTime >= intervalHours * 60L * 60L * 1000L;
    }

    public static boolean isCacheExpired(CoreDatasetSyncTask task, long now) {
        if (!isCacheReady(task) || task.getLastExecTime() == null || task.getLastExecTime() <= 0) {
            return false;
        }
        int expireHours = Objects.requireNonNullElse(task.getCacheExpireHours(), DEFAULT_CACHE_EXPIRE_HOURS);
        if (expireHours <= 0) {
            return false;
        }
        return now - task.getLastExecTime() > expireHours * 60L * 60L * 1000L;
    }

    public static boolean isTaskTimedOut(CoreDatasetSyncTask task, long startTime, long now) {
        int timeoutMinutes = Objects.requireNonNullElse(task == null ? null : task.getTaskTimeoutMinutes(), DEFAULT_TASK_TIMEOUT_MINUTES);
        return timeoutMinutes > 0 && now - startTime > timeoutMinutes * 60L * 1000L;
    }

    public static boolean isFailureWarned(CoreDatasetSyncTask task) {
        if (task == null) {
            return false;
        }
        int failures = Objects.requireNonNullElse(task.getConsecutiveFailures(), 0);
        int threshold = Objects.requireNonNullElse(task.getFailureWarnThreshold(), DEFAULT_FAILURE_WARN_THRESHOLD);
        return threshold > 0 && failures >= threshold;
    }

    public static boolean isTaskRunnable(CoreDatasetSyncTask task) {
        return task != null
                && !StringUtils.equalsAnyIgnoreCase(task.getTaskStatus(), TaskStatus.Stopped.name(), TaskStatus.Suspend.name());
    }

    public static ReconcileResult reconcile(Long sourceRowCount, Long cacheRowCount, String sourceWatermark, String cacheWatermark) {
        if (!Objects.equals(sourceRowCount, cacheRowCount)) {
            return new ReconcileResult("WARNING", "源端行数 " + sourceRowCount + " 与缓存行数 " + cacheRowCount + " 不一致");
        }
        if (StringUtils.isNotBlank(sourceWatermark) || StringUtils.isNotBlank(cacheWatermark)) {
            if (!StringUtils.equals(StringUtils.defaultString(sourceWatermark), StringUtils.defaultString(cacheWatermark))) {
                return new ReconcileResult(
                        "WARNING",
                        "源端最大水位 " + sourceWatermark + " 与缓存最大水位 " + cacheWatermark + " 不一致"
                );
            }
        }
        return new ReconcileResult("PASSED", "对账通过");
    }

    public static List<TableField> toEngineTableFields(List<DatasetTableFieldDTO> fields) {
        List<TableField> tableFields = new ArrayList<>();
        if (fields == null) {
            return tableFields;
        }
        for (DatasetTableFieldDTO field : fields) {
            if (!Objects.equals(field.getChecked(), true)) {
                continue;
            }
            if (!Objects.equals(field.getExtField(), ExtFieldConstant.EXT_NORMAL)) {
                continue;
            }
            String columnName = StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName());
            if (StringUtils.isBlank(columnName)) {
                continue;
            }
            TableField tableField = new TableField();
            tableField.setName(columnName);
            tableField.setOriginName(columnName);
            tableField.setFieldType(field.getName());
            tableField.setType(field.getType());
            tableField.setDeExtractType(field.getDeExtractType());
            tableField.setDeType(field.getDeType());
            tableField.setChecked(true);
            tableFields.add(tableField);
        }
        return tableFields;
    }

    public static String buildIncrementalPredicate(DatasetTableFieldDTO field, String lastValue, String prefix, String suffix) {
        return buildIncrementalPredicate(field, lastValue, prefix, suffix, false);
    }

    public static String buildIncrementalPredicate(DatasetTableFieldDTO field, String lastValue, String prefix, String suffix, boolean inclusive) {
        String column = prefix + StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName()) + suffix;
        String operator = inclusive ? " >= " : " > ";
        if (Objects.equals(field.getDeExtractType(), 1) || Objects.equals(field.getDeType(), 1)) {
            return column + operator + "TO_TIMESTAMP('" + escapeSqlLiteral(lastValue) + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
        }
        if (Objects.equals(field.getDeExtractType(), 2) || Objects.equals(field.getDeExtractType(), 3)
                || Objects.equals(field.getDeType(), 2) || Objects.equals(field.getDeType(), 3)) {
            return column + operator + lastValue;
        }
        return column + operator + "'" + escapeSqlLiteral(lastValue) + "'";
    }

    public static String buildCacheWatermarkPredicate(DatasetTableFieldDTO field, String lastValue, String prefix, String suffix, String operator) {
        String column = prefix + StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName()) + suffix;
        String safeOperator = StringUtils.defaultIfBlank(operator, ">").trim();
        if (Objects.equals(field.getDeExtractType(), 2) || Objects.equals(field.getDeExtractType(), 3)
                || Objects.equals(field.getDeType(), 2) || Objects.equals(field.getDeType(), 3)) {
            return column + " " + safeOperator + " " + lastValue;
        }
        return column + " " + safeOperator + " '" + escapeSqlLiteral(lastValue) + "'";
    }

    public static String buildCacheSelectSql(Long datasetGroupId, List<DatasetTableFieldDTO> fields, String prefix, String suffix, String schemaAlias) {
        String selectFields = fields.stream()
                .filter(field -> Objects.equals(field.getChecked(), true))
                .filter(field -> Objects.equals(field.getExtField(), ExtFieldConstant.EXT_NORMAL))
                .map(field -> StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName()))
                .filter(StringUtils::isNotBlank)
                .map(fieldName -> quote(fieldName, prefix, suffix) + " AS " + quote(fieldName, prefix, suffix))
                .collect(Collectors.joining(","));
        String table = quote(cacheTableName(datasetGroupId), prefix, suffix);
        if (StringUtils.isNotBlank(schemaAlias)) {
            table = quote(schemaAlias, prefix, suffix) + "." + table;
        }
        return "SELECT " + selectFields + " FROM " + table;
    }

    public static String buildOraclePageSql(String sourceSql, int limit, int offset, List<DatasetTableFieldDTO> fields, String prefix, String suffix) {
        String selectFields = fields.stream()
                .filter(field -> Objects.equals(field.getChecked(), true))
                .filter(field -> Objects.equals(field.getExtField(), ExtFieldConstant.EXT_NORMAL))
                .map(field -> StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName()))
                .filter(StringUtils::isNotBlank)
                .map(fieldName -> "DE_SYNC_PAGE." + quote(fieldName, prefix, suffix))
                .collect(Collectors.joining(","));
        return "SELECT " + selectFields
                + " FROM (SELECT DE_SYNC_SRC.*, ROWNUM AS DE_ROWNUM FROM (" + sourceSql + ") DE_SYNC_SRC WHERE ROWNUM <= "
                + (offset + limit) + ") DE_SYNC_PAGE WHERE DE_ROWNUM > " + offset;
    }

    public static String quote(String value, String prefix, String suffix) {
        return prefix + value + suffix;
    }

    public static String schemaHash(List<DatasetTableFieldDTO> fields) {
        String signature = fields.stream()
                .filter(field -> Objects.equals(field.getChecked(), true))
                .filter(field -> Objects.equals(field.getExtField(), ExtFieldConstant.EXT_NORMAL))
                .map(field -> String.join("|",
                        StringUtils.defaultString(field.getDataeaseName()),
                        StringUtils.defaultString(field.getFieldShortName()),
                        StringUtils.defaultString(field.getName()),
                        StringUtils.defaultString(field.getOriginName()),
                        String.valueOf(field.getDeExtractType()),
                        String.valueOf(field.getDeType())
                ))
                .collect(Collectors.joining("\n"));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(signature.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static String escapeSqlLiteral(String value) {
        return StringUtils.defaultString(value).replace("'", "''");
    }

    public record ReconcileResult(String status, String message) {
    }
}
