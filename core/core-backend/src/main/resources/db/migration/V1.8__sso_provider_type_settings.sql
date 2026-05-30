INSERT INTO `core_sys_setting` (`id`, `pkey`, `pval`, `type`, `sort`)
SELECT
    100140000000000019,
    'sso.providerType',
    CASE
        WHEN EXISTS (
            SELECT 1 FROM (
                SELECT `pval` FROM `core_sys_setting`
                WHERE `pkey` IN ('sso.providerName', 'sso.issuer')
                  AND LOWER(`pval`) LIKE '%casdoor%'
            ) existing_provider
        ) THEN 'CASDOOR'
        ELSE 'OIDC_GENERIC'
    END,
    'text',
    19
WHERE NOT EXISTS (
    SELECT 1 FROM (
        SELECT `id` FROM `core_sys_setting` WHERE `pkey` = 'sso.providerType'
    ) existing_setting
);

INSERT INTO `core_sys_setting` (`id`, `pkey`, `pval`, `type`, `sort`)
SELECT 100140000000000020, 'sso.unionIdAttribute', '', 'text', 20
WHERE NOT EXISTS (
    SELECT 1 FROM (
        SELECT `id` FROM `core_sys_setting` WHERE `pkey` = 'sso.unionIdAttribute'
    ) existing_setting
);
