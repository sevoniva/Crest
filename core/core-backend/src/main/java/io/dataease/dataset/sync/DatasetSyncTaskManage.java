package io.dataease.dataset.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.dataease.api.dataset.dto.DatasetSyncLogDTO;
import io.dataease.api.dataset.dto.DatasetSyncTaskDTO;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.dataset.dao.auto.entity.CoreDatasetSyncTask;
import io.dataease.dataset.dao.auto.entity.CoreDatasetSyncTaskLog;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetSyncTaskLogMapper;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetSyncTaskMapper;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.provider.EngineProvider;
import io.dataease.datasource.provider.ProviderUtil;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.request.EngineRequest;
import io.dataease.datasource.server.DatasourceTaskServer;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.job.schedule.DatasetSyncJob;
import io.dataease.job.schedule.ScheduleManager;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class DatasetSyncTaskManage {

    public static final String DEFAULT_UPDATE_TYPE = "all_scope";
    private static final long RUNNING_TASK_STALE_MILLIS = 12 * 60 * 60 * 1000L;
    private static final String JOB_GROUP_PREFIX = "dataset_sync_";

    @Resource
    private CoreDatasetSyncTaskMapper taskMapper;
    @Resource
    private CoreDatasetSyncTaskLogMapper logMapper;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private EngineManage engineManage;
    @Resource
    private CalciteProvider calciteProvider;

    public CoreDatasetSyncTask selectById(Long id) {
        return taskMapper.selectById(id);
    }

    public CoreDatasetSyncTask selectByDatasetGroupId(Long datasetGroupId) {
        QueryWrapper<CoreDatasetSyncTask> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", datasetGroupId);
        wrapper.last("limit 1");
        return taskMapper.selectOne(wrapper);
    }

    public List<CoreDatasetSyncTask> listAll() {
        return taskMapper.selectList(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public DatasetSyncTaskDTO save(DatasetSyncTaskDTO task) {
        if (task == null || task.getDatasetGroupId() == null) {
            DEException.throwException("datasetGroupId can not be empty");
        }
        CoreDatasetSyncTask record = normalize(task);
        CoreDatasetSyncTask exists = selectByDatasetGroupId(record.getDatasetGroupId());
        long now = System.currentTimeMillis();
        if (exists == null) {
            record.setId(IDUtils.snowID());
            record.setCreateTime(now);
            record.setUpdateTime(now);
            taskMapper.insert(record);
        } else {
            record.setId(exists.getId());
            record.setCreateTime(exists.getCreateTime());
            record.setUpdateTime(now);
            record.setCacheReady(exists.getCacheReady());
            if (StringUtils.isBlank(record.getIncrementalLastValue())) {
                record.setIncrementalLastValue(exists.getIncrementalLastValue());
            }
            taskMapper.updateById(record);
        }
        CoreDatasetSyncTask saved = taskMapper.selectById(record.getId());
        refreshSchedule(saved);
        return toDTO(saved);
    }

    public DatasetSyncTaskDTO task(Long datasetGroupId) {
        CoreDatasetSyncTask task = selectByDatasetGroupId(datasetGroupId);
        return task == null ? null : toDTO(task);
    }

    public List<DatasetSyncLogDTO> logs(Long datasetGroupId) {
        QueryWrapper<CoreDatasetSyncTaskLog> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", datasetGroupId);
        wrapper.orderByDesc("start_time");
        wrapper.last("limit 20");
        return logMapper.selectList(wrapper).stream().map(this::toLogDTO).toList();
    }

    public synchronized boolean markUnderExecution(CoreDatasetSyncTask task) {
        recoverIfStale(task);
        UpdateWrapper<CoreDatasetSyncTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", task.getId());
        wrapper.ne("task_status", TaskStatus.UnderExecution.name());
        long now = System.currentTimeMillis();
        CoreDatasetSyncTask record = new CoreDatasetSyncTask();
        record.setTaskStatus(TaskStatus.UnderExecution.name());
        record.setLastExecTime(now);
        record.setHeartbeatTime(now);
        return taskMapper.update(record, wrapper) == 0;
    }

    public void touchHeartbeat(Long taskId) {
        CoreDatasetSyncTask record = new CoreDatasetSyncTask();
        record.setHeartbeatTime(System.currentTimeMillis());
        UpdateWrapper<CoreDatasetSyncTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", taskId);
        wrapper.eq("task_status", TaskStatus.UnderExecution.name());
        taskMapper.update(record, wrapper);
    }

    public CoreDatasetSyncTaskLog initLog(CoreDatasetSyncTask task, String triggerType) {
        long now = System.currentTimeMillis();
        CoreDatasetSyncTaskLog log = new CoreDatasetSyncTaskLog();
        log.setId(IDUtils.snowID());
        log.setDatasetGroupId(task.getDatasetGroupId());
        log.setTaskId(task.getId());
        log.setUpdateType(task.getUpdateType());
        log.setTableName(DatasetSyncUtils.cacheTableName(task.getDatasetGroupId()));
        log.setTaskStatus(TaskStatus.UnderExecution.name());
        log.setStartTime(now);
        log.setCreateTime(now);
        log.setRowCount(0L);
        log.setTriggerType(triggerType);
        log.setInfo("");
        logMapper.insert(log);
        return log;
    }

    public void updateLog(CoreDatasetSyncTaskLog log) {
        logMapper.updateById(log);
    }

    public void finishTask(CoreDatasetSyncTask task, TaskStatus status, String incrementalLastValue, String schemaHash) {
        CoreDatasetSyncTask record = new CoreDatasetSyncTask();
        record.setLastExecStatus(status.name());
        record.setUpdateTime(System.currentTimeMillis());
        if (status == TaskStatus.Completed) {
            record.setCacheReady(1);
        }
        if (StringUtils.isNotBlank(incrementalLastValue)) {
            record.setIncrementalLastValue(incrementalLastValue);
        }
        if (StringUtils.isNotBlank(schemaHash)) {
            record.setSchemaHash(schemaHash);
        }
        CoreDatasetSyncTask latest = selectById(task.getId());
        if (latest != null && StringUtils.equalsAnyIgnoreCase(latest.getTaskStatus(), TaskStatus.Stopped.name(), TaskStatus.Suspend.name())) {
            record.setTaskStatus(latest.getTaskStatus());
        } else if (StringUtils.equalsIgnoreCase(task.getSyncRate(), DatasourceTaskServer.ScheduleType.RIGHTNOW.name())) {
            record.setTaskStatus(TaskStatus.Stopped.name());
        } else {
            record.setTaskStatus(TaskStatus.WaitingForExecution.name());
        }
        UpdateWrapper<CoreDatasetSyncTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", task.getId());
        taskMapper.update(record, wrapper);
    }

    public void recoverInterruptedTasks() {
        QueryWrapper<CoreDatasetSyncTask> wrapper = new QueryWrapper<>();
        wrapper.eq("task_status", TaskStatus.UnderExecution.name());
        List<CoreDatasetSyncTask> tasks = taskMapper.selectList(wrapper);
        for (CoreDatasetSyncTask task : tasks) {
            markRecovered(task, "应用重启后恢复未完成的数据集同步任务");
        }
    }

    public void stop(Long datasetGroupId) {
        CoreDatasetSyncTask task = selectByDatasetGroupId(datasetGroupId);
        if (task == null) {
            return;
        }
        deleteSchedule(task);
        CoreDatasetSyncTask record = new CoreDatasetSyncTask();
        record.setTaskStatus(TaskStatus.Stopped.name());
        record.setUpdateTime(System.currentTimeMillis());
        UpdateWrapper<CoreDatasetSyncTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", task.getId());
        taskMapper.update(record, wrapper);
    }

    public void deleteByDatasetGroupId(Long datasetGroupId) {
        CoreDatasetSyncTask task = selectByDatasetGroupId(datasetGroupId);
        if (task != null) {
            deleteSchedule(task);
        }
        QueryWrapper<CoreDatasetSyncTask> taskWrapper = new QueryWrapper<>();
        taskWrapper.eq("dataset_group_id", datasetGroupId);
        taskMapper.delete(taskWrapper);
        QueryWrapper<CoreDatasetSyncTaskLog> logWrapper = new QueryWrapper<>();
        logWrapper.eq("dataset_group_id", datasetGroupId);
        logMapper.delete(logWrapper);
        dropCacheTable(DatasetSyncUtils.cacheTableName(datasetGroupId));
        dropCacheTable(DatasetSyncUtils.tmpCacheTableName(datasetGroupId));
    }

    public void refreshSchedule(CoreDatasetSyncTask task) {
        if (StringUtils.equalsIgnoreCase(task.getSyncRate(), DatasourceTaskServer.ScheduleType.RIGHTNOW.name())) {
            deleteSchedule(task);
            return;
        }
        addSchedule(task);
    }

    public void addSchedule(CoreDatasetSyncTask task) {
        Date endTime = null;
        if (task.getEndTime() != null && task.getEndTime() > 0) {
            endTime = new Date(task.getEndTime());
            if (endTime.before(new Date())) {
                deleteSchedule(task);
                return;
            }
        }
        String taskId = task.getId().toString();
        String datasetGroupId = task.getDatasetGroupId().toString();
        scheduleManager.addOrUpdateCronJob(
                new JobKey(taskId, jobGroup(datasetGroupId)),
                new TriggerKey(taskId, jobGroup(datasetGroupId)),
                DatasetSyncJob.class,
                task.getCron(),
                new Date(Objects.requireNonNullElse(task.getStartTime(), System.currentTimeMillis())),
                endTime,
                scheduleManager.getDefaultJobDataMap(datasetGroupId, task.getCron(), taskId, task.getUpdateType())
        );
    }

    public void deleteSchedule(CoreDatasetSyncTask task) {
        String taskId = task.getId().toString();
        String datasetGroupId = task.getDatasetGroupId().toString();
        JobKey jobKey = new JobKey(taskId, jobGroup(datasetGroupId));
        if (!scheduleManager.exist(jobKey)) {
            return;
        }
        scheduleManager.removeJob(jobKey, new TriggerKey(taskId, jobGroup(datasetGroupId)));
    }

    public boolean cacheTableExists(Long datasetGroupId) {
        try {
            CoreDeEngine engine = engineManage.info();
            DatasourceConfiguration.DatasourceType engineType = DatasourceConfiguration.DatasourceType.valueOf(engine.getType());
            EngineRequest request = new EngineRequest();
            request.setEngine(engine);
            request.setQuery("SELECT 1 FROM " + DatasetSyncUtils.quote(
                    DatasetSyncUtils.cacheTableName(datasetGroupId),
                    engineType.getPrefix(),
                    engineType.getSuffix()
            ) + " WHERE 1 = 0");
            calciteProvider.exec(request);
            return true;
        } catch (Exception e) {
            LogUtil.warn("Dataset sync cache table is not available: " + datasetGroupId + ", " + e.getMessage());
            return false;
        }
    }

    private CoreDatasetSyncTask normalize(DatasetSyncTaskDTO task) {
        CoreDatasetSyncTask record = new CoreDatasetSyncTask();
        BeanUtils.copyBean(record, task);
        if (StringUtils.isBlank(record.getName())) {
            record.setName("dataset_sync_" + task.getDatasetGroupId());
        }
        if (StringUtils.isBlank(record.getUpdateType())) {
            record.setUpdateType(DEFAULT_UPDATE_TYPE);
        }
        if (StringUtils.isBlank(record.getSyncRate())) {
            record.setSyncRate(DatasourceTaskServer.ScheduleType.RIGHTNOW.name());
        }
        if (record.getStartTime() == null || record.getStartTime() <= 0) {
            record.setStartTime(System.currentTimeMillis());
        }
        if (StringUtils.equalsIgnoreCase(record.getSyncRate(), DatasourceTaskServer.ScheduleType.SIMPLE_CRON.name())) {
            record.setCron(simpleCron(record));
        }
        if (!StringUtils.equalsIgnoreCase(record.getSyncRate(), DatasourceTaskServer.ScheduleType.RIGHTNOW.name())
                && StringUtils.isBlank(record.getCron())) {
            DEException.throwException("cron can not be empty");
        }
        if (StringUtils.isBlank(record.getTaskStatus())) {
            record.setTaskStatus(TaskStatus.WaitingForExecution.name());
        }
        if (record.getCacheReady() == null) {
            record.setCacheReady(0);
        }
        return record;
    }

    private String simpleCron(CoreDatasetSyncTask task) {
        long value = Objects.requireNonNullElse(task.getSimpleCronValue(), 30L);
        String type = StringUtils.defaultIfBlank(task.getSimpleCronType(), "minute");
        if (StringUtils.equalsIgnoreCase(type, "hour")) {
            value = Math.max(1, Math.min(value, 23));
            return "0 0 0/" + value + " * * ? *";
        }
        if (StringUtils.equalsIgnoreCase(type, "day")) {
            value = Math.max(1, Math.min(value, 31));
            return "0 0 0 1/" + value + " * ? *";
        }
        value = Math.max(1, Math.min(value, 59));
        return "0 0/" + value + " * * * ? *";
    }

    private DatasetSyncTaskDTO toDTO(CoreDatasetSyncTask task) {
        DatasetSyncTaskDTO dto = new DatasetSyncTaskDTO();
        BeanUtils.copyBean(dto, task);
        return dto;
    }

    private DatasetSyncLogDTO toLogDTO(CoreDatasetSyncTaskLog log) {
        DatasetSyncLogDTO dto = new DatasetSyncLogDTO();
        BeanUtils.copyBean(dto, log);
        return dto;
    }

    private String jobGroup(String datasetGroupId) {
        return JOB_GROUP_PREFIX + datasetGroupId;
    }

    private void recoverIfStale(CoreDatasetSyncTask task) {
        CoreDatasetSyncTask latest = selectById(task.getId());
        if (latest == null || !StringUtils.equalsIgnoreCase(latest.getTaskStatus(), TaskStatus.UnderExecution.name())) {
            return;
        }
        Long runningTime = latest.getHeartbeatTime() != null ? latest.getHeartbeatTime() : latest.getLastExecTime();
        long runningAt = runningTime == null ? 0L : runningTime;
        if (runningAt == 0 || System.currentTimeMillis() - runningAt <= RUNNING_TASK_STALE_MILLIS) {
            return;
        }
        markRecovered(latest, "数据集同步任务超过运行心跳阈值，已自动恢复");
    }

    private void markRecovered(CoreDatasetSyncTask task, String message) {
        CoreDatasetSyncTask record = new CoreDatasetSyncTask();
        record.setTaskStatus(TaskStatus.WaitingForExecution.name());
        record.setLastExecStatus(TaskStatus.Error.name());
        record.setUpdateTime(System.currentTimeMillis());
        UpdateWrapper<CoreDatasetSyncTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", task.getId());
        wrapper.eq("task_status", TaskStatus.UnderExecution.name());
        taskMapper.update(record, wrapper);

        CoreDatasetSyncTaskLog logRecord = new CoreDatasetSyncTaskLog();
        logRecord.setTaskStatus(TaskStatus.Error.name());
        logRecord.setEndTime(System.currentTimeMillis());
        logRecord.setInfo(message);
        UpdateWrapper<CoreDatasetSyncTaskLog> logWrapper = new UpdateWrapper<>();
        logWrapper.eq("task_id", task.getId());
        logWrapper.eq("task_status", TaskStatus.UnderExecution.name());
        logMapper.update(logRecord, logWrapper);
    }

    private void dropCacheTable(String tableName) {
        try {
            CoreDeEngine engine = engineManage.info();
            EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
            EngineRequest request = new EngineRequest();
            request.setEngine(engine);
            request.setQuery(engineProvider.dropTable(tableName));
            calciteProvider.exec(request);
        } catch (Exception e) {
            LogUtil.warn("Drop dataset sync cache table failed: " + tableName + ", " + e.getMessage());
        }
    }
}
