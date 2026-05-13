INSERT INTO `core_sys_setting`(`id`, `pkey`, `pval`, `type`, `sort`)
VALUES (1048232869488627719, 'basic.defaultSort', '1', 'text', 13);

INSERT INTO `core_sys_setting`(`id`, `pkey`, `pval`, `type`, `sort`)
VALUES (1048232869488627720, 'basic.defaultOpen', '0', 'text', 14);

INSERT INTO `core_menu` VALUES (70, 0, 1, 'msg', NULL, 200, NULL, '/msg', 1, 1, 0);

UPDATE `xpack_setting_authentication` set `synced` = 0 where `name` = 'oidc' or name = 'cas';

ALTER TABLE `data_visualization_info`
    ADD COLUMN `content_id` varchar(50) NULL DEFAULT '0' COMMENT '内容标识';

ALTER TABLE `visualization_link_jump_target_view_info`
    ADD COLUMN `target_type` varchar(50) NULL DEFAULT 'view' COMMENT '联动目标类型 view 图表 filter 过滤组件 outParams 外部参数';

ALTER TABLE `visualization_link_jump_target_view_info`
    MODIFY COLUMN `target_view_id` varchar(50) NULL DEFAULT NULL COMMENT '目标图表ID' AFTER `source_field_active_id`,
    MODIFY COLUMN `target_field_id` varchar(50) NULL DEFAULT NULL COMMENT '目标字段ID' AFTER `target_view_id`;

update visualization_link_jump_target_view_info set target_type = 'view';
ALTER TABLE `data_visualization_info`
    ADD COLUMN `check_version` varchar(50) NULL DEFAULT '1' COMMENT '内容检查标识';
update data_visualization_info set check_version = '1';

UPDATE `visualization_background` SET `name` = 'Board1' WHERE `id` = 'board_1';
UPDATE `visualization_background` SET `name` = 'Board2' WHERE `id` = 'board_2';
UPDATE `visualization_background` SET `name` = 'Board3' WHERE `id` = 'board_3';
UPDATE `visualization_background` SET `name` = 'Board4' WHERE `id` = 'board_4';
UPDATE `visualization_background` SET `name` = 'Board5' WHERE `id` = 'board_5';
UPDATE `visualization_background` SET `name` = 'Board6' WHERE `id` = 'board_6';
UPDATE `visualization_background` SET `name` = 'Board7' WHERE `id` = 'board_7';
UPDATE `visualization_background` SET `name` = 'Board8' WHERE `id` = 'board_8';
UPDATE `visualization_background` SET `name` = 'Board9' WHERE `id` = 'board_9';

DROP TABLE IF EXISTS `core_custom_geo_area`;
CREATE TABLE core_custom_geo_area
(
    id   varchar(50) not null comment 'id'
        primary key,
    name varchar(50) null comment '区域名称'
)
    comment '自定义地理区域';
DROP TABLE IF EXISTS `core_custom_geo_sub_area`;
create table core_custom_geo_sub_area
(
    id          bigint        not null comment 'id'
        primary key,
    name        varchar(50)   not null comment '名称',
    scope       varchar(1024) null comment '区域范围',
    geo_area_id varchar(50)   not null comment '自定义地理区域id'
)
    comment '自定义地理区域分区详情';


UPDATE `core_sys_setting` SET `sort` = 11 WHERE `pkey` = 'basic.dsIntervalTime';

