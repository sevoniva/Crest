CREATE TABLE IF NOT EXISTS `crest_org` (
  `id` bigint NOT NULL COMMENT '组织ID',
  `pid` bigint DEFAULT NULL COMMENT '父组织ID',
  `name` varchar(128) NOT NULL COMMENT '组织名称',
  `code` varchar(64) DEFAULT NULL COMMENT '组织编码',
  `path` varchar(1024) NOT NULL COMMENT '组织路径',
  `level` int NOT NULL DEFAULT 0 COMMENT '层级',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `enable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `readonly` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否只读',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `update_time` bigint NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_crest_org_code` (`code`),
  KEY `idx_crest_org_pid` (`pid`),
  KEY `idx_crest_org_path` (`path`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织表';

CREATE TABLE IF NOT EXISTS `crest_role` (
  `id` bigint NOT NULL COMMENT '角色ID',
  `oid` bigint NOT NULL COMMENT '组织ID',
  `name` varchar(128) NOT NULL COMMENT '角色名称',
  `code` varchar(64) DEFAULT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `type_code` int NOT NULL DEFAULT 0 COMMENT '角色类型',
  `readonly` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否只读',
  `system_role` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否系统角色',
  `org_admin` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否组织管理员',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `update_time` bigint NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_crest_role_oid_code` (`oid`, `code`),
  KEY `idx_crest_role_oid` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `crest_user_org` (
  `id` bigint NOT NULL COMMENT 'ID',
  `uid` bigint NOT NULL COMMENT '用户ID',
  `oid` bigint NOT NULL COMMENT '组织ID',
  `default_org` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认组织',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_crest_user_org_uid_oid` (`uid`, `oid`),
  KEY `idx_crest_user_org_oid` (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户组织关系表';

CREATE TABLE IF NOT EXISTS `crest_user_role` (
  `id` bigint NOT NULL COMMENT 'ID',
  `uid` bigint NOT NULL COMMENT '用户ID',
  `oid` bigint NOT NULL COMMENT '组织ID',
  `rid` bigint NOT NULL COMMENT '角色ID',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_crest_user_role_uid_oid_rid` (`uid`, `oid`, `rid`),
  KEY `idx_crest_user_role_rid` (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关系表';

CREATE TABLE IF NOT EXISTS `crest_role_menu_permission` (
  `id` bigint NOT NULL COMMENT 'ID',
  `rid` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `permission` varchar(32) NOT NULL DEFAULT 'read' COMMENT '权限动作',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_crest_role_menu_perm` (`rid`, `menu_id`, `permission`),
  KEY `idx_crest_role_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单权限表';

CREATE TABLE IF NOT EXISTS `crest_resource_index` (
  `id` bigint NOT NULL COMMENT 'ID',
  `resource_id` varchar(64) NOT NULL COMMENT '业务资源ID',
  `resource_type` varchar(32) NOT NULL COMMENT '资源类型',
  `oid` bigint NOT NULL COMMENT '组织ID',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `name` varchar(255) DEFAULT NULL COMMENT '资源名称',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_crest_resource_unique` (`resource_type`, `resource_id`),
  KEY `idx_crest_resource_oid_type` (`oid`, `resource_type`),
  KEY `idx_crest_resource_creator` (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一资源索引表';

CREATE TABLE IF NOT EXISTS `crest_resource_permission` (
  `id` bigint NOT NULL COMMENT 'ID',
  `resource_type` varchar(32) NOT NULL COMMENT '资源类型',
  `resource_id` varchar(64) NOT NULL COMMENT '业务资源ID',
  `target_type` varchar(16) NOT NULL COMMENT '授权对象类型 user/role/org',
  `target_id` bigint NOT NULL COMMENT '授权对象ID',
  `permission` varchar(32) NOT NULL DEFAULT 'read' COMMENT '权限动作',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_crest_resource_permission` (`resource_type`, `resource_id`, `target_type`, `target_id`, `permission`),
  KEY `idx_crest_resource_permission_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资源授权表';

INSERT IGNORE INTO `crest_org` (`id`, `pid`, `name`, `code`, `path`, `level`, `sort`, `enable`, `readonly`, `create_time`, `update_time`)
VALUES (1, 0, '默认组织', 'default', '/1/', 0, 0, 1, 1, 1779664240000, 1779664240000);

INSERT IGNORE INTO `crest_role` (`id`, `oid`, `name`, `code`, `description`, `type_code`, `readonly`, `system_role`, `org_admin`, `create_time`, `update_time`)
VALUES
  (1, 1, '系统管理员', 'system_admin', '拥有全部系统管理和资源管理权限', 1, 1, 1, 1, 1779664240000, 1779664240000),
  (2, 1, '普通用户', 'member', '默认业务使用角色', 2, 1, 1, 0, 1779664240000, 1779664240000);

INSERT IGNORE INTO `core_menu` (`id`, `pid`, `type`, `name`, `component`, `menu_sort`, `icon`, `path`, `hidden`, `in_layout`, `auth`)
VALUES
  (73, 15, 2, 'org-management', 'system/org', 5, 'icon_org', '/org-management', 0, 1, 1),
  (74, 15, 2, 'role-management', 'system/role', 6, 'icon_member_outlined', '/role-management', 0, 1, 1),
  (75, 15, 2, 'permission-management', 'system/permission', 7, 'icon_authority', '/permission-management', 0, 1, 1);

INSERT IGNORE INTO `crest_user_org` (`id`, `uid`, `oid`, `default_org`, `create_time`)
SELECT id, id, 1, 1, COALESCE(create_time, 1779664240000) FROM `crest_user`;

INSERT IGNORE INTO `crest_user_role` (`id`, `uid`, `oid`, `rid`, `create_time`)
SELECT CAST(CONV(SUBSTR(MD5(CONCAT('user-role:', id, ':', CASE WHEN is_admin = 1 THEN 1 ELSE 2 END)), 1, 15), 16, 10) AS UNSIGNED),
       id, 1, CASE WHEN is_admin = 1 THEN 1 ELSE 2 END, COALESCE(create_time, 1779664240000)
FROM `crest_user`;

INSERT IGNORE INTO `crest_role_menu_permission` (`id`, `rid`, `menu_id`, `permission`, `create_time`)
SELECT id * 100 + 1, 1, id, 'manage', 1779664240000 FROM `core_menu` WHERE `auth` = 1 OR `id` IN (15, 16, 64, 67, 68, 69, 71, 72);

INSERT IGNORE INTO `crest_role_menu_permission` (`id`, `rid`, `menu_id`, `permission`, `create_time`)
SELECT id * 100 + 2, 2, id, 'read', 1779664240000 FROM `core_menu` WHERE `auth` = 1 AND `id` NOT IN (15, 16, 64, 67, 68, 69, 71, 72);

INSERT IGNORE INTO `crest_resource_index` (`id`, `resource_id`, `resource_type`, `oid`, `creator`, `name`, `create_time`, `update_time`)
SELECT CAST(CONV(SUBSTR(MD5(CONCAT('datasource:', id)), 1, 15), 16, 10) AS UNSIGNED), CAST(id AS CHAR), 'datasource', 1, CAST(create_by AS UNSIGNED), name, create_time, update_time
FROM `core_datasource` WHERE `type` != 'folder';

INSERT IGNORE INTO `crest_resource_index` (`id`, `resource_id`, `resource_type`, `oid`, `creator`, `name`, `create_time`, `update_time`)
SELECT CAST(CONV(SUBSTR(MD5(CONCAT('dataset:', id)), 1, 15), 16, 10) AS UNSIGNED), CAST(id AS CHAR), 'dataset', 1, CAST(create_by AS UNSIGNED), name, create_time, last_update_time
FROM `core_dataset_group` WHERE `node_type` = 'dataset';

INSERT IGNORE INTO `crest_resource_index` (`id`, `resource_id`, `resource_type`, `oid`, `creator`, `name`, `create_time`, `update_time`)
SELECT CAST(CONV(SUBSTR(MD5(CONCAT('visualization:', id)), 1, 15), 16, 10) AS UNSIGNED), CAST(id AS CHAR), CASE WHEN `type` = 'dataV' THEN 'screen' ELSE 'panel' END,
       COALESCE(CAST(NULLIF(org_id, '') AS UNSIGNED), 1), CAST(create_by AS UNSIGNED), name, create_time, update_time
FROM `data_visualization_info` WHERE `delete_flag` = 0 AND `node_type` = 'leaf';
