CREATE TABLE IF NOT EXISTS `per_busi_resource`
(
    `id`     bigint NOT NULL,
    `pid`    bigint DEFAULT NULL,
    `org_id` bigint DEFAULT NULL,
    `rt_id`  int    DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_per_busi_resource_rt_id` (`rt_id`),
    KEY `idx_per_busi_resource_org_rt` (`org_id`, `rt_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `per_auth_busi_user`
(
    `id`            bigint NOT NULL,
    `oid`           bigint DEFAULT NULL,
    `uid`           bigint DEFAULT NULL,
    `resource_id`   bigint DEFAULT NULL,
    `resource_type` int    DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_per_auth_busi_user_uid` (`oid`, `uid`, `resource_type`),
    KEY `idx_per_auth_busi_user_resource` (`resource_id`, `resource_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `per_auth_busi_role`
(
    `id`            bigint NOT NULL,
    `rid`           bigint DEFAULT NULL,
    `resource_id`   bigint DEFAULT NULL,
    `resource_type` int    DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_per_auth_busi_role_rid` (`rid`, `resource_type`),
    KEY `idx_per_auth_busi_role_resource` (`resource_id`, `resource_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `per_user_role`
(
    `id`  bigint NOT NULL,
    `oid` bigint DEFAULT NULL,
    `uid` bigint DEFAULT NULL,
    `rid` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_per_user_role_uid` (`oid`, `uid`),
    KEY `idx_per_user_role_rid` (`rid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `per_role`
(
    `id`       bigint NOT NULL,
    `readonly` tinyint(1) DEFAULT 0,
    `pid`      bigint     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
