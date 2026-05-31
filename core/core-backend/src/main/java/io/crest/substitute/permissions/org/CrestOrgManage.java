package io.crest.substitute.permissions.org;

import io.crest.api.permissions.org.dto.OrgCreator;
import io.crest.api.permissions.org.dto.OrgEditor;
import io.crest.api.permissions.org.dto.OrgLazyRequest;
import io.crest.api.permissions.org.dto.OrgRequest;
import io.crest.api.permissions.org.vo.LazyOrgTreeNode;
import io.crest.api.permissions.org.vo.LazyTreeVO;
import io.crest.api.permissions.org.vo.OrgDetailVO;
import io.crest.api.permissions.org.vo.OrgPageVO;
import io.crest.exception.DEException;
import io.crest.substitute.permissions.auth.PlatformPermissionManage;
import io.crest.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CrestOrgManage {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private PlatformPermissionManage platformPermissionManage;

    public List<OrgPageVO> pageTree(OrgRequest request) {
        List<OrgPageVO> orgs = jdbcTemplate.query("""
                SELECT id, pid, name, create_time, readonly
                FROM crest_org
                WHERE enable = 1
                ORDER BY sort ASC, id ASC
                """, (rs, rowNum) -> {
            OrgPageVO vo = new OrgPageVO();
            vo.setId(rs.getLong("id"));
            vo.setName(rs.getString("name"));
            vo.setCreateTime(rs.getLong("create_time"));
            vo.setReadOnly(rs.getBoolean("readonly"));
            return vo;
        });
        Map<Long, OrgPageVO> byId = orgs.stream().collect(Collectors.toMap(OrgPageVO::getId, item -> item));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, pid FROM crest_org WHERE enable = 1");
        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            Number pidNumber = (Number) row.get("pid");
            Long pid = pidNumber == null ? 0L : pidNumber.longValue();
            if (pid != 0 && byId.containsKey(pid) && byId.containsKey(id)) {
                OrgPageVO parent = byId.get(pid);
                if (parent.getChildren() == null) {
                    parent.setChildren(new java.util.ArrayList<>());
                }
                parent.getChildren().add(byId.get(id));
            }
        }
        return rows.stream()
                .filter(row -> ((Number) row.get("pid")).longValue() == 0L)
                .map(row -> byId.get(((Number) row.get("id")).longValue()))
                .toList();
    }

    public LazyTreeVO lazyPageTree(OrgLazyRequest request) {
        Long pid = request == null || request.getPid() == null ? 0L : request.getPid();
        LazyTreeVO vo = new LazyTreeVO();
        vo.setNodes(children(pid));
        vo.setExpandKeyList(List.of(String.valueOf(PlatformPermissionManage.ROOT_ORG_ID)));
        return vo;
    }

    @Transactional
    public Long create(OrgCreator creator) {
        platformPermissionManage.requireSystemAdmin();
        if (creator == null || StringUtils.isBlank(creator.getName())) {
            DEException.throwException("组织名称不能为空");
        }
        Long pid = creator.getPid() == null ? 0L : creator.getPid();
        String parentPath = "/";
        int level = 0;
        if (pid != 0L) {
            OrgDetailVO parent = detail(pid);
            if (parent == null) {
                DEException.throwException("上级组织不存在");
            }
            parentPath = parent.getRootPath();
            level = parentPath.split("/").length - 2;
        }
        long id = creator.getId() == null ? IDUtils.snowID() : creator.getId();
        long now = System.currentTimeMillis();
        jdbcTemplate.update("""
                INSERT INTO crest_org(id, pid, name, code, path, level, sort, enable, readonly, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, pid, creator.getName().trim(), null, parentPath + id + "/", level, 0, true, false, now, now);
        return id;
    }

    @Transactional
    public void edit(OrgEditor editor) {
        platformPermissionManage.requireSystemAdmin();
        if (editor == null || editor.getId() == null || StringUtils.isBlank(editor.getName())) {
            DEException.throwException("组织名称不能为空");
        }
        jdbcTemplate.update("UPDATE crest_org SET name = ?, update_time = ? WHERE id = ? AND readonly = 0",
                editor.getName().trim(), System.currentTimeMillis(), editor.getId());
    }

    @Transactional
    public void delete(Long id) {
        platformPermissionManage.requireSystemAdmin();
        if (id == null || id == PlatformPermissionManage.ROOT_ORG_ID) {
            DEException.throwException("默认组织不能删除");
        }
        if (resourceExist(id)) {
            DEException.throwException("组织下存在用户、子组织或资源，不能删除");
        }
        jdbcTemplate.update("DELETE FROM crest_org WHERE id = ? AND readonly = 0", id);
    }

    public boolean resourceExist(Long oid) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT
                  (SELECT COUNT(1) FROM crest_org WHERE pid = ?) +
                  (SELECT COUNT(1) FROM crest_user_org WHERE oid = ?) +
                  (SELECT COUNT(1) FROM crest_resource_index WHERE oid = ?)
                """, Integer.class, oid, oid, oid);
        return count != null && count > 0;
    }

    public OrgDetailVO detail(Long oid) {
        List<OrgDetailVO> list = jdbcTemplate.query("""
                SELECT id, name, pid, path FROM crest_org WHERE id = ? LIMIT 1
                """, (rs, rowNum) -> new OrgDetailVO(rs.getLong("id"), rs.getString("name"),
                rs.getLong("pid"), rs.getString("path")), oid);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<String> subOrgs(Long oid) {
        OrgDetailVO org = detail(oid);
        if (org == null) {
            return List.of();
        }
        return jdbcTemplate.queryForList("SELECT CAST(id AS CHAR) FROM crest_org WHERE path LIKE ?",
                String.class, org.getRootPath() + "%");
    }

    private List<LazyOrgTreeNode> children(Long pid) {
        return jdbcTemplate.query("""
                SELECT o.id, o.pid, o.name, o.create_time, o.readonly,
                       EXISTS(SELECT 1 FROM crest_org c WHERE c.pid = o.id) AS has_children
                FROM crest_org o
                WHERE o.pid = ? AND o.enable = 1
                ORDER BY o.sort ASC, o.id ASC
                """, (rs, rowNum) -> {
            LazyOrgTreeNode node = new LazyOrgTreeNode();
            node.setId(rs.getLong("id"));
            node.setPid(rs.getLong("pid"));
            node.setName(rs.getString("name"));
            node.setCreateTime(rs.getLong("create_time"));
            node.setReadOnly(rs.getBoolean("readonly"));
            node.setHasChildren(rs.getBoolean("has_children"));
            return node;
        }, pid);
    }
}
