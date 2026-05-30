package io.crest.system.sso;

import io.crest.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class SsoIdentityBindingManage {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void upsertProvider(Long providerId, SsoProviderType providerType, String providerName) {
        long now = System.currentTimeMillis();
        jdbcTemplate.update("""
                INSERT INTO sso_provider(id, provider_key, provider_type, name, enabled, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    provider_type = VALUES(provider_type),
                    name = VALUES(name),
                    enabled = VALUES(enabled),
                    update_time = VALUES(update_time)
                """,
                providerId,
                providerId == 1L ? "default" : "provider-" + providerId,
                providerType.name(),
                StringUtils.defaultIfBlank(providerName, "统一身份认证"),
                true,
                now,
                now);
    }

    public Long boundUserId(SsoIdentityProfile profile) {
        List<Long> bySubject = jdbcTemplate.queryForList("""
                SELECT user_id FROM sso_identity_binding
                WHERE provider_id = ? AND external_subject = ?
                LIMIT 1
                """, Long.class, profile.getProviderId(), profile.getExternalSubject());
        if (!bySubject.isEmpty()) {
            return bySubject.get(0);
        }
        if (StringUtils.isBlank(profile.getUnionId())) {
            return null;
        }
        List<Long> byUnion = jdbcTemplate.queryForList("""
                SELECT user_id FROM sso_identity_binding
                WHERE provider_id = ? AND union_id = ?
                LIMIT 1
                """, Long.class, profile.getProviderId(), profile.getUnionId());
        return byUnion.isEmpty() ? null : byUnion.get(0);
    }

    @Transactional
    public void upsert(Long userId, SsoIdentityProfile profile) {
        long now = System.currentTimeMillis();
        jdbcTemplate.update("""
                INSERT INTO sso_identity_binding(id, user_id, provider_id, provider_type, external_subject,
                    account, display_name, email, union_id, last_login_time, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    user_id = VALUES(user_id),
                    provider_type = VALUES(provider_type),
                    external_subject = VALUES(external_subject),
                    account = VALUES(account),
                    display_name = VALUES(display_name),
                    email = VALUES(email),
                    union_id = VALUES(union_id),
                    last_login_time = VALUES(last_login_time),
                    update_time = VALUES(update_time)
                """,
                IDUtils.snowID(),
                userId,
                profile.getProviderId(),
                profile.getProviderType().name(),
                profile.getExternalSubject(),
                profile.getAccount(),
                profile.getName(),
                profile.getEmail(),
                StringUtils.trimToNull(profile.getUnionId()),
                now,
                now,
                now);
    }
}
