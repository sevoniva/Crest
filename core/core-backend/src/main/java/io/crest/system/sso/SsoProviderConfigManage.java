package io.crest.system.sso;

import io.crest.api.system.vo.SsoConfigVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SsoProviderConfigManage {

    @Resource
    private JdbcTemplate jdbcTemplate;

    public static SsoProviderConfig defaultProvider(SsoConfigVO config, String encryptedSecret) {
        SsoProviderConfig provider = new SsoProviderConfig();
        provider.setProviderId(1L);
        provider.setProviderKey("default");
        provider.setProviderType(SsoProviderType.fromConfig(config.getProviderType()));
        provider.setName(StringUtils.defaultIfBlank(config.getProviderName(), "统一身份认证"));
        provider.setEnabled(Boolean.TRUE.equals(config.getEnabled()));
        provider.setClientId(config.getClientId());
        provider.setClientSecret(encryptedSecret);
        provider.setAuthorizationEndpoint(config.getAuthorizationEndpoint());
        provider.setTokenEndpoint(config.getTokenEndpoint());
        provider.setUserInfoEndpoint(config.getUserInfoEndpoint());
        provider.setIssuer(config.getIssuer());
        provider.setScope(config.getScope());
        provider.setRedirectUri(config.getRedirectUri());
        provider.setUserIdAttribute(config.getUserIdAttribute());
        provider.setAccountAttribute(config.getAccountAttribute());
        provider.setNameAttribute(config.getNameAttribute());
        provider.setEmailAttribute(config.getEmailAttribute());
        provider.setUnionIdAttribute(config.getUnionIdAttribute());
        provider.setAutoCreateUser(Boolean.TRUE.equals(config.getAutoCreateUser()));
        provider.setRequireHttps(Boolean.TRUE.equals(config.getRequireHttps()));
        return provider;
    }

    @Transactional
    public void upsert(SsoProviderConfig provider) {
        long now = System.currentTimeMillis();
        jdbcTemplate.update("""
                INSERT INTO sso_provider(id, provider_key, provider_type, name, enabled, client_id, client_secret,
                    authorization_endpoint, token_endpoint, user_info_endpoint, issuer, scope, redirect_uri,
                    user_id_attribute, account_attribute, name_attribute, email_attribute, union_id_attribute,
                    auto_create_user, require_https, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    provider_type = VALUES(provider_type),
                    name = VALUES(name),
                    enabled = VALUES(enabled),
                    client_id = VALUES(client_id),
                    client_secret = VALUES(client_secret),
                    authorization_endpoint = VALUES(authorization_endpoint),
                    token_endpoint = VALUES(token_endpoint),
                    user_info_endpoint = VALUES(user_info_endpoint),
                    issuer = VALUES(issuer),
                    scope = VALUES(scope),
                    redirect_uri = VALUES(redirect_uri),
                    user_id_attribute = VALUES(user_id_attribute),
                    account_attribute = VALUES(account_attribute),
                    name_attribute = VALUES(name_attribute),
                    email_attribute = VALUES(email_attribute),
                    union_id_attribute = VALUES(union_id_attribute),
                    auto_create_user = VALUES(auto_create_user),
                    require_https = VALUES(require_https),
                    update_time = VALUES(update_time)
                """,
                provider.getProviderId(),
                provider.getProviderKey(),
                provider.getProviderType().name(),
                provider.getName(),
                provider.getEnabled(),
                provider.getClientId(),
                provider.getClientSecret(),
                provider.getAuthorizationEndpoint(),
                provider.getTokenEndpoint(),
                provider.getUserInfoEndpoint(),
                provider.getIssuer(),
                provider.getScope(),
                provider.getRedirectUri(),
                provider.getUserIdAttribute(),
                provider.getAccountAttribute(),
                provider.getNameAttribute(),
                provider.getEmailAttribute(),
                provider.getUnionIdAttribute(),
                provider.getAutoCreateUser(),
                provider.getRequireHttps(),
                now,
                now);
    }
}
