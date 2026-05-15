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

    private Integer fullSyncIntervalHours;

    private Long lastFullSyncTime;

    private Integer verifyEnabled;

    private Long lastVerifyTime;

    private String lastVerifyStatus;

    private String lastVerifyMessage;

    private Long lastSourceRowCount;

    private Long lastCacheRowCount;

    private Integer cacheExpireHours;

    private Integer taskTimeoutMinutes;

    private Integer consecutiveFailures;

    private Integer failureWarnThreshold;

    private Boolean cacheExpired;

    private Boolean failureWarned;
}
