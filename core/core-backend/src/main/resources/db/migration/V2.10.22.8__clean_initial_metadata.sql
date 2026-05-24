DELETE FROM `core_api_traffic`;
DELETE FROM `core_dataset_table_sql_log`;
DELETE FROM `core_datasource_task_log`;
DELETE FROM `core_export_download_task`;
DELETE FROM `core_export_task`;
DELETE FROM `core_opt_recent`;
DELETE FROM `core_store`;

DELETE FROM `snapshot_core_chart_view`
WHERE `scene_id` = 985192741891870720;
DELETE FROM `core_chart_view`
WHERE `scene_id` = 985192741891870720;

DELETE FROM `snapshot_data_visualization_info`
WHERE `id` IN ('985192741891870720', '985247460244983808');
DELETE FROM `data_visualization_info`
WHERE `id` IN ('985192741891870720', '985247460244983808');

DELETE FROM `core_copilot_config`;
DELETE FROM `core_copilot_msg`;
DELETE FROM `core_copilot_token`;

DELETE FROM `crest_user`
WHERE `id` <> 1;

UPDATE `crest_user`
SET
  `account` = 'admin',
  `name` = '管理员',
  `password_hash` = MD5('admin'),
  `enable` = 1,
  `is_admin` = 1,
  `update_time` = UNIX_TIMESTAMP() * 1000
WHERE `id` = 1;
