package io.crest.substitute.permissions.auth;

import io.crest.api.permissions.auth.vo.PermissionItem;
import io.crest.api.permissions.org.vo.MountedVO;
import io.crest.api.permissions.role.vo.RoleVO;
import io.crest.api.permissions.user.vo.UserGridRoleItem;
import io.crest.exception.DEException;
import io.crest.utils.AuthUtils;
import io.crest.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PlatformPermissionManage {

    public static final long ROOT_ORG_ID = 1L;
    public static final long SYSTEM_ADMIN_ROLE_ID = 1L;
    public static final long MEMBER_ROLE_ID = 2L;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public Long defaultOrgId(Long uid) {
        if (uid == null) {
            return ROOT_ORG_ID;
        }
        List<Long> orgIds = jdbcTemplate.queryForList("""
                SELECT oid FROM crest_user_org
                WHERE uid = ?
                ORDER BY default_org DESC, create_time ASC
                LIMIT 1
                """, Long.class, uid);
        if (!orgIds.isEmpty()) {
            return orgIds.get(0);
        }
        bindUserToOrg(uid, ROOT_ORG_ID, true);
        bindUserToRole(uid, ROOT_ORG_ID, MEMBER_ROLE_ID);
        return ROOT_ORG_ID;
    }

    public List<Long> roleIds(Long uid, Long oid) {
        if (uid == null) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT rid FROM crest_user_role
                WHERE uid = ? AND oid = ?
                ORDER BY rid ASC
                """, Long.class, uid, oid == null ? defaultOrgId(uid) : oid);
    }

    public List<UserGridRoleItem> userRoleItems(Long uid) {
        List<UserGridRoleItem> roles = jdbcTemplate.query("""
                SELECT r.id, r.name
                FROM crest_role r
                INNER JOIN crest_user_role ur ON ur.rid = r.id
                WHERE ur.uid = ?
                ORDER BY r.system_role DESC, r.id ASC
                """, (rs, rowNum) -> {
            UserGridRoleItem item = new UserGridRoleItem();
            item.setId(rs.getLong("id"));
            item.setName(rs.getString("name"));
            return item;
        }, uid);
        return roles.isEmpty() ? defaultMemberRoleItem() : roles;
    }

    public List<String> userRoleIdStrings(Long uid) {
        return userRoleItems(uid).stream().map(item -> String.valueOf(item.getId())).toList();
    }

    public boolean isSystemAdmin(Long uid) {
        if (uid == null) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM crest_user_role
                WHERE uid = ? AND rid = ?
                """, Integer.class, uid, SYSTEM_ADMIN_ROLE_ID);
        return (count != null && count > 0) || AuthUtils.isSysAdmin(uid);
    }

    public boolean isOrgAdmin(Long uid, Long oid) {
        if (isSystemAdmin(uid)) {
            return true;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM crest_user_role ur
                INNER JOIN crest_role r ON r.id = ur.rid
                WHERE ur.uid = ? AND ur.oid = ? AND r.org_admin = 1
                """, Integer.class, uid, oid == null ? defaultOrgId(uid) : oid);
        return count != null && count > 0;
    }

    @Transactional
    public void replaceUserRoles(Long uid, List<Long> roleIds) {
        Long oid = defaultOrgId(uid);
        jdbcTemplate.update("DELETE FROM crest_user_role WHERE uid = ? AND oid = ?", uid, oid);
        List<Long> effectiveRoleIds = roleIds == null || roleIds.isEmpty() ? List.of(MEMBER_ROLE_ID) : roleIds;
        for (Long roleId : effectiveRoleIds) {
            bindUserToRole(uid, oid, roleId);
        }
    }

    @Transactional
    public void bindUserToOrg(Long uid, Long oid, boolean defaultOrg) {
        long now = System.currentTimeMillis();
        if (defaultOrg) {
            jdbcTemplate.update("UPDATE crest_user_org SET default_org = 0 WHERE uid = ?", uid);
        }
        jdbcTemplate.update("""
                INSERT IGNORE INTO crest_user_org(id, uid, oid, default_org, create_time)
                VALUES (?, ?, ?, ?, ?)
                """, IDUtils.snowID(), uid, oid, defaultOrg, now);
        if (defaultOrg) {
            jdbcTemplate.update("UPDATE crest_user_org SET default_org = 1 WHERE uid = ? AND oid = ?", uid, oid);
        }
    }

    @Transactional
    public void bindUserToRole(Long uid, Long oid, Long rid) {
        if (uid == null || oid == null || rid == null) {
            return;
        }
        jdbcTemplate.update("""
                INSERT IGNORE INTO crest_user_role(id, uid, oid, rid, create_time)
                VALUES (?, ?, ?, ?, ?)
                """, IDUtils.snowID(), uid, oid, rid, System.currentTimeMillis());
    }

    public List<RoleVO> roles(Long oid, String keyword) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, name, readonly, system_role FROM crest_role WHERE oid = ?");
        args.add(oid == null ? ROOT_ORG_ID : oid);
        if (StringUtils.isNotBlank(keyword)) {
            sql.append(" AND name LIKE ?");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY system_role DESC, id ASC");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            RoleVO vo = new RoleVO();
            vo.setId(rs.getLong("id"));
            vo.setName(rs.getString("name"));
            vo.setReadonly(rs.getBoolean("readonly"));
            vo.setRoot(rs.getBoolean("system_role"));
            return vo;
        }, args.toArray());
    }

    public List<MountedVO> mountedOrgs(Long uid, String keyword) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT o.id, o.name, o.readonly
                FROM crest_org o
                INNER JOIN crest_user_org uo ON uo.oid = o.id
                WHERE uo.uid = ? AND o.enable = 1
                """);
        args.add(uid);
        if (isSystemAdmin(uid)) {
            sql = new StringBuilder("SELECT id, name, readonly FROM crest_org WHERE enable = 1");
            args.clear();
        }
        if (StringUtils.isNotBlank(keyword)) {
            sql.append(" AND name LIKE ?");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY id ASC");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            MountedVO vo = new MountedVO();
            vo.setId(rs.getLong("id"));
            vo.setName(rs.getString("name"));
            vo.setReadOnly(rs.getBoolean("readonly"));
            return vo;
        }, args.toArray());
    }

    public String resourceScopeSql(String resourceType, String resourceIdColumn, String creatorColumn, String orgColumn) {
        Long uid = AuthUtils.getUser() == null ? null : AuthUtils.getUser().getUserId();
        Long oid = AuthUtils.getUser() == null ? ROOT_ORG_ID : AuthUtils.getUser().getDefaultOid();
        if (uid == null || isSystemAdmin(uid)) {
            return null;
        }
        String uidValue = String.valueOf(uid);
        String oidValue = String.valueOf(oid == null ? defaultOrgId(uid) : oid);
        String roleIds = roleIds(uid, oid).stream().map(String::valueOf).collect(Collectors.joining(","));
        if (StringUtils.isBlank(roleIds)) {
            roleIds = "-1";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("(").append(creatorColumn).append(" = '").append(uidValue).append("'");
        if (StringUtils.isNotBlank(orgColumn)) {
            sql.append(" OR ").append(orgColumn).append(" = '").append(oidValue).append("'");
        } else {
            sql.append(" OR EXISTS (SELECT 1 FROM crest_resource_index cri WHERE cri.resource_type = '")
                    .append(resourceType).append("' AND cri.resource_id = CAST(").append(resourceIdColumn)
                    .append(" AS CHAR) AND cri.oid = ").append(oidValue).append(")");
        }
        sql.append(" OR EXISTS (SELECT 1 FROM crest_resource_permission crp WHERE crp.resource_type = '")
                .append(resourceType).append("' AND crp.resource_id = CAST(").append(resourceIdColumn).append(" AS CHAR)")
                .append(" AND crp.permission IN ('read', 'manage') AND ((crp.target_type = 'user' AND crp.target_id = ")
                .append(uidValue).append(") OR (crp.target_type = 'role' AND crp.target_id IN (").append(roleIds)
                .append(")) OR (crp.target_type = 'org' AND crp.target_id = ").append(oidValue).append(")))");
        sql.append(")");
        return sql.toString();
    }

    public Set<String> permissionsFromWeight(int weight) {
        if (weight >= 7) {
            return Set.of("read", "manage");
        }
        if (weight > 0) {
            return Set.of("read");
        }
        return Set.of();
    }

    @Transactional
    public void upsertResource(String resourceType, String resourceId, Long oid, Long creator, String name, Long createTime, Long updateTime) {
        if (resourceType == null || resourceId == null) {
            return;
        }
        long now = System.currentTimeMillis();
        jdbcTemplate.update("""
                INSERT INTO crest_resource_index(id, resource_id, resource_type, oid, creator, name, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE oid = VALUES(oid), creator = VALUES(creator), name = VALUES(name), update_time = VALUES(update_time)
                """, IDUtils.snowID(), resourceId, resourceType, oid == null ? ROOT_ORG_ID : oid, creator,
                name, createTime == null ? now : createTime, updateTime == null ? now : updateTime);
    }

    @Transactional
    public void deleteResource(String resourceType, String resourceId) {
        jdbcTemplate.update("DELETE FROM crest_resource_index WHERE resource_type = ? AND resource_id = ?", resourceType, resourceId);
        jdbcTemplate.update("DELETE FROM crest_resource_permission WHERE resource_type = ? AND resource_id = ?", resourceType, resourceId);
    }

    public String permissionByWeight(int weight) {
        return weight >= 7 ? "manage" : "read";
    }

    public int weightByPermission(String permission) {
        return "manage".equalsIgnoreCase(permission) ? 7 : 1;
    }

    public void requireSystemAdmin() {
        Long uid = AuthUtils.getUser() == null ? null : AuthUtils.getUser().getUserId();
        if (!isSystemAdmin(uid)) {
            DEException.throwException("当前用户没有系统管理权限");
        }
    }

    public List<PermissionItem> roleMenuPermissions(Long rid) {
        return jdbcTemplate.query("""
                SELECT menu_id, MAX(CASE WHEN permission = 'manage' THEN 7 ELSE 1 END) AS weight
                FROM crest_role_menu_permission
                WHERE rid = ?
                GROUP BY menu_id
                """, (rs, rowNum) -> {
            PermissionItem item = new PermissionItem();
            item.setId(rs.getLong("menu_id"));
            item.setWeight(rs.getInt("weight"));
            return item;
        }, rid);
    }

    private List<UserGridRoleItem> defaultMemberRoleItem() {
        UserGridRoleItem role = new UserGridRoleItem();
        role.setId(MEMBER_ROLE_ID);
        role.setName("普通用户");
        return List.of(role);
    }
}
