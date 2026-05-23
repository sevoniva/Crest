package io.dataease.job.schedule;

import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.LogUtil;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DeTaskScheduleJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Trigger trigger = jobExecutionContext.getTrigger();
        JobKey jobKey = trigger.getJobKey();
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        DeTaskExecutor deTaskExecutor = CommonBeanFactory.getBean(DeTaskExecutor.class);
        assert deTaskExecutor != null;
        try {
            boolean taskLoaded = deTaskExecutor.execute(jobDataMap);
            if (!taskLoaded) {
                Objects.requireNonNull(CommonBeanFactory.getBean(ScheduleManager.class)).removeJob(jobKey, trigger.getKey());
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e.getCause());
        }
    }
}
