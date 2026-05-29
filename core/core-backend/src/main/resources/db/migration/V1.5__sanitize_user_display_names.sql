UPDATE crest_user
SET name = '管理员'
WHERE id = 1
  AND (name LIKE '%<%' OR name LIKE '%>%');

UPDATE crest_user
SET name = account
WHERE id <> 1
  AND (name LIKE '%<%' OR name LIKE '%>%');

UPDATE core_sys_setting
SET pval = '统一身份认证'
WHERE pkey = 'sso.providerName'
  AND pval = '企业单点登录';
