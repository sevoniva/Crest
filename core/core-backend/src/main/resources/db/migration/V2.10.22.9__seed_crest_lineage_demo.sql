SET NAMES utf8mb4;

DROP TABLE IF EXISTS `demo_lineage_refund`;
DROP TABLE IF EXISTS `demo_lineage_material_cost`;
DROP TABLE IF EXISTS `demo_lineage_order_item`;
DROP TABLE IF EXISTS `demo_lineage_order_main`;
DROP TABLE IF EXISTS `demo_lineage_coupon`;
DROP TABLE IF EXISTS `demo_lineage_member`;
DROP TABLE IF EXISTS `demo_lineage_product`;
DROP TABLE IF EXISTS `demo_lineage_store`;

CREATE TABLE `demo_lineage_store`
(
    `store_id`   varchar(32)  NOT NULL,
    `store_name` varchar(64)  NOT NULL,
    `region`     varchar(32)  NOT NULL,
    `city`       varchar(32)  NOT NULL,
    `channel`    varchar(32)  NOT NULL,
    `open_date`  date         NOT NULL,
    PRIMARY KEY (`store_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `demo_lineage_product`
(
    `product_id`     varchar(32)    NOT NULL,
    `product_name`   varchar(64)    NOT NULL,
    `category`       varchar(32)    NOT NULL,
    `list_price`     decimal(10, 2) NOT NULL,
    `recipe_version` varchar(32)    NOT NULL,
    PRIMARY KEY (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `demo_lineage_member`
(
    `member_id`        varchar(32) NOT NULL,
    `member_level`     varchar(32) NOT NULL,
    `register_channel` varchar(32) NOT NULL,
    `registered_at`    datetime    NOT NULL,
    PRIMARY KEY (`member_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `demo_lineage_coupon`
(
    `coupon_id`      varchar(32)    NOT NULL,
    `campaign_name`  varchar(64)    NOT NULL,
    `discount_type`  varchar(32)    NOT NULL,
    `discount_value` decimal(10, 2) NOT NULL,
    PRIMARY KEY (`coupon_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `demo_lineage_order_main`
(
    `order_id`     varchar(32)    NOT NULL,
    `order_date`   datetime       NOT NULL,
    `store_id`     varchar(32)    NOT NULL,
    `member_id`    varchar(32)    NOT NULL,
    `coupon_id`    varchar(32)    DEFAULT NULL,
    `order_status` varchar(32)    NOT NULL,
    `total_amount` decimal(10, 2) NOT NULL,
    `paid_amount`  decimal(10, 2) NOT NULL,
    PRIMARY KEY (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `demo_lineage_order_item`
(
    `item_id`         varchar(32)    NOT NULL,
    `order_id`        varchar(32)    NOT NULL,
    `product_id`      varchar(32)    NOT NULL,
    `quantity`        int            NOT NULL,
    `unit_price`      decimal(10, 2) NOT NULL,
    `discount_amount` decimal(10, 2) NOT NULL,
    PRIMARY KEY (`item_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `demo_lineage_material_cost`
(
    `product_id`     varchar(32)    NOT NULL,
    `material_name`  varchar(64)    NOT NULL,
    `unit_cost`      decimal(10, 2) NOT NULL,
    `cost_version`   varchar(32)    NOT NULL,
    PRIMARY KEY (`product_id`, `material_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `demo_lineage_refund`
(
    `refund_id`     varchar(32)    NOT NULL,
    `order_id`      varchar(32)    NOT NULL,
    `refund_amount` decimal(10, 2) NOT NULL,
    `refund_reason` varchar(64)    NOT NULL,
    `refund_time`   datetime       NOT NULL,
    PRIMARY KEY (`refund_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO `demo_lineage_store` VALUES
    ('S001', '南山万象店', '华南', '深圳', '直营', '2022-03-18'),
    ('S002', '天河城店', '华南', '广州', '直营', '2021-11-06'),
    ('S003', '湖滨银泰店', '华东', '杭州', '加盟', '2023-01-12'),
    ('S004', '徐家汇店', '华东', '上海', '直营', '2020-09-22'),
    ('S005', '朝阳大悦城店', '华北', '北京', '加盟', '2022-07-01');

INSERT INTO `demo_lineage_product` VALUES
    ('P001', '芝芝桃桃', '果茶', 28.00, 'R2026A'),
    ('P002', '生椰拿铁', '咖啡', 24.00, 'R2026B'),
    ('P003', '芋泥波波奶茶', '奶茶', 22.00, 'R2025C'),
    ('P004', '多肉葡萄', '果茶', 30.00, 'R2026A'),
    ('P005', '轻乳茶', '奶茶', 18.00, 'R2025D');

INSERT INTO `demo_lineage_member` VALUES
    ('M001', '黄金会员', '小程序', '2024-01-03 09:12:00'),
    ('M002', '普通会员', '门店收银', '2024-02-15 14:22:00'),
    ('M003', '黑金会员', '企业微信', '2023-12-20 11:31:00'),
    ('M004', '普通会员', '抖音团购', '2024-03-09 19:06:00'),
    ('M005', '黄金会员', '小程序', '2024-04-21 08:45:00');

INSERT INTO `demo_lineage_coupon` VALUES
    ('C001', '春季新品券', '满减', 5.00),
    ('C002', '会员复购券', '满减', 8.00),
    ('C003', '抖音团购券', '折扣', 0.85);

INSERT INTO `demo_lineage_order_main` VALUES
    ('O20260501001', '2026-05-01 10:15:00', 'S001', 'M001', 'C001', '已支付', 84.00, 79.00),
    ('O20260501002', '2026-05-01 12:40:00', 'S002', 'M002', NULL, '已支付', 48.00, 48.00),
    ('O20260502001', '2026-05-02 15:20:00', 'S003', 'M003', 'C002', '已支付', 90.00, 82.00),
    ('O20260502002', '2026-05-02 18:05:00', 'S004', 'M004', 'C003', '已退款', 60.00, 51.00),
    ('O20260503001', '2026-05-03 09:50:00', 'S005', 'M005', NULL, '已支付', 72.00, 72.00),
    ('O20260503002', '2026-05-03 20:18:00', 'S001', 'M003', 'C002', '已支付', 110.00, 102.00);

INSERT INTO `demo_lineage_order_item` VALUES
    ('I001', 'O20260501001', 'P001', 2, 28.00, 3.00),
    ('I002', 'O20260501001', 'P003', 1, 22.00, 2.00),
    ('I003', 'O20260501002', 'P002', 2, 24.00, 0.00),
    ('I004', 'O20260502001', 'P004', 3, 30.00, 8.00),
    ('I005', 'O20260502002', 'P005', 2, 18.00, 3.00),
    ('I006', 'O20260502002', 'P002', 1, 24.00, 6.00),
    ('I007', 'O20260503001', 'P003', 2, 22.00, 0.00),
    ('I008', 'O20260503001', 'P001', 1, 28.00, 0.00),
    ('I009', 'O20260503002', 'P004', 2, 30.00, 4.00),
    ('I010', 'O20260503002', 'P002', 2, 24.00, 4.00);

INSERT INTO `demo_lineage_material_cost` VALUES
    ('P001', '桃肉/芝士/茶底', 10.20, 'COST2026Q2'),
    ('P002', '椰浆/咖啡豆/牛乳', 8.60, 'COST2026Q2'),
    ('P003', '芋泥/珍珠/茶底', 7.80, 'COST2026Q2'),
    ('P004', '葡萄/芝士/茶底', 11.40, 'COST2026Q2'),
    ('P005', '茶底/牛乳', 5.90, 'COST2026Q2');

INSERT INTO `demo_lineage_refund` VALUES
    ('R001', 'O20260502002', 20.00, '口味不符', '2026-05-02 19:10:00');

DELETE FROM `core_chart_view`
WHERE `id` BETWEEN 1880202600000400001 AND 1880202600000400008;
DELETE FROM `snapshot_core_chart_view`
WHERE `id` BETWEEN 1880202600000400001 AND 1880202600000400008;
DELETE FROM `data_visualization_info`
WHERE `id` IN ('988020260500001', '988020260500000');
DELETE FROM `snapshot_data_visualization_info`
WHERE `id` IN ('988020260500001', '988020260500000');
DELETE FROM `core_dataset_table_field`
WHERE `id` BETWEEN 1880202600000300001 AND 1880202600000399999;
DELETE FROM `core_dataset_table`
WHERE `id` BETWEEN 1880202600000200001 AND 1880202600000200008;
DELETE FROM `core_dataset_group`
WHERE `id` IN (1880202600000100000, 1880202600000100001);

INSERT INTO `core_dataset_group`
(`id`, `name`, `pid`, `level`, `node_type`, `type`, `mode`, `info`, `create_by`, `create_time`, `update_by`, `last_update_time`, `union_sql`, `is_cross`)
VALUES
    (1880202600000100000, 'Crest 内置示例', 0, 0, 'folder', NULL, 0, NULL, '1', 1779618367019, '1', 1779618367019, NULL, b'0'),
    (1880202600000100001, '茶饮经营全链路分析', 1880202600000100000, 0, 'dataset', 'union', 0, NULL, '1', 1779618367019, '1', 1779618367019,
     'SELECT * FROM s_a_985188400292302848.`demo_lineage_order_main`', b'0');

INSERT INTO `core_dataset_table`
(`id`, `name`, `table_name`, `datasource_id`, `dataset_group_id`, `type`, `info`)
VALUES
    (1880202600000200001, '订单主表', 'demo_lineage_order_main', 985188400292302848, 1880202600000100001, 'db', '{"table":"demo_lineage_order_main","sql":""}'),
    (1880202600000200002, '订单明细', 'demo_lineage_order_item', 985188400292302848, 1880202600000100001, 'db', '{"table":"demo_lineage_order_item","sql":""}'),
    (1880202600000200003, '门店档案', 'demo_lineage_store', 985188400292302848, 1880202600000100001, 'db', '{"table":"demo_lineage_store","sql":""}'),
    (1880202600000200004, '商品档案', 'demo_lineage_product', 985188400292302848, 1880202600000100001, 'db', '{"table":"demo_lineage_product","sql":""}'),
    (1880202600000200005, '会员档案', 'demo_lineage_member', 985188400292302848, 1880202600000100001, 'db', '{"table":"demo_lineage_member","sql":""}'),
    (1880202600000200006, '优惠券活动', 'demo_lineage_coupon', 985188400292302848, 1880202600000100001, 'db', '{"table":"demo_lineage_coupon","sql":""}'),
    (1880202600000200007, '退款记录', 'demo_lineage_refund', 985188400292302848, 1880202600000100001, 'db', '{"table":"demo_lineage_refund","sql":""}'),
    (1880202600000200008, '商品原料成本', 'demo_lineage_material_cost', 985188400292302848, 1880202600000100001, 'db', '{"table":"demo_lineage_material_cost","sql":""}');

INSERT INTO `core_dataset_table_field`
(`id`, `datasource_id`, `dataset_table_id`, `dataset_group_id`, `origin_name`, `name`, `description`, `field_short_name`, `group_type`, `type`, `de_type`, `de_extract_type`, `ext_field`, `checked`, `accuracy`)
VALUES
    (1880202600000300001, 985188400292302848, 1880202600000200001, 1880202600000100001, 'order_id', '订单ID', '订单主键，用于串联明细与退款。', 'f_order_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300002, 985188400292302848, 1880202600000200001, 1880202600000100001, 'order_date', '下单时间', '订单支付时间。', 'f_order_date', 'd', 'DATETIME', 1, 1, 0, 1, 0),
    (1880202600000300003, 985188400292302848, 1880202600000200001, 1880202600000100001, 'store_id', '门店ID', '订单所属门店。', 'f_order_store_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300004, 985188400292302848, 1880202600000200001, 1880202600000100001, 'member_id', '会员ID', '下单会员。', 'f_order_member_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300005, 985188400292302848, 1880202600000200001, 1880202600000100001, 'coupon_id', '优惠券ID', '订单使用的优惠券。', 'f_order_coupon_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300006, 985188400292302848, 1880202600000200001, 1880202600000100001, 'order_status', '订单状态', '订单履约状态。', 'f_order_status', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300007, 985188400292302848, 1880202600000200001, 1880202600000100001, 'total_amount', '订单应收金额', '优惠前订单金额。', 'f_total_amount', 'q', 'DECIMAL', 2, 2, 0, 1, 2),
    (1880202600000300008, 985188400292302848, 1880202600000200001, 1880202600000100001, 'paid_amount', '订单实收金额', '优惠后实收金额。', 'f_paid_amount', 'q', 'DECIMAL', 2, 2, 0, 1, 2),

    (1880202600000300101, 985188400292302848, 1880202600000200002, 1880202600000100001, 'item_id', '明细ID', '订单明细主键。', 'f_item_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300102, 985188400292302848, 1880202600000200002, 1880202600000100001, 'order_id', '明细订单ID', '明细所属订单。', 'f_item_order_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300103, 985188400292302848, 1880202600000200002, 1880202600000100001, 'product_id', '商品ID', '销售商品。', 'f_item_product_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300104, 985188400292302848, 1880202600000200002, 1880202600000100001, 'quantity', '销售杯数', '订单明细销售数量。', 'f_quantity', 'q', 'INT', 2, 2, 0, 1, 0),
    (1880202600000300105, 985188400292302848, 1880202600000200002, 1880202600000100001, 'unit_price', '成交单价', '明细成交单价。', 'f_unit_price', 'q', 'DECIMAL', 2, 2, 0, 1, 2),
    (1880202600000300106, 985188400292302848, 1880202600000200002, 1880202600000100001, 'discount_amount', '明细优惠金额', '分摊到明细的优惠。', 'f_discount_amount', 'q', 'DECIMAL', 2, 2, 0, 1, 2),

    (1880202600000300201, 985188400292302848, 1880202600000200003, 1880202600000100001, 'store_id', '门店档案ID', '门店档案主键。', 'f_store_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300202, 985188400292302848, 1880202600000200003, 1880202600000100001, 'store_name', '门店名称', '门店展示名称。', 'f_store_name', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300203, 985188400292302848, 1880202600000200003, 1880202600000100001, 'region', '大区', '门店所属大区。', 'f_region', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300204, 985188400292302848, 1880202600000200003, 1880202600000100001, 'city', '城市', '门店所在城市。', 'f_city', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300205, 985188400292302848, 1880202600000200003, 1880202600000100001, 'channel', '门店类型', '直营或加盟。', 'f_channel', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300206, 985188400292302848, 1880202600000200003, 1880202600000100001, 'open_date', '开业日期', '门店开业日期。', 'f_open_date', 'd', 'DATE', 1, 1, 0, 1, 0),

    (1880202600000300301, 985188400292302848, 1880202600000200004, 1880202600000100001, 'product_id', '商品档案ID', '商品档案主键。', 'f_product_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300302, 985188400292302848, 1880202600000200004, 1880202600000100001, 'product_name', '商品名称', '商品展示名称。', 'f_product_name', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300303, 985188400292302848, 1880202600000200004, 1880202600000100001, 'category', '商品品类', '商品所属品类。', 'f_category', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300304, 985188400292302848, 1880202600000200004, 1880202600000100001, 'list_price', '挂牌价', '商品标准价。', 'f_list_price', 'q', 'DECIMAL', 2, 2, 0, 1, 2),
    (1880202600000300305, 985188400292302848, 1880202600000200004, 1880202600000100001, 'recipe_version', '配方版本', '成本核算使用的配方版本。', 'f_recipe_version', 'd', 'VARCHAR', 0, 0, 0, 1, 0),

    (1880202600000300401, 985188400292302848, 1880202600000200005, 1880202600000100001, 'member_id', '会员档案ID', '会员档案主键。', 'f_member_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300402, 985188400292302848, 1880202600000200005, 1880202600000100001, 'member_level', '会员等级', '会员权益等级。', 'f_member_level', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300403, 985188400292302848, 1880202600000200005, 1880202600000100001, 'register_channel', '注册渠道', '会员来源渠道。', 'f_register_channel', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300404, 985188400292302848, 1880202600000200005, 1880202600000100001, 'registered_at', '注册时间', '会员注册时间。', 'f_registered_at', 'd', 'DATETIME', 1, 1, 0, 1, 0),

    (1880202600000300501, 985188400292302848, 1880202600000200006, 1880202600000100001, 'coupon_id', '券档案ID', '优惠券主键。', 'f_coupon_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300502, 985188400292302848, 1880202600000200006, 1880202600000100001, 'campaign_name', '活动名称', '营销活动名称。', 'f_campaign_name', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300503, 985188400292302848, 1880202600000200006, 1880202600000100001, 'discount_type', '优惠类型', '满减或折扣。', 'f_discount_type', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300504, 985188400292302848, 1880202600000200006, 1880202600000100001, 'discount_value', '优惠值', '券面值或折扣率。', 'f_discount_value', 'q', 'DECIMAL', 2, 2, 0, 1, 2),

    (1880202600000300601, 985188400292302848, 1880202600000200007, 1880202600000100001, 'refund_id', '退款ID', '退款记录主键。', 'f_refund_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300602, 985188400292302848, 1880202600000200007, 1880202600000100001, 'order_id', '退款订单ID', '退款所属订单。', 'f_refund_order_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300603, 985188400292302848, 1880202600000200007, 1880202600000100001, 'refund_amount', '退款金额', '实际退款金额。', 'f_refund_amount', 'q', 'DECIMAL', 2, 2, 0, 1, 2),
    (1880202600000300604, 985188400292302848, 1880202600000200007, 1880202600000100001, 'refund_reason', '退款原因', '退款原因分类。', 'f_refund_reason', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300605, 985188400292302848, 1880202600000200007, 1880202600000100001, 'refund_time', '退款时间', '退款发生时间。', 'f_refund_time', 'd', 'DATETIME', 1, 1, 0, 1, 0),

    (1880202600000300701, 985188400292302848, 1880202600000200008, 1880202600000100001, 'product_id', '成本商品ID', '成本记录所属商品。', 'f_cost_product_id', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300702, 985188400292302848, 1880202600000200008, 1880202600000100001, 'material_name', '原料包', '商品消耗的原料组合。', 'f_material_name', 'd', 'VARCHAR', 0, 0, 0, 1, 0),
    (1880202600000300703, 985188400292302848, 1880202600000200008, 1880202600000100001, 'unit_cost', '杯均原料成本', '单杯原料成本。', 'f_unit_cost', 'q', 'DECIMAL', 2, 2, 0, 1, 2),
    (1880202600000300704, 985188400292302848, 1880202600000200008, 1880202600000100001, 'cost_version', '成本版本', '成本测算版本。', 'f_cost_version', 'd', 'VARCHAR', 0, 0, 0, 1, 0);

INSERT INTO `core_dataset_table_field`
(`id`, `dataset_group_id`, `origin_name`, `name`, `description`, `field_short_name`, `group_type`, `type`, `de_type`, `de_extract_type`, `ext_field`, `checked`, `accuracy`, `date_format`, `date_format_type`)
VALUES
    (1880202600000390001, 1880202600000100001, '[1880202600000300104]*[1880202600000300105]-[1880202600000300106]', '明细销售额', '销售杯数 * 成交单价 - 明细优惠金额。', 'f_line_sales_amount', 'q', 'DECIMAL', 3, 3, 2, 1, 2, '', ''),
    (1880202600000390002, 1880202600000100001, '[1880202600000300008]-[1880202600000300603]', '净销售额', '订单实收金额 - 退款金额。无退款时按退款金额为空处理。', 'f_net_sales_amount', 'q', 'DECIMAL', 3, 3, 2, 1, 2, '', ''),
    (1880202600000390003, 1880202600000100001, '[1880202600000390001]-[1880202600000300104]*[1880202600000300703]', '毛利额', '明细销售额 - 销售杯数 * 杯均原料成本。', 'f_gross_profit_amount', 'q', 'DECIMAL', 3, 3, 2, 1, 2, '', ''),
    (1880202600000390004, 1880202600000100001, 'round([1880202600000390003]/nullif([1880202600000390001],0),4)', '毛利率', '毛利额 / 明细销售额。', 'f_gross_profit_rate', 'q', 'DECIMAL', 3, 3, 2, 1, 4, '', ''),
    (1880202600000390005, 1880202600000100001, 'round(sum([1880202600000300008])/count_distinct([1880202600000300001]),2)', '客单价', '订单实收金额求和 / 订单数去重计数。', 'f_avg_order_value', 'q', 'DECIMAL', 3, 3, 2, 1, 2, '', ''),
    (1880202600000390006, 1880202600000100001, 'round(sum([1880202600000300504])/sum([1880202600000300007]),4)', '优惠强度', '优惠值求和 / 订单应收金额求和。', 'f_discount_intensity', 'q', 'DECIMAL', 3, 3, 2, 1, 4, '', '');

UPDATE `core_dataset_table_field`
SET `dataease_name` = `field_short_name`
WHERE `dataset_group_id` = 1880202600000100001
  AND `dataease_name` IS NULL;

SET @root = JSON_OBJECT(
        'currentDs', JSON_OBJECT('id', '1880202600000200001', 'name', '订单主表', 'tableName', 'demo_lineage_order_main', 'datasourceId', '985188400292302848', 'type', 'db', 'info', '{"table":"demo_lineage_order_main","sql":""}'),
        'childrenDs', JSON_ARRAY(
                JSON_OBJECT(
                        'currentDs', JSON_OBJECT('id', '1880202600000200002', 'name', '订单明细', 'tableName', 'demo_lineage_order_item', 'datasourceId', '985188400292302848', 'type', 'db', 'info', '{"table":"demo_lineage_order_item","sql":""}'),
                        'childrenDs', JSON_ARRAY(
                                JSON_OBJECT(
                                        'currentDs', JSON_OBJECT('id', '1880202600000200004', 'name', '商品档案', 'tableName', 'demo_lineage_product', 'datasourceId', '985188400292302848', 'type', 'db', 'info', '{"table":"demo_lineage_product","sql":""}'),
                                        'childrenDs', JSON_ARRAY(
                                                JSON_OBJECT(
                                                        'currentDs', JSON_OBJECT('id', '1880202600000200008', 'name', '商品原料成本', 'tableName', 'demo_lineage_material_cost', 'datasourceId', '985188400292302848', 'type', 'db', 'info', '{"table":"demo_lineage_material_cost","sql":""}'),
                                                        'childrenDs', JSON_ARRAY(),
                                                        'unionToParent', JSON_OBJECT('unionType', 'left', 'parentDs', JSON_OBJECT('id', '1880202600000200004'), 'currentDs', JSON_OBJECT('id', '1880202600000200008'), 'unionFields', JSON_ARRAY(JSON_OBJECT('parentField', JSON_OBJECT('id', '1880202600000300301', 'name', '商品档案ID'), 'currentField', JSON_OBJECT('id', '1880202600000300701', 'name', '成本商品ID'))))
                                                )
                                        ),
                                        'unionToParent', JSON_OBJECT('unionType', 'left', 'parentDs', JSON_OBJECT('id', '1880202600000200002'), 'currentDs', JSON_OBJECT('id', '1880202600000200004'), 'unionFields', JSON_ARRAY(JSON_OBJECT('parentField', JSON_OBJECT('id', '1880202600000300103', 'name', '商品ID'), 'currentField', JSON_OBJECT('id', '1880202600000300301', 'name', '商品档案ID'))))
                                )
                        ),
                        'unionToParent', JSON_OBJECT('unionType', 'left', 'parentDs', JSON_OBJECT('id', '1880202600000200001'), 'currentDs', JSON_OBJECT('id', '1880202600000200002'), 'unionFields', JSON_ARRAY(JSON_OBJECT('parentField', JSON_OBJECT('id', '1880202600000300001', 'name', '订单ID'), 'currentField', JSON_OBJECT('id', '1880202600000300102', 'name', '明细订单ID'))))
                ),
                JSON_OBJECT(
                        'currentDs', JSON_OBJECT('id', '1880202600000200003', 'name', '门店档案', 'tableName', 'demo_lineage_store', 'datasourceId', '985188400292302848', 'type', 'db', 'info', '{"table":"demo_lineage_store","sql":""}'),
                        'childrenDs', JSON_ARRAY(),
                        'unionToParent', JSON_OBJECT('unionType', 'left', 'parentDs', JSON_OBJECT('id', '1880202600000200001'), 'currentDs', JSON_OBJECT('id', '1880202600000200003'), 'unionFields', JSON_ARRAY(JSON_OBJECT('parentField', JSON_OBJECT('id', '1880202600000300003', 'name', '门店ID'), 'currentField', JSON_OBJECT('id', '1880202600000300201', 'name', '门店档案ID'))))
                ),
                JSON_OBJECT(
                        'currentDs', JSON_OBJECT('id', '1880202600000200005', 'name', '会员档案', 'tableName', 'demo_lineage_member', 'datasourceId', '985188400292302848', 'type', 'db', 'info', '{"table":"demo_lineage_member","sql":""}'),
                        'childrenDs', JSON_ARRAY(),
                        'unionToParent', JSON_OBJECT('unionType', 'left', 'parentDs', JSON_OBJECT('id', '1880202600000200001'), 'currentDs', JSON_OBJECT('id', '1880202600000200005'), 'unionFields', JSON_ARRAY(JSON_OBJECT('parentField', JSON_OBJECT('id', '1880202600000300004', 'name', '会员ID'), 'currentField', JSON_OBJECT('id', '1880202600000300401', 'name', '会员档案ID'))))
                ),
                JSON_OBJECT(
                        'currentDs', JSON_OBJECT('id', '1880202600000200006', 'name', '优惠券活动', 'tableName', 'demo_lineage_coupon', 'datasourceId', '985188400292302848', 'type', 'db', 'info', '{"table":"demo_lineage_coupon","sql":""}'),
                        'childrenDs', JSON_ARRAY(),
                        'unionToParent', JSON_OBJECT('unionType', 'left', 'parentDs', JSON_OBJECT('id', '1880202600000200001'), 'currentDs', JSON_OBJECT('id', '1880202600000200006'), 'unionFields', JSON_ARRAY(JSON_OBJECT('parentField', JSON_OBJECT('id', '1880202600000300005', 'name', '优惠券ID'), 'currentField', JSON_OBJECT('id', '1880202600000300501', 'name', '券档案ID'))))
                ),
                JSON_OBJECT(
                        'currentDs', JSON_OBJECT('id', '1880202600000200007', 'name', '退款记录', 'tableName', 'demo_lineage_refund', 'datasourceId', '985188400292302848', 'type', 'db', 'info', '{"table":"demo_lineage_refund","sql":""}'),
                        'childrenDs', JSON_ARRAY(),
                        'unionToParent', JSON_OBJECT('unionType', 'left', 'parentDs', JSON_OBJECT('id', '1880202600000200001'), 'currentDs', JSON_OBJECT('id', '1880202600000200007'), 'unionFields', JSON_ARRAY(JSON_OBJECT('parentField', JSON_OBJECT('id', '1880202600000300001', 'name', '订单ID'), 'currentField', JSON_OBJECT('id', '1880202600000300602', 'name', '退款订单ID'))))
                )
        ),
        'unionToParent', JSON_OBJECT('unionType', 'left', 'unionFields', JSON_ARRAY()),
        'allChildCount', 7
            );

UPDATE `core_dataset_group`
SET `info` = CONCAT('[', @root, ']')
WHERE `id` = 1880202600000100001;
