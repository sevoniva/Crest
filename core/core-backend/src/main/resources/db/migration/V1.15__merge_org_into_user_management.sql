-- 组织管理并入用户管理，移除独立的组织管理菜单及其角色权限
DELETE FROM `crest_role_menu_permission` WHERE `menu_id` = 73;
DELETE FROM `core_menu` WHERE `id` = 73;
