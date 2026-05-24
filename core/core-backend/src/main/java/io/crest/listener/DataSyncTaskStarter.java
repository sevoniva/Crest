package io.crest.listener;

import io.crest.job.schedule.DeDataFillingTaskExecutor;
import io.crest.job.schedule.DeTaskExecutor;
import io.crest.job.schedule.DeDataSyncTaskExecutor;
import io.crest.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 4)
public class DataSyncTaskStarter implements ApplicationRunner {

    @Resource
    private DeTaskExecutor deTaskExecutor;

    @Resource
    private DeDataFillingTaskExecutor deDataFillingTaskExecutor;

    @Resource
    private DeDataSyncTaskExecutor deDataSyncTaskExecutor;

    @Override
    public void run(ApplicationArguments args) {
        try {
            deTaskExecutor.init();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e.getCause());
        }
        try {
            deDataFillingTaskExecutor.init();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e.getCause());
        }
        try {
            deDataSyncTaskExecutor.init();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e.getCause());
        }
    }
}
