CREATE TABLE IF NOT EXISTS `sso_provider`
(
    `id`            bigint       NOT NULL,
    `provider_key`  varchar(64)  NOT NULL,
    `provider_type` varchar(32)  NOT NULL,
    `name`          varchar(128) NOT NULL,
    `enabled`       tinyint(1)   NOT NULL DEFAULT 1,
    `create_time`   bigint       NOT NULL,
    `update_time`   bigint       NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sso_provider_key` (`provider_key`),
    KEY `idx_sso_provider_type` (`provider_type`, `enabled`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `sso_identity_binding`
(
    `id`               bigint       NOT NULL,
    `user_id`          bigint       NOT NULL,
    `provider_id`      bigint       NOT NULL,
    `provider_type`    varchar(32)  NOT NULL,
    `external_subject` varchar(191) NOT NULL,
    `account`          varchar(64)  NOT NULL,
    `display_name`     varchar(64)  NOT NULL,
    `email`            varchar(191) DEFAULT NULL,
    `union_id`         varchar(191) DEFAULT NULL,
    `last_login_time`  bigint       NOT NULL,
    `create_time`      bigint       NOT NULL,
    `update_time`      bigint       NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sso_identity_subject` (`provider_id`, `external_subject`),
    UNIQUE KEY `uk_sso_identity_union` (`provider_id`, `union_id`),
    KEY `idx_sso_identity_user` (`user_id`, `provider_id`),
    KEY `idx_sso_identity_account` (`account`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT IGNORE INTO `sso_provider` (`id`, `provider_key`, `provider_type`, `name`, `enabled`, `create_time`, `update_time`)
VALUES (1, 'default', 'OIDC_GENERIC', '统一身份认证', 1, 0, 0);
