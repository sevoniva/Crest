package io.dataease.api.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class DatasetSyncTaskDTO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long datasetGroupId;

    private String name;

    private String updateType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long incrementalFieldId;

    private String incrementalLastValue;

    private Long startTime;

    private String syncRate;

    private String cron;

    private Long simpleCronValue;

    private String simpleCronType;

    private Long endTime;

    private Long createTime;

    private Long updateTime;

    private Long lastExecTime;

    private Long heartbeatTime;

    private String lastExecStatus;

    private String taskStatus;

    private Integer cacheReady;

    private String schemaHash;
}
