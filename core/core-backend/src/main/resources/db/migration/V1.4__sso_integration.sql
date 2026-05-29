ALTER TABLE `core_sys_setting` MODIFY COLUMN `pval` text NOT NULL COMMENT '值';

ALTER TABLE `crest_user`
    ADD COLUMN `auth_type` varchar(32) NOT NULL DEFAULT 'LOCAL' COMMENT '认证类型' AFTER `origin`,
    ADD COLUMN `external_id` varchar(128) DEFAULT NULL COMMENT '外部身份ID' AFTER `auth_type`,
    ADD COLUMN `last_login_time` bigint DEFAULT NULL COMMENT '最近登录时间' AFTER `external_id`;

CREATE INDEX `idx_crest_user_auth_type` ON `crest_user` (`auth_type`);
CREATE UNIQUE INDEX `idx_crest_user_external` ON `crest_user` (`auth_type`, `external_id`);

INSERT INTO `core_menu` VALUES
    (71,15,2,'single-sign-on','system/sso',5,'authentication','/single-sign-on',0,1,1);

INSERT INTO `core_sys_setting` (`id`, `pkey`, `pval`, `type`, `sort`) VALUES
    (100140000000000001,'sso.enabled','false','text',1),
    (100140000000000002,'sso.providerName','企业单点登录','text',2),
    (100140000000000003,'sso.clientId','','text',3),
    (100140000000000004,'sso.clientSecret','','text',4),
    (100140000000000005,'sso.authorizationEndpoint','','text',5),
    (100140000000000006,'sso.tokenEndpoint','','text',6),
    (100140000000000007,'sso.userInfoEndpoint','','text',7),
    (100140000000000008,'sso.issuer','','text',8),
    (100140000000000009,'sso.scope','openid profile email','text',9),
    (100140000000000010,'sso.redirectUri','','text',10),
    (100140000000000011,'sso.userIdAttribute','sub','text',11),
    (100140000000000012,'sso.accountAttribute','preferred_username','text',12),
    (100140000000000013,'sso.nameAttribute','name','text',13),
    (100140000000000014,'sso.emailAttribute','email','text',14),
    (100140000000000015,'sso.autoCreateUser','true','text',15),
    (100140000000000016,'sso.allowLocalLogin','true','text',16),
    (100140000000000017,'sso.requireHttps','true','text',17),
    (100140000000000018,'sso.logoutRedirectUrl','','text',18);
