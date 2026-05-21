INSERT INTO `core_menu` (`id`, `pid`, `type`, `name`, `component`, `menu_sort`, `icon`, `path`, `hidden`, `in_layout`, `auth`)
SELECT 66, 4, 2, 'association', 'visualized/data/lineage', 3, 'association', '/lineage', 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `core_menu` WHERE `id` = 66);
