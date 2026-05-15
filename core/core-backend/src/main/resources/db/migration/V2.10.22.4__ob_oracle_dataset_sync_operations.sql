ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `full_sync_interval_hours` int DEFAULT 24 COMMENT '增量任务周期性全量校准间隔小时，0表示关闭';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `last_full_sync_time` bigint DEFAULT NULL COMMENT '最近一次全量同步时间';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `verify_enabled` tinyint(1) DEFAULT 1 COMMENT '同步后是否执行源端与缓存对账';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `last_verify_time` bigint DEFAULT NULL COMMENT '最近一次对账时间';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `last_verify_status` varchar(50) DEFAULT NULL COMMENT '最近一次对账状态';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `last_verify_message` text DEFAULT NULL COMMENT '最近一次对账信息';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `last_source_row_count` bigint DEFAULT NULL COMMENT '最近一次源端行数';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `last_cache_row_count` bigint DEFAULT NULL COMMENT '最近一次缓存行数';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `cache_expire_hours` int DEFAULT 26 COMMENT '缓存过期提示阈值小时，0表示关闭';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `task_timeout_minutes` int DEFAULT 360 COMMENT '单次同步任务超时分钟，0表示关闭';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `consecutive_failures` int DEFAULT 0 COMMENT '连续失败次数';

ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `failure_warn_threshold` int DEFAULT 1 COMMENT '连续失败告警阈值，0表示关闭';

UPDATE `core_dataset_sync_task`
SET `last_full_sync_time` = `last_exec_time`
WHERE `cache_ready` = 1
  AND `last_exec_status` = 'Completed'
  AND `last_full_sync_time` IS NULL;
