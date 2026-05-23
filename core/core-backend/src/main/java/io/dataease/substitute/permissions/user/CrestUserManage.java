package io.dataease.substitute.permissions.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.permissions.user.dto.EnableSwitchRequest;
import io.dataease.api.permissions.user.dto.UserCreator;
import io.dataease.api.permissions.user.dto.UserEditor;
import io.dataease.api.permissions.user.dto.UserGridRequest;
import io.dataease.api.permissions.user.vo.CurUserVO;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.permissions.user.vo.UserGridVO;
import io.dataease.api.permissions.user.vo.UserGridRoleItem;
import io.dataease.exception.DEException;
import io.dataease.substitute.permissions.user.model.CrestUser;
import io.dataease.utils.IDUtils;
import io.dataease.utils.Md5Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component("crestUserManage")
public class CrestUserManage {

    private static final String DEFAULT_PASSWORD = "admin";

    @Resource
    private JdbcTemplate jdbcTemplate;

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
                    """, 1L, "admin", "管理员", Md5Utils.md5(DEFAULT_PASSWORD), true, true, 0, now, now);
        } else {
            jdbcTemplate.update("UPDATE crest_user SET is_admin = 1, enable = 1 WHERE id = 1");
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

    public String secretByUid(Long uid) {
        CrestUser user = queryById(uid);
        return user == null ? null : user.getPasswordHash();
    }

    public boolean passwordMatches(CrestUser user, String rawPassword) {
        return user != null && StringUtils.equals(user.getPasswordHash(), Md5Utils.md5(rawPassword));
    }

    public boolean isAdmin(Long uid) {
        CrestUser user = queryById(uid);
        return user != null && Boolean.TRUE.equals(user.getAdmin());
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
                INSERT INTO crest_user(id, account, name, email, phone_prefix, phone, password_hash, enable, is_admin, origin, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, creator.getAccount().trim(), creator.getName().trim(), creator.getEmail(),
                creator.getPhonePrefix(), creator.getPhone(), Md5Utils.md5(DEFAULT_PASSWORD),
                creator.getEnable() == null || creator.getEnable(), hasAdminRole(creator.getRoleIds()), 0, now, now);
        return id;
    }

    @Transactional
    public void edit(UserEditor editor) {
        CrestUser user = queryById(editor.getId());
        if (user == null) {
            DEException.throwException("用户不存在");
        }
        validate(editor.getAccount(), editor.getName());
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
        jdbcTemplate.update("UPDATE crest_user SET password_hash = ?, update_time = ? WHERE id = ?",
                Md5Utils.md5(DEFAULT_PASSWORD), System.currentTimeMillis(), id);
    }

    @Transactional
    public void modifyPwd(Long id, String oldPwd, String newPwd) {
        CrestUser user = queryById(id);
        if (!passwordMatches(user, oldPwd)) {
            DEException.throwException("原密码不正确");
        }
        if (StringUtils.length(newPwd) < 5 || StringUtils.length(newPwd) > 32) {
            DEException.throwException("密码长度为 5-32 位");
        }
        jdbcTemplate.update("UPDATE crest_user SET password_hash = ?, update_time = ? WHERE id = ?",
                Md5Utils.md5(newPwd), System.currentTimeMillis(), id);
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
        vo.setModel("local");
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
        if (StringUtils.isBlank(name)) {
            DEException.throwException("姓名不能为空");
        }
    }

    private boolean hasAdminRole(List<Long> roleIds) {
        return roleIds != null && roleIds.stream().anyMatch(roleId -> Long.valueOf(1L).equals(roleId));
    }
}
