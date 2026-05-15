ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `heartbeat_time` bigint DEFAULT NULL COMMENT '运行中心跳时间';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `schema_hash` varchar(128) DEFAULT NULL COMMENT '数据集同步字段结构签名';

UPDATE `core_dataset_sync_task`
SET `heartbeat_time` = `last_exec_time`
WHERE `task_status` = 'UnderExecution'
  AND `heartbeat_time` IS NULL;
