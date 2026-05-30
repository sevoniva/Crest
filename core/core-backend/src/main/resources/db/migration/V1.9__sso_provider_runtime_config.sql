ALTER TABLE `sso_provider`
    ADD COLUMN `client_id` varchar(191) DEFAULT NULL AFTER `enabled`,
    ADD COLUMN `client_secret` text DEFAULT NULL AFTER `client_id`,
    ADD COLUMN `authorization_endpoint` text DEFAULT NULL AFTER `client_secret`,
    ADD COLUMN `token_endpoint` text DEFAULT NULL AFTER `authorization_endpoint`,
    ADD COLUMN `user_info_endpoint` text DEFAULT NULL AFTER `token_endpoint`,
    ADD COLUMN `issuer` text DEFAULT NULL AFTER `user_info_endpoint`,
    ADD COLUMN `scope` varchar(255) DEFAULT NULL AFTER `issuer`,
    ADD COLUMN `redirect_uri` text DEFAULT NULL AFTER `scope`,
    ADD COLUMN `user_id_attribute` varchar(128) DEFAULT NULL AFTER `redirect_uri`,
    ADD COLUMN `account_attribute` varchar(128) DEFAULT NULL AFTER `user_id_attribute`,
    ADD COLUMN `name_attribute` varchar(128) DEFAULT NULL AFTER `account_attribute`,
    ADD COLUMN `email_attribute` varchar(128) DEFAULT NULL AFTER `name_attribute`,
    ADD COLUMN `union_id_attribute` varchar(128) DEFAULT NULL AFTER `email_attribute`,
    ADD COLUMN `auto_create_user` tinyint(1) NOT NULL DEFAULT 1 AFTER `union_id_attribute`,
    ADD COLUMN `require_https` tinyint(1) NOT NULL DEFAULT 1 AFTER `auto_create_user`;

UPDATE `sso_provider`
SET
    `provider_type` = COALESCE((SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.providerType' LIMIT 1), `provider_type`),
    `name` = COALESCE((SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.providerName' LIMIT 1), `name`),
    `enabled` = COALESCE((SELECT CASE WHEN LOWER(`pval`) = 'true' THEN 1 ELSE 0 END FROM `core_sys_setting` WHERE `pkey` = 'sso.enabled' LIMIT 1), `enabled`),
    `client_id` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.clientId' LIMIT 1),
    `client_secret` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.clientSecret' LIMIT 1),
    `authorization_endpoint` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.authorizationEndpoint' LIMIT 1),
    `token_endpoint` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.tokenEndpoint' LIMIT 1),
    `user_info_endpoint` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.userInfoEndpoint' LIMIT 1),
    `issuer` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.issuer' LIMIT 1),
    `scope` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.scope' LIMIT 1),
    `redirect_uri` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.redirectUri' LIMIT 1),
    `user_id_attribute` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.userIdAttribute' LIMIT 1),
    `account_attribute` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.accountAttribute' LIMIT 1),
    `name_attribute` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.nameAttribute' LIMIT 1),
    `email_attribute` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.emailAttribute' LIMIT 1),
    `union_id_attribute` = (SELECT `pval` FROM `core_sys_setting` WHERE `pkey` = 'sso.unionIdAttribute' LIMIT 1),
    `auto_create_user` = COALESCE((SELECT CASE WHEN LOWER(`pval`) = 'true' THEN 1 ELSE 0 END FROM `core_sys_setting` WHERE `pkey` = 'sso.autoCreateUser' LIMIT 1), `auto_create_user`),
    `require_https` = COALESCE((SELECT CASE WHEN LOWER(`pval`) = 'true' THEN 1 ELSE 0 END FROM `core_sys_setting` WHERE `pkey` = 'sso.requireHttps' LIMIT 1), `require_https`),
    `update_time` = UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000
WHERE `provider_key` = 'default';
