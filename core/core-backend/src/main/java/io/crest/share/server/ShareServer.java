package io.crest.share.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.crest.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.crest.api.share.ShareApi;
import io.crest.api.share.request.*;
import io.crest.api.share.vo.ShareGridVO;
import io.crest.api.share.vo.ShareProxyVO;
import io.crest.api.share.vo.ShareVO;
import io.crest.utils.BeanUtils;
import io.crest.share.dao.auto.entity.CoreShare;
import io.crest.share.manage.ShareManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/share")
@RestController
public class ShareServer implements ShareApi {

    @Resource(name = "shareManage")
    private ShareManage shareManage;
    @Override
    public boolean status(Long resourceId) {
        return ObjectUtils.isNotEmpty(shareManage.queryByResource(resourceId));
    }

    @Override
    public void switcher(Long resourceId) {
        shareManage.switcher(resourceId);
    }

    @Override
    public void editExp(ShareExpRequest request) {
        shareManage.editExp(request.getResourceId(), request.getExp());
    }

    @Override
    public void editPwd(SharePwdRequest request) {
        shareManage.editPwd(request.getResourceId(), request.getPwd(), request.getAutoPwd());
    }

    @Override
    public ShareVO detail(Long resourceId) {
        CoreShare coreShare = shareManage.queryByResource(resourceId);
        if (ObjectUtils.isEmpty(coreShare)) return null;
        return BeanUtils.copyBean(new ShareVO(), coreShare);
    }

    @Override
    public List<ShareGridVO> query(VisualizationWorkbranchQueryRequest request) {
        return shareManage.query(1, 20, request).getRecords();
    }

    @Override
    public IPage<ShareGridVO> pager(int goPage, int pageSize, VisualizationWorkbranchQueryRequest request) {
        return shareManage.query(goPage, pageSize, request);
    }

    @Override
    public ShareProxyVO proxyInfo(ShareProxyRequest request) {
        return shareManage.proxyInfo(request);
    }

    @Override
    public boolean validatePwd(SharePwdValidator validator) {
        return shareManage.validatePwd(validator);
    }

    @Override
    public Map<String, String> queryRelationByUserId(Long uid) {
        return shareManage.queryRelationByUserId(uid);
    }

    @Override
    public String editUuid(ShareUuidEditor editor) {
        return shareManage.editUuid(editor);
    }
}
