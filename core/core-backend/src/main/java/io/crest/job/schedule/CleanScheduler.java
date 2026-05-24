package io.crest.job.schedule;

import io.crest.datasource.server.DatasourceTaskServer;
import io.crest.exportCenter.manage.ExportCenterManage;
import io.crest.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CleanScheduler {

    @Resource(name = "exportCenterManage")
    private ExportCenterManage exportCenterManage;
    @Resource(name = "datasourceTaskServer")
    private DatasourceTaskServer datasourceTaskServer;

    @Scheduled(cron = "0 0 0 * * ?")
    public void clean() {
        LogUtil.info("Start to execute export file cleaner ...");
        exportCenterManage.cleanLog();
        LogUtil.info("Execute export file cleaner success");
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanSyncLog() {
        LogUtil.info("Start to clean sync log ...");
        datasourceTaskServer.cleanLog();
        LogUtil.info("End to clean sync log.");
    }
}
