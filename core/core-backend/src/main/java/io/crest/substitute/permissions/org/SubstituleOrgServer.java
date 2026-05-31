package io.crest.substitute.permissions.org;

import io.crest.api.permissions.org.api.OrgApi;
import io.crest.api.permissions.org.dto.OrgCreator;
import io.crest.api.permissions.org.dto.OrgEditor;
import io.crest.api.permissions.org.dto.OrgLazyRequest;
import io.crest.api.permissions.org.dto.OrgRequest;
import io.crest.api.permissions.org.vo.LazyMountedVO;
import io.crest.api.permissions.org.vo.LazyTreeVO;
import io.crest.api.permissions.org.vo.MountedVO;
import io.crest.api.permissions.org.vo.OrgDetailVO;
import io.crest.api.permissions.org.vo.OrgPageVO;
import io.crest.model.KeywordRequest;
import io.crest.substitute.permissions.auth.PlatformPermissionManage;
import io.crest.utils.AuthUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/org")
public class SubstituleOrgServer implements OrgApi {

    @Resource
    private CrestOrgManage crestOrgManage;

    @Resource
    private PlatformPermissionManage platformPermissionManage;

    @Override
    public List<OrgPageVO> pageTree(OrgRequest request) {
        platformPermissionManage.requireSystemAdmin();
        return crestOrgManage.pageTree(request);
    }

    @Override
    public LazyTreeVO lazyPageTree(OrgLazyRequest request) {
        platformPermissionManage.requireSystemAdmin();
        return crestOrgManage.lazyPageTree(request);
    }

    @Override
    public Long create(OrgCreator creator) {
        return crestOrgManage.create(creator);
    }

    @Override
    public void edit(OrgEditor editor) {
        crestOrgManage.edit(editor);
    }

    @Override
    public void delete(Long id) {
        crestOrgManage.delete(id);
    }

    @Override
    public List<MountedVO> mounted(KeywordRequest request) {
        Long uid = AuthUtils.getUser() == null ? 1L : AuthUtils.getUser().getUserId();
        String keyword = request == null ? null : request.getKeyword();
        return platformPermissionManage.mountedOrgs(uid, keyword);
    }

    @Override
    public LazyMountedVO lazyMounted(OrgLazyRequest request) {
        LazyMountedVO vo = new LazyMountedVO();
        vo.setNodes(mounted(request));
        vo.setName("组织");
        vo.setExpandKeyList(List.of(String.valueOf(PlatformPermissionManage.ROOT_ORG_ID)));
        return vo;
    }

    @Override
    public boolean resourceExist(Long oid) {
        return crestOrgManage.resourceExist(oid);
    }

    @Override
    public OrgDetailVO detail(Long oid) {
        return crestOrgManage.detail(oid);
    }

    @Override
    public List<String> subOrgs() {
        Long oid = AuthUtils.getUser() == null ? PlatformPermissionManage.ROOT_ORG_ID : AuthUtils.getUser().getDefaultOid();
        return crestOrgManage.subOrgs(oid);
    }
}
