package io.dataease.listener;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.dataset.dao.auto.entity.CoreDatasetSyncTask;
import io.dataease.dataset.sync.DatasetSyncTaskManage;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.datasource.server.DatasourceTaskServer;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.manage.SysParameterManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Order(value = 2)
public class DataSourceInitStartListener implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private DatasourceSyncManage datasourceSyncManage;
    @Resource
    private DatasourceServer datasourceServer;
    @Resource
    private DataSourceManage dataSourceManage;
    @Resource
    private DatasourceTaskServer datasourceTaskServer;
    @Resource
    private DatasetSyncTaskManage datasetSyncTaskManage;
    @Resource
    private CalciteProvider calciteProvider;
    @Resource
    private EngineManage engineManage;
    @Resource
    private SysParameterManage sysParameterManage;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        try {
            engineManage.initSimpleEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            calciteProvider.initConnectionPool();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<CoreDatasourceTask> list = datasourceTaskServer.listAll();
        for (CoreDatasourceTask task : list) {
            try {
                if (!StringUtils.equalsIgnoreCase(task.getSyncRate(), DatasourceTaskServer.ScheduleType.RIGHTNOW.toString())) {
                    if (task.getEndTime() != null && task.getEndTime() > 0) {
                        if (task.getEndTime() > System.currentTimeMillis()) {
                            datasourceSyncManage.addSchedule(task);
                        } else {
                            datasourceSyncManage.deleteSchedule(task);
                        }
                    } else {
                        datasourceSyncManage.addSchedule(task);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<CoreDatasetSyncTask> datasetSyncTasks = List.of();
        try {
            datasetSyncTaskManage.recoverInterruptedTasks();
            datasetSyncTasks = datasetSyncTaskManage.listAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (CoreDatasetSyncTask task : datasetSyncTasks) {
            try {
                if (!StringUtils.equalsIgnoreCase(task.getSyncRate(), DatasourceTaskServer.ScheduleType.RIGHTNOW.toString())) {
                    if (task.getEndTime() != null && task.getEndTime() > 0) {
                        if (task.getEndTime() > System.currentTimeMillis()) {
                            datasetSyncTaskManage.addSchedule(task);
                        } else {
                            datasetSyncTaskManage.deleteSchedule(task);
                        }
                    } else {
                        datasetSyncTaskManage.addSchedule(task);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            List<CoreSysSetting> coreSysSettings = sysParameterManage.groupList("basic.");
            datasourceServer.addJob(coreSysSettings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataSourceManage.encryptDsConfig();
    }


}
