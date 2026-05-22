UPDATE `core_menu`
SET `menu_sort` = 5
WHERE `id` = 4 OR `name` = 'data';

INSERT INTO `core_menu` (`id`, `pid`, `type`, `name`, `component`, `menu_sort`, `icon`, `path`, `hidden`, `in_layout`, `auth`)
VALUES (66, 0, 2, 'association', 'visualized/data/lineage', 4, 'association', '/lineage', 0, 1, 1)
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
