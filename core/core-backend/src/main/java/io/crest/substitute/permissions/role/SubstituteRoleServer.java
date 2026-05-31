package io.crest.substitute.permissions.role;

import io.crest.api.permissions.role.api.RoleApi;
import io.crest.api.permissions.role.dto.MountExternalUserRequest;
import io.crest.api.permissions.role.dto.MountUserRequest;
import io.crest.api.permissions.role.dto.RoleCopyRequest;
import io.crest.api.permissions.role.dto.RoleCreator;
import io.crest.api.permissions.role.dto.RoleEditor;
import io.crest.api.permissions.role.dto.RoleRequest;
import io.crest.api.permissions.role.dto.UnmountUserRequest;
import io.crest.api.permissions.role.vo.ExternalUserVO;
import io.crest.api.permissions.role.vo.RoleDetailVO;
import io.crest.api.permissions.role.vo.RoleVO;
import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.log.DeLog;
import io.crest.model.KeywordRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/role")
public class SubstituteRoleServer implements RoleApi {

    @Resource
    private CrestRoleManage crestRoleManage;

    @Override
    public List<RoleVO> query(KeywordRequest request) {
        return crestRoleManage.query(request == null ? null : request.getKeyword());
    }

    @Override
    @DeLog(ot = LogOT.CREATE, st = LogST.ROLE)
    public Long create(RoleCreator creator) {
        return crestRoleManage.create(creator);
    }

    @Override
    @DeLog(ot = LogOT.MODIFY, st = LogST.ROLE, id = "#p0.id")
    public void edit(RoleEditor editor) {
        crestRoleManage.edit(editor);
    }

    @Override
    @DeLog(ot = LogOT.BIND, st = LogST.ROLE, id = "#p0.rid")
    public void mountUser(MountUserRequest request) {
        crestRoleManage.mountUsers(request.getRid(), request.getUids());
    }

    @Override
    @DeLog(ot = LogOT.BIND, st = LogST.ROLE, id = "#p0.rid")
    public void mountExternalUser(MountExternalUserRequest request) {
        crestRoleManage.mountUsers(request.getRid(), List.of(request.getUid()));
    }

    @Override
    public ExternalUserVO searchExternalUser(String keyword) {
        return crestRoleManage.searchExternalUser(keyword);
    }

    @Override
    @DeLog(ot = LogOT.UNBIND, st = LogST.ROLE, id = "#p0.rid")
    public void unMountUser(UnmountUserRequest request) {
        crestRoleManage.unmountUser(request.getRid(), request.getUid());
    }

    @Override
    public List<RoleVO> optionForUser(RoleRequest request) {
        return crestRoleManage.query(request == null ? null : request.getKeyword());
    }

    @Override
    public List<RoleVO> selectedForUser(RoleRequest request) {
        if (request == null || request.getUid() == null) {
            return List.of();
        }
        return crestRoleManage.selectedForUser(request.getUid(), request.getKeyword());
    }

    @Override
    public RoleDetailVO detail(Long rid) {
        return crestRoleManage.detail(rid);
    }

    @Override
    @DeLog(ot = LogOT.DELETE, st = LogST.ROLE, id = "#p0")
    public void delete(Long rid) {
        crestRoleManage.delete(rid);
    }

    @Override
    public Integer beforeUnmountInfo(UnmountUserRequest request) {
        return crestRoleManage.beforeUnmountInfo(request.getRid(), request.getUid());
    }

    @Override
    @DeLog(ot = LogOT.CREATE, st = LogST.ROLE, id = "#p0.copyId")
    public void copy(RoleCopyRequest request) {
        RoleDetailVO detail = crestRoleManage.detail(request.getCopyId());
        RoleCreator creator = new RoleCreator();
        creator.setName(request.getName() == null ? detail.getName() + " 副本" : request.getName());
        creator.setDesc(request.getDesc() == null ? detail.getDesc() : request.getDesc());
        creator.setTypeCode(detail.getTypeCode());
        crestRoleManage.create(creator);
    }

    @Override
    public List<RoleVO> byCurOrg(KeywordRequest request) {
        return query(request);
    }

    @Override
    public List<RoleVO> queryWithOid(Long oid) {
        return crestRoleManage.queryByOid(oid, null);
    }
}
