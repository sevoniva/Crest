package io.crest.substitute.permissions.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.crest.api.permissions.user.dto.EnableSwitchRequest;
import io.crest.api.permissions.user.dto.UserCreator;
import io.crest.api.permissions.user.dto.UserEditor;
import io.crest.api.permissions.user.dto.UserGridRequest;
import io.crest.api.permissions.user.vo.CurUserVO;
import io.crest.api.permissions.user.vo.UserFormVO;
import io.crest.api.permissions.user.vo.UserGridVO;
import io.crest.api.permissions.user.vo.UserGridRoleItem;
import io.crest.exception.DEException;
import io.crest.substitute.permissions.user.model.CrestUser;
import io.crest.substitute.permissions.user.model.SsoUserProfile;
import io.crest.system.sso.SsoIdentityAction;
import io.crest.system.sso.SsoIdentityDecision;
import io.crest.system.sso.SsoIdentityProfile;
import io.crest.utils.IDUtils;
import io.crest.utils.Md5Utils;
import io.crest.utils.PasswordEncoder;
import io.crest.utils.PasswordValidator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component("crestUserManage")
public class CrestUserManage {

    public static final String AUTH_TYPE_LOCAL = "LOCAL";
    public static final String AUTH_TYPE_SSO = "SSO";

    private static final String INITIAL_PASSWORD_PROPERTY = "crest.user.initial-password";
    private static final String LEGACY_ADMIN_PASSWORD_HASH = "21232f297a57a5a743894a0e4a801fc3";
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[A-Za-z0-9._@-]{1,64}$");
    private static final Pattern UNSAFE_DISPLAY_NAME_PATTERN = Pattern.compile("[<>\\p{Cntrl}]");

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Value("${crest.user.initial-password:}")
    private String configuredInitialPassword;

