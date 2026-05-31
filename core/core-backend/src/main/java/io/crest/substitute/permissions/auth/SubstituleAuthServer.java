package io.crest.substitute.permissions.auth;

import io.crest.api.permissions.auth.api.AuthApi;
import io.crest.api.permissions.auth.dto.BusiPerEditor;
import io.crest.api.permissions.auth.dto.BusiPermissionRequest;
import io.crest.api.permissions.auth.dto.BusiTargetPerCreator;
import io.crest.api.permissions.auth.dto.MenuPerEditor;
import io.crest.api.permissions.auth.dto.MenuPermissionRequest;
import io.crest.api.permissions.auth.dto.MenuTargetPerCreator;
import io.crest.api.permissions.auth.vo.PermissionItem;
import io.crest.api.permissions.auth.vo.PermissionVO;
import io.crest.api.permissions.auth.vo.ResourceItemVO;
import io.crest.api.permissions.auth.vo.ResourceVO;
import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.log.DeLog;
import io.crest.utils.IDUtils;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class SubstituleAuthServer implements AuthApi {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private PlatformPermissionManage platformPermissionManage;

    @Override
    public List<ResourceVO> busiResource(String flag) {
        String type = normalizeFlag(flag);
        return jdbcTemplate.query("""
                SELECT id, resource_id, name
                FROM crest_resource_index
                WHERE resource_type = ?
                ORDER BY update_time DESC, id DESC
                """, (rs, rowNum) -> {
            ResourceVO vo = new ResourceVO();
            vo.setId(Long.parseLong(rs.getString("resource_id")));
            vo.setName(rs.getString("name"));
            vo.setLeaf(true);
            return vo;
        }, type);
    }

    @Override
    public PermissionVO busiPermission(BusiPermissionRequest request) {
        PermissionVO vo = new PermissionVO();
        vo.setRoot(false);
        vo.setReadonly(false);
        String targetType = targetType(request.getType());
        vo.setPermissions(jdbcTemplate.query("""
                SELECT CAST(resource_id AS UNSIGNED) AS resource_id, permission
                FROM crest_resource_permission
                WHERE target_type = ? AND target_id = ? AND resource_type = ?
                """, (rs, rowNum) -> {
            PermissionItem item = new PermissionItem();
            item.setId(rs.getLong("resource_id"));
            item.setWeight(platformPermissionManage.weightByPermission(rs.getString("permission")));
            return item;
        }, targetType, request.getId(), normalizeFlag(request.getFlag())));
        vo.setPermissionOrigins(List.of());
        return vo;
    }

    @Override
    public PermissionVO busiTargetPermission(BusiPermissionRequest request) {
        return busiPermission(request);
    }

    @Override
    public List<ResourceVO> menuResource() {
        List<ResourceVO> menus = jdbcTemplate.query("""
                SELECT id, pid, name, type
                FROM core_menu
                WHERE in_layout = 1
                ORDER BY menu_sort ASC, id ASC
                """, (rs, rowNum) -> {
            ResourceVO vo = new ResourceVO();
            vo.setId(rs.getLong("id"));
            vo.setName(rs.getString("name"));
            vo.setLeaf(rs.getInt("type") != 1);
            vo.setExtraFlag(rs.getInt("type"));
            return vo;
        });
        Map<Long, ResourceVO> byId = menus.stream().collect(Collectors.toMap(ResourceVO::getId, item -> item));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, pid FROM core_menu WHERE in_layout = 1");
        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            Long pid = ((Number) row.get("pid")).longValue();
            if (pid != 0 && byId.containsKey(pid) && byId.containsKey(id)) {
                ResourceVO parent = byId.get(pid);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(byId.get(id));
            }
        }
        return rows.stream()
                .filter(row -> ((Number) row.get("pid")).longValue() == 0L)
                .map(row -> byId.get(((Number) row.get("id")).longValue()))
                .toList();
    }

    @Override
    public PermissionVO menuPermission(MenuPermissionRequest request) {
        PermissionVO vo = new PermissionVO();
        vo.setRoot(false);
        vo.setReadonly(false);
        vo.setPermissions(platformPermissionManage.roleMenuPermissions(request.getId()));
        vo.setPermissionOrigins(List.of());
        return vo;
    }

    @Override
    public PermissionVO menuTargetPermission(MenuPermissionRequest request) {
        return menuPermission(request);
    }

    @Override
    @DeLog(ot = LogOT.AUTHORIZE, st = LogST.DATA, id = "#p0.id")
    public void saveBusiPer(BusiPerEditor editor) {
        platformPermissionManage.requireSystemAdmin();
        String targetType = targetType(editor.getType());
        jdbcTemplate.update("""
                DELETE FROM crest_resource_permission
                WHERE target_type = ? AND target_id = ? AND resource_type = ?
                """, targetType, editor.getId(), normalizeFlag(editor.getFlag()));
        if (editor.getPermissions() == null) {
            return;
        }
        for (PermissionItem item : editor.getPermissions()) {
            saveResourcePermission(normalizeFlag(editor.getFlag()), String.valueOf(item.getId()), targetType,
                    editor.getId(), platformPermissionManage.permissionByWeight(item.getWeight()));
        }
    }

    @Override
    @DeLog(ot = LogOT.AUTHORIZE, st = LogST.DATA)
    public void saveBusiTargetPer(BusiTargetPerCreator creator) {
        platformPermissionManage.requireSystemAdmin();
        if (creator.getPermissions() == null || creator.getIds() == null) {
            return;
        }
        String targetType = targetType(creator.getType());
        for (Long resourceId : creator.getIds()) {
            jdbcTemplate.update("""
                    DELETE FROM crest_resource_permission
                    WHERE resource_type = ? AND resource_id = ? AND target_type = ?
                    """, normalizeFlag(creator.getFlag()), String.valueOf(resourceId), targetType);
            for (PermissionItem item : creator.getPermissions()) {
                saveResourcePermission(normalizeFlag(creator.getFlag()), String.valueOf(resourceId), targetType,
                        item.getId(), platformPermissionManage.permissionByWeight(item.getWeight()));
            }
        }
    }

    @Override
    @DeLog(ot = LogOT.AUTHORIZE, st = LogST.MENU, id = "#p0.id")
    public void saveMenuPer(MenuPerEditor editor) {
        platformPermissionManage.requireSystemAdmin();
        jdbcTemplate.update("DELETE FROM crest_role_menu_permission WHERE rid = ?", editor.getId());
        if (editor.getPermissions() == null) {
            return;
        }
        for (PermissionItem item : editor.getPermissions()) {
            jdbcTemplate.update("""
                    INSERT IGNORE INTO crest_role_menu_permission(id, rid, menu_id, permission, create_time)
                    VALUES (?, ?, ?, ?, ?)
                    """, IDUtils.snowID(), editor.getId(), item.getId(),
                    platformPermissionManage.permissionByWeight(item.getWeight()), System.currentTimeMillis());
        }
    }

    @Override
    @DeLog(ot = LogOT.AUTHORIZE, st = LogST.MENU)
    public void saveMenuTargetPer(MenuTargetPerCreator creator) {
        platformPermissionManage.requireSystemAdmin();
        if (creator.getPermissions() == null || creator.getIds() == null) {
            return;
        }
        for (Long roleId : creator.getIds()) {
            MenuPerEditor editor = new MenuPerEditor();
            editor.setId(roleId);
            editor.setPermissions(creator.getPermissions());
            saveMenuPer(editor);
        }
    }

    @Override
    public List<ResourceItemVO> busiTargetPermissionAll(BusiPermissionRequest request) {
        String targetType = targetType(request.getType());
        if ("user".equals(targetType)) {
            return jdbcTemplate.query("""
                    SELECT DISTINCT u.id, u.account, u.name
                    FROM crest_user u
                    INNER JOIN crest_resource_permission p ON p.target_id = u.id AND p.target_type = 'user'
                    WHERE p.resource_type = ? AND p.resource_id = ?
                    ORDER BY u.name ASC, u.account ASC
                    """, (rs, rowNum) -> {
                ResourceItemVO vo = new ResourceItemVO();
                vo.setId(rs.getLong("id"));
                vo.setAccount(rs.getString("account"));
                vo.setName(rs.getString("name"));
                return vo;
            }, normalizeFlag(request.getFlag()), String.valueOf(request.getId()));
        }
        return jdbcTemplate.query("""
                SELECT DISTINCT r.id, r.code AS account, r.name
                FROM crest_role r
                INNER JOIN crest_resource_permission p ON p.target_id = r.id AND p.target_type = 'role'
                WHERE p.resource_type = ? AND p.resource_id = ?
                ORDER BY r.system_role DESC, r.name ASC
                """, (rs, rowNum) -> {
            ResourceItemVO vo = new ResourceItemVO();
            vo.setId(rs.getLong("id"));
            vo.setAccount(rs.getString("account"));
            vo.setName(rs.getString("name"));
            return vo;
        }, normalizeFlag(request.getFlag()), String.valueOf(request.getId()));
    }

    private void saveResourcePermission(String resourceType, String resourceId, String targetType, Long targetId, String permission) {
        jdbcTemplate.update("""
                INSERT IGNORE INTO crest_resource_permission(id, resource_type, resource_id, target_type, target_id, permission, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, IDUtils.snowID(), resourceType, resourceId, targetType, targetId, permission, System.currentTimeMillis());
    }

    private String targetType(Integer type) {
        return type != null && type == 1 ? "user" : "role";
    }

    private String normalizeFlag(String flag) {
        if ("dataV".equalsIgnoreCase(flag)) {
            return "screen";
        }
        if ("dashboard".equalsIgnoreCase(flag)) {
            return "panel";
        }
        return flag == null ? "panel" : flag;
    }
}
