UPDATE `core_menu`
SET `icon` = CASE `id`
    WHEN 73 THEN 'org'
    WHEN 74 THEN 'peoples'
    WHEN 75 THEN 'auth'
    ELSE `icon`
END
WHERE `id` IN (73, 74, 75);

INSERT IGNORE INTO `crest_role` (`id`, `oid`, `name`, `code`, `description`, `type_code`, `readonly`, `system_role`, `org_admin`, `create_time`, `update_time`)
VALUES (3, 1, '审计只读', 'auditor', '面向审计和巡检场景的只读角色', 3, 1, 1, 0, 1779664240000, 1779664240000);

UPDATE `crest_role`
SET `readonly` = 0
WHERE `id` = 2 AND `code` = 'member';

INSERT IGNORE INTO `crest_role_menu_permission` (`id`, `rid`, `menu_id`, `permission`, `create_time`)
SELECT id * 100 + 3, 3, id, 'read', 1779664240000
FROM `core_menu`
WHERE `auth` = 1 AND `id` NOT IN (15, 16, 64, 67, 68, 69, 71, 72, 73, 74, 75);
