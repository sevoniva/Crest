package io.crest.dataset.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("core_dataset_sync_task_log")
public class CoreDatasetSyncTaskLog implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long datasetGroupId;

    private Long taskId;

    private String updateType;

    private String tableName;

    private Long startTime;

    private Long endTime;

    private String taskStatus;

    private Long rowCount;

    private String info;

    private Long createTime;

    private String triggerType;
}
