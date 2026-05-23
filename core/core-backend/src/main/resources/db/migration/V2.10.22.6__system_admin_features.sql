CREATE TABLE IF NOT EXISTS `crest_user`
(
    `id`            bigint       NOT NULL COMMENT '用户ID',
    `account`       varchar(64)  NOT NULL COMMENT '账号',
    `name`          varchar(128) NOT NULL COMMENT '姓名',
    `email`         varchar(255)          DEFAULT NULL COMMENT '邮箱',
    `phone_prefix`  varchar(16)           DEFAULT NULL COMMENT '电话区号',
    `phone`         varchar(64)           DEFAULT NULL COMMENT '电话',
    `password_hash` varchar(64)  NOT NULL COMMENT '密码摘要',
    `enable`        tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否启用',
    `is_admin`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否管理员',
    `origin`        int          NOT NULL DEFAULT 0 COMMENT '用户来源',
    `create_time`   bigint       NOT NULL COMMENT '创建时间',
    `update_time`   bigint       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_crest_user_account` (`account`)
) COMMENT = 'Crest用户表';

SET @crest_user_is_admin_sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `crest_user` ADD COLUMN `is_admin` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否管理员''',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'crest_user'
    AND column_name = 'is_admin'
);
PREPARE crest_user_is_admin_stmt FROM @crest_user_is_admin_sql;
EXECUTE crest_user_is_admin_stmt;
DEALLOCATE PREPARE crest_user_is_admin_stmt;

INSERT INTO `crest_user` (`id`, `account`, `name`, `email`, `phone_prefix`, `phone`, `password_hash`, `enable`, `is_admin`, `origin`, `create_time`, `update_time`)
VALUES (1, 'admin', '管理员', NULL, NULL, NULL, MD5('admin'), 1, 1, 0, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `password_hash` = IF(`password_hash` IS NULL OR `password_hash` = '', VALUES(`password_hash`), `password_hash`),
  `enable` = 1,
  `is_admin` = 1,
  `update_time` = VALUES(`update_time`);

INSERT INTO `core_menu` (`id`, `pid`, `type`, `name`, `component`, `menu_sort`, `icon`, `path`, `hidden`, `in_layout`, `auth`)
VALUES
  (67, 15, 2, 'share-management', 'system/share', 2, 'icon_share-label_outlined', '/share-management', 0, 1, 1),
  (68, 15, 2, 'site-setting', 'system/site', 3, 'tab-title', '/site-setting', 0, 1, 1),
  (69, 15, 2, 'user-management', 'system/user', 4, 'icon_member_filled', '/user-management', 0, 1, 1)
ON DUPLICATE KEY UPDATE
  `pid` = VALUES(`pid`),
  `type` = VALUES(`type`),
  `name` = VALUES(`name`),
  `component` = VALUES(`component`),
  `menu_sort` = VALUES(`menu_sort`),
  `icon` = VALUES(`icon`),
  `path` = VALUES(`path`),
  `hidden` = VALUES(`hidden`),
  `in_layout` = VALUES(`in_layout`),
  `auth` = VALUES(`auth`);

INSERT INTO `core_sys_setting` (`id`, `pkey`, `pval`, `type`, `sort`)
SELECT 100102206800000001, 'basic.siteTitle', 'Crest', 'text', 9
WHERE NOT EXISTS (SELECT 1 FROM `core_sys_setting` WHERE `pkey` = 'basic.siteTitle');