    private final RowMapper<CrestUser> rowMapper = (rs, rowNum) -> {
        CrestUser user = new CrestUser();
        user.setId(rs.getLong("id"));
        user.setAccount(rs.getString("account"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhonePrefix(rs.getString("phone_prefix"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEnable(rs.getBoolean("enable"));
        user.setAdmin(rs.getBoolean("is_admin"));
        user.setOrigin(rs.getInt("origin"));
        user.setAuthType(rs.getString("auth_type"));
        user.setExternalId(rs.getString("external_id"));
        long lastLoginTime = rs.getLong("last_login_time");
        user.setLastLoginTime(rs.wasNull() ? null : lastLoginTime);
        user.setCreateTime(rs.getLong("create_time"));
        user.setUpdateTime(rs.getLong("update_time"));
        return user;
    };

    @PostConstruct
    public void initAdmin() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM crest_user WHERE id = 1", Integer.class);
        if (count == null || count == 0) {
            long now = System.currentTimeMillis();
            jdbcTemplate.update("""
                    INSERT INTO crest_user(id, account, name, password_hash, enable, is_admin, origin, create_time, update_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, 1L, "admin", "管理员", PasswordEncoder.encode(initialPassword()), true, true, 0, now, now);
        } else {
            String passwordHash = jdbcTemplate.queryForObject(
                    "SELECT password_hash FROM crest_user WHERE id = 1", String.class);
            if (Strings.CS.equals(passwordHash, LEGACY_ADMIN_PASSWORD_HASH)) {
                jdbcTemplate.update("UPDATE crest_user SET password_hash = ?, is_admin = 1, enable = 1, update_time = ? WHERE id = 1",
                        PasswordEncoder.encode(initialPassword()), System.currentTimeMillis());
            } else {
                jdbcTemplate.update("UPDATE crest_user SET is_admin = 1, enable = 1 WHERE id = 1");
            }
        }
    }

    public CrestUser queryByAccount(String account) {
        List<CrestUser> users = jdbcTemplate.query("SELECT * FROM crest_user WHERE account = ? LIMIT 1", rowMapper, account);
        return users.isEmpty() ? null : users.get(0);
    }

    public CrestUser queryById(Long id) {
        List<CrestUser> users = jdbcTemplate.query("SELECT * FROM crest_user WHERE id = ? LIMIT 1", rowMapper, id);
        return users.isEmpty() ? null : users.get(0);
    }

    public CrestUser queryByExternalId(String authType, String externalId) {
        List<CrestUser> users = jdbcTemplate.query("""
                SELECT * FROM crest_user
                WHERE auth_type = ? AND external_id = ?
                LIMIT 1
                """, rowMapper, authType, externalId);
        return users.isEmpty() ? null : users.get(0);
    }

    public String secretByUid(Long uid) {
        CrestUser user = queryById(uid);
        return user == null ? null : user.getPasswordHash();
    }

    public boolean passwordMatches(CrestUser user, String rawPassword) {
        if (user == null || rawPassword == null) {
            return false;
        }

        String storedHash = user.getPasswordHash();

        // 1. 尝试新格式（PBKDF2）
        if (storedHash != null && storedHash.contains(":")) {
            return PasswordEncoder.matches(rawPassword, storedHash);
        }

        // 2. 兼容旧格式（MD5）
        boolean matches = Strings.CS.equals(storedHash, Md5Utils.md5(rawPassword));

        // 3. 如果匹配且是旧格式，自动升级到新格式
        if (matches && PasswordEncoder.needsReEncoding(storedHash)) {
            String newHash = PasswordEncoder.encode(rawPassword);
            jdbcTemplate.update("UPDATE crest_user SET password_hash = ? WHERE id = ?",
                    newHash, user.getId());
        }

        return matches;
    }

    public boolean isAdmin(Long uid) {
        CrestUser user = queryById(uid);
        return user != null && Boolean.TRUE.equals(user.getAdmin());
    }

    @Transactional
    public CrestUser createOrUpdateSsoUser(SsoUserProfile profile, boolean autoCreateUser) {
        if (profile == null || StringUtils.isBlank(profile.getExternalId())) {
            DEException.throwException("单点登录用户唯一标识不能为空");
        }
        validate(profile.getAccount(), StringUtils.defaultIfBlank(profile.getName(), profile.getAccount()));
        String account = profile.getAccount().trim();
        String name = StringUtils.defaultIfBlank(profile.getName(), account).trim();
        String email = StringUtils.trimToNull(profile.getEmail());
        long now = System.currentTimeMillis();

        CrestUser user = queryByExternalId(AUTH_TYPE_SSO, profile.getExternalId());
        if (user == null) {
            user = queryByAccount(account);
        }
        if (user == null) {
            if (!autoCreateUser) {
                DEException.throwException("用户不存在，且未启用自动创建用户");
            }
            long id = IDUtils.snowID();
            jdbcTemplate.update("""
                    INSERT INTO crest_user(id, account, name, email, phone_prefix, phone, password_hash, enable, is_admin,
                        origin, auth_type, external_id, last_login_time, create_time, update_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, id, account, name, email, null, null, Md5Utils.md5(IDUtils.randomID(32)),
                    true, false, 2, AUTH_TYPE_SSO, profile.getExternalId(), now, now, now);
            return queryById(id);
        }
        if (Boolean.FALSE.equals(user.getEnable())) {
            DEException.throwException("用户已停用");
        }
        CrestUser sameAccount = queryByAccount(account);
        if (sameAccount != null && !sameAccount.getId().equals(user.getId())) {
            DEException.throwException("单点登录账号已被其他用户占用");
        }
        jdbcTemplate.update("""
                UPDATE crest_user
                SET account = ?, name = ?, email = ?, auth_type = ?, external_id = ?, origin = ?, last_login_time = ?, update_time = ?
                WHERE id = ?
                """, account, name, email, AUTH_TYPE_SSO, profile.getExternalId(), 2, now, now, user.getId());
        return queryById(user.getId());
    }

    @Transactional
    public CrestUser applySsoIdentity(SsoIdentityDecision decision) {
        SsoIdentityProfile profile = decision.getProfile();
        String account = profile.getAccount();
        String name = profile.getName();
        String email = StringUtils.trimToNull(profile.getEmail());
        long now = System.currentTimeMillis();
        if (SsoIdentityAction.CREATE_USER.equals(decision.getAction())) {
            long id = IDUtils.snowID();
            jdbcTemplate.update("""
                    INSERT INTO crest_user(id, account, name, email, phone_prefix, phone, password_hash, enable, is_admin,
                        origin, auth_type, external_id, last_login_time, create_time, update_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, id, account, name, email, null, null, Md5Utils.md5(IDUtils.randomID(32)),
                    true, false, 2, AUTH_TYPE_SSO, profile.getExternalSubject(), now, now, now);
            return queryById(id);
        }
        Long userId = decision.getUserId();
        jdbcTemplate.update("""
                UPDATE crest_user
                SET account = ?, name = ?, email = ?, auth_type = ?, external_id = ?, origin = ?, last_login_time = ?, update_time = ?
                WHERE id = ?
                """, account, name, email, AUTH_TYPE_SSO, profile.getExternalSubject(), 2, now, now, userId);
        return queryById(userId);
    }

    @Transactional
    public void markLoginSuccess(Long id) {
        if (id == null) return;
        long now = System.currentTimeMillis();
        jdbcTemplate.update("UPDATE crest_user SET last_login_time = ?, update_time = ? WHERE id = ?", now, now, id);
    }

    public IPage<UserGridVO> pager(int goPage, int pageSize, UserGridRequest request) {
        String keyword = request == null ? null : request.getKeyword();
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (StringUtils.isNotBlank(keyword)) {
            where.append(" AND (account LIKE ? OR name LIKE ? OR email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (request != null && request.getStatusList() != null && !request.getStatusList().isEmpty()) {
            where.append(" AND enable IN (");
            for (int i = 0; i < request.getStatusList().size(); i++) {
                if (i > 0) where.append(",");
                where.append("?");
                args.add(request.getStatusList().get(i));
            }
            where.append(")");
        }
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM crest_user" + where, Long.class, args.toArray());
        String order = request != null && Boolean.FALSE.equals(request.getTimeDesc()) ? " ASC" : " DESC";
        args.add(Math.max((goPage - 1) * pageSize, 0));
        args.add(pageSize);
        List<CrestUser> users = jdbcTemplate.query(
                "SELECT * FROM crest_user" + where + " ORDER BY create_time" + order + " LIMIT ?, ?",
                rowMapper,
                args.toArray());
        Page<UserGridVO> page = new Page<>(goPage, pageSize);
        page.setTotal(total == null ? 0 : total);
        page.setRecords(users.stream().map(this::toGrid).toList());
        return page;
    }

    @Transactional
    public Long create(UserCreator creator) {
        validate(creator.getAccount(), creator.getName());
        if (queryByAccount(creator.getAccount()) != null) {
            DEException.throwException("账号已存在");
        }
        long id = IDUtils.snowID();
        long now = System.currentTimeMillis();
        jdbcTemplate.update("""
                INSERT INTO crest_user(id, account, name, email, phone_prefix, phone, password_hash, enable, is_admin,
                    origin, auth_type, external_id, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, creator.getAccount().trim(), creator.getName().trim(), creator.getEmail(),
                creator.getPhonePrefix(), creator.getPhone(), PasswordEncoder.encode(initialPassword()),
                creator.getEnable() == null || creator.getEnable(), hasAdminRole(creator.getRoleIds()),
                0, AUTH_TYPE_LOCAL, null, now, now);
        return id;
    }

    @Transactional
    public void edit(UserEditor editor) {
        CrestUser user = queryById(editor.getId());
        if (user == null) {
            DEException.throwException("用户不存在");
        }
        validate(editor.getAccount(), editor.getName());
        if (AUTH_TYPE_SSO.equalsIgnoreCase(user.getAuthType()) && !Strings.CS.equals(user.getAccount(), editor.getAccount().trim())) {
            DEException.throwException("单点登录用户账号由身份提供方维护");
        }
        CrestUser sameAccount = queryByAccount(editor.getAccount());
        if (sameAccount != null && !sameAccount.getId().equals(editor.getId())) {
            DEException.throwException("账号已存在");
        }
        jdbcTemplate.update("""
                UPDATE crest_user
                SET account = ?, name = ?, email = ?, phone_prefix = ?, phone = ?, enable = ?, is_admin = ?, update_time = ?
                WHERE id = ?
                """, editor.getAccount().trim(), editor.getName().trim(), editor.getEmail(),
                editor.getPhonePrefix(), editor.getPhone(), editor.getEnable() == null || editor.getEnable(),
                editor.getId() == 1L || hasAdminRole(editor.getRoleIds()), System.currentTimeMillis(), editor.getId());
    }

    @Transactional
    public void delete(Long id) {
        if (id == 1L) {
            DEException.throwException("内置管理员不能删除");
        }
        jdbcTemplate.update("DELETE FROM crest_user WHERE id = ?", id);
    }

    @Transactional
    public void enable(EnableSwitchRequest request) {
        if (request.getId() == 1L && Boolean.FALSE.equals(request.getEnable())) {
            DEException.throwException("内置管理员不能停用");
        }
        jdbcTemplate.update("UPDATE crest_user SET enable = ?, update_time = ? WHERE id = ?",
                request.getEnable(), System.currentTimeMillis(), request.getId());
    }

    @Transactional
    public void resetPwd(Long id) {
        CrestUser user = queryById(id);
        if (user == null) {
            DEException.throwException("用户不存在");
        }
        if (AUTH_TYPE_SSO.equalsIgnoreCase(user.getAuthType())) {
            DEException.throwException("单点登录用户不支持重置本地密码");
        }
        jdbcTemplate.update("UPDATE crest_user SET password_hash = ?, update_time = ? WHERE id = ?",
                PasswordEncoder.encode(initialPassword()), System.currentTimeMillis(), id);
    }

    @Transactional
    public void modifyPwd(Long id, String oldPwd, String newPwd) {
        CrestUser user = queryById(id);
        if (user == null) {
            DEException.throwException("用户不存在");
        }
        if (AUTH_TYPE_SSO.equalsIgnoreCase(user.getAuthType())) {
            DEException.throwException("单点登录用户不支持修改本地密码");
        }
        if (!passwordMatches(user, oldPwd)) {
            DEException.throwException("原密码不正确");
        }

        // 使用密码策略验证器
        PasswordValidator.validate(newPwd);

        jdbcTemplate.update("UPDATE crest_user SET password_hash = ?, update_time = ? WHERE id = ?",
                PasswordEncoder.encode(newPwd), System.currentTimeMillis(), id);
    }

    public UserFormVO toForm(CrestUser user) {
        if (user == null) return null;
        UserFormVO vo = new UserFormVO();
        vo.setId(user.getId());
        vo.setAccount(user.getAccount());
        vo.setName(user.getName());
        vo.setEmail(user.getEmail());
        vo.setPhonePrefix(user.getPhonePrefix());
        vo.setPhone(user.getPhone());
        vo.setEnable(user.getEnable());
        vo.setOrigin(user.getOrigin());
        vo.setAuthType(user.getAuthType());
        vo.setExternalId(user.getExternalId());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setModel(AUTH_TYPE_SSO.equalsIgnoreCase(user.getAuthType()) ? "sso" : "local");
        vo.setRoleIds(Boolean.TRUE.equals(user.getAdmin()) ? List.of("1") : List.of("2"));
        return vo;
    }

    public UserGridVO toGrid(CrestUser user) {
        UserGridVO vo = new UserGridVO();
        vo.setId(user.getId());
        vo.setAccount(user.getAccount());
        vo.setName(user.getName());
        vo.setEmail(user.getEmail());
        vo.setPhonePrefix(user.getPhonePrefix());
        vo.setPhone(user.getPhone());
        vo.setEnable(user.getEnable());
        vo.setOrigin(user.getOrigin());
        vo.setAuthType(user.getAuthType());
        vo.setExternalId(user.getExternalId());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());
        UserGridRoleItem role = new UserGridRoleItem();
        role.setId(Boolean.TRUE.equals(user.getAdmin()) ? 1L : 2L);
        role.setName(Boolean.TRUE.equals(user.getAdmin()) ? "管理员" : "普通用户");
        vo.setRoleItems(List.of(role));
        return vo;
    }

    public CurUserVO toCurrent(CrestUser user) {
        if (user == null) return null;
        CurUserVO vo = new CurUserVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        vo.setOid(1L);
        vo.setLanguage("zh-CN");
        return vo;
    }

    private void validate(String account, String name) {
        if (StringUtils.isBlank(account)) {
            DEException.throwException("账号不能为空");
        }
        if (!ACCOUNT_PATTERN.matcher(account.trim()).matches()) {
            DEException.throwException("账号只支持 64 位以内的字母、数字、点、下划线、横线和 @");
        }
        if (StringUtils.isBlank(name)) {
            DEException.throwException("姓名不能为空");
        }
        String displayName = name.trim();
        if (displayName.length() > 64) {
            DEException.throwException("姓名不能超过 64 个字符");
        }
        if (UNSAFE_DISPLAY_NAME_PATTERN.matcher(displayName).find()) {
            DEException.throwException("姓名不能包含 HTML 标签或控制字符");
        }
    }

    private boolean hasAdminRole(List<Long> roleIds) {
        return roleIds != null && roleIds.stream().anyMatch(roleId -> Long.valueOf(1L).equals(roleId));
    }

    private String initialPassword() {
        if (StringUtils.isBlank(configuredInitialPassword)) {
            throw new IllegalStateException(INITIAL_PASSWORD_PROPERTY + " must be configured");
        }
        return configuredInitialPassword;
    }
}
