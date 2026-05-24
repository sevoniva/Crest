SET @legacy_prefix = CONCAT('x', 'pack');

SET @old_table = CONCAT(@legacy_prefix, '_share');
SET @sql_text = (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = @old_table)
            AND NOT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'core_share'),
        CONCAT('RENAME TABLE `', @old_table, '` TO `core_share`'),
        'SELECT 1'
    )
);
PREPARE rename_stmt FROM @sql_text;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

SET @old_table = CONCAT(@legacy_prefix, '_setting_authentication');
SET @sql_text = (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = @old_table)
            AND NOT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'core_setting_authentication'),
        CONCAT('RENAME TABLE `', @old_table, '` TO `core_setting_authentication`'),
        'SELECT 1'
    )
);
PREPARE rename_stmt FROM @sql_text;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

SET @old_table = CONCAT(@legacy_prefix, '_platform_token');
SET @sql_text = (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = @old_table)
            AND NOT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'core_platform_token'),
        CONCAT('RENAME TABLE `', @old_table, '` TO `core_platform_token`'),
        'SELECT 1'
    )
);
PREPARE rename_stmt FROM @sql_text;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

SET @old_table = CONCAT(@legacy_prefix, '_threshold_info');
SET @sql_text = (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = @old_table)
            AND NOT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'core_threshold_info'),
        CONCAT('RENAME TABLE `', @old_table, '` TO `core_threshold_info`'),
        'SELECT 1'
    )
);
PREPARE rename_stmt FROM @sql_text;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

SET @old_table = CONCAT(@legacy_prefix, '_threshold_instance');
SET @sql_text = (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = @old_table)
            AND NOT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'core_threshold_instance'),
        CONCAT('RENAME TABLE `', @old_table, '` TO `core_threshold_instance`'),
        'SELECT 1'
    )
);
PREPARE rename_stmt FROM @sql_text;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

SET @old_table = CONCAT(@legacy_prefix, '_webhook');
SET @sql_text = (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = @old_table)
            AND NOT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'core_webhook'),
        CONCAT('RENAME TABLE `', @old_table, '` TO `core_webhook`'),
        'SELECT 1'
    )
);
PREPARE rename_stmt FROM @sql_text;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

SET @old_table = CONCAT(@legacy_prefix, '_plugin');
SET @sql_text = (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = @old_table)
            AND NOT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'core_plugin'),
        CONCAT('RENAME TABLE `', @old_table, '` TO `core_plugin`'),
        'SELECT 1'
    )
);
PREPARE rename_stmt FROM @sql_text;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;
