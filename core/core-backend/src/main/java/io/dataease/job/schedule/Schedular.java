package io.dataease.job.schedule;

import io.dataease.datasource.server.DatasourceServer;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class Schedular {

    @Resource
    private DatasourceServer datasourceServer;

    @Scheduled(cron = "0 0/3 * * * ?")
    public void updateStopJobStatus() {
        datasourceServer.updateStopJobStatus();
    }

}
