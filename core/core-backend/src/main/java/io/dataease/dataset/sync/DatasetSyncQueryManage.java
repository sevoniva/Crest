package io.dataease.dataset.sync;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.constant.SQLConstants;
import io.dataease.dataset.dao.auto.entity.CoreDatasetSyncTask;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatasetSyncQueryManage {

    @Resource
    private EngineManage engineManage;
    @Resource
    private DatasetSyncTaskManage taskManage;

    @SuppressWarnings("unchecked")
    public Map<String, Object> routeIfSynced(DatasetGroupInfoDTO dataset, Map<String, Object> sqlMap) {
        if (!DatasetSyncUtils.shouldRouteToCache(dataset, sqlMap)) {
            return sqlMap;
        }
        List<DatasetTableFieldDTO> fields = (List<DatasetTableFieldDTO>) sqlMap.get("field");
        if (ObjectUtils.isEmpty(fields)) {
            return sqlMap;
        }
        CoreDatasetSyncTask task = taskManage.selectByDatasetGroupId(dataset.getId());
        if (!DatasetSyncUtils.isCacheReady(task, DatasetSyncUtils.schemaHash(fields))) {
            return sqlMap;
        }
        if (!taskManage.cacheTableExists(dataset.getId())) {
            return sqlMap;
        }

        CoreDatasource engine = engineManage.getDeEngine();
        DatasourceSchemaDTO datasource = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasource, engine);
        datasource.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasource.getId()));

        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasource.getType());
        Map<Long, DatasourceSchemaDTO> dsMap = new LinkedHashMap<>();
        dsMap.put(datasource.getId(), datasource);

        Map<String, Object> routed = new LinkedHashMap<>(sqlMap);
        routed.put("sql", DatasetSyncUtils.buildCacheSelectSql(
                dataset.getId(),
                fields,
                datasourceType.getPrefix(),
                datasourceType.getSuffix(),
                datasource.getSchemaAlias()
        ));
        routed.put("dsMap", dsMap);
        routed.put("isFullJoin", false);
        return routed;
    }
}
