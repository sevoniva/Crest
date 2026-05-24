package io.crest.share.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.crest.api.system.vo.ShareBaseVO;
import io.crest.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.crest.api.share.request.ShareProxyRequest;
import io.crest.api.share.request.SharePwdValidator;
import io.crest.api.share.request.ShareUuidEditor;
import io.crest.api.share.vo.TicketValidVO;
import io.crest.api.share.vo.ShareGridVO;
import io.crest.api.share.vo.ShareProxyVO;
import io.crest.auth.bo.TokenUserBO;
import io.crest.constant.AuthConstant;
import io.crest.constant.BusiResourceEnum;
import io.crest.exception.DEException;
import io.crest.i18n.Translator;
import io.crest.share.dao.auto.entity.CoreShareTicket;
import io.crest.share.dao.auto.entity.CoreShare;
import io.crest.share.dao.auto.mapper.CoreShareTicketMapper;
import io.crest.share.dao.auto.mapper.CoreShareMapper;
import io.crest.share.dao.ext.mapper.ShareExtMapper;
import io.crest.share.dao.ext.po.SharePO;
import io.crest.share.util.LinkTokenUtil;
import io.crest.system.manage.SysParameterManage;
import io.crest.utils.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("shareManage")
public class ShareManage {

    @Resource(name = "coreShareMapper")
    private CoreShareMapper coreShareMapper;

    @Resource(name = "shareExtMapper")
    private ShareExtMapper shareExtMapper;

    @Resource
    private ShareTicketManage shareTicketManage;

    @Resource(name = "coreShareTicketMapper")
    private CoreShareTicketMapper coreShareTicketMapper;

    @Resource
    private SysParameterManage sysParameterManage;

    @Resource
    private ShareSecretManage shareSecretManage;

    public void deleteByResource(Long resourceId) {
        if (resourceId == null) {
            return;
        }
        QueryWrapper<CoreShare> wrapper = new QueryWrapper<>();
        wrapper.eq("resource_id", resourceId);
        List<CoreShare> shares = coreShareMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(shares)) {
            return;
        }
        coreShareMapper.delete(wrapper);

        List<String> uuidList = shares.stream().map(CoreShare::getUuid).collect(Collectors.toList());
        QueryWrapper<CoreShareTicket> ticketQueryWrapper = new QueryWrapper<>();
        ticketQueryWrapper.in("uuid", uuidList);
        coreShareTicketMapper.delete(ticketQueryWrapper);
    }

    public CoreShare queryByResource(Long resourceId) {
        Long userId = AuthUtils.getUser().getUserId();
        QueryWrapper<CoreShare> queryWrapper = new QueryWrapper<>();
        if (!CrestPermissionUtils.currentUserIsAdmin()) {
            queryWrapper.eq("creator", userId);
        }
        queryWrapper.eq("resource_id", resourceId);
        return coreShareMapper.selectOne(queryWrapper);
    }


    @Transactional
    public void switcher(Long resourceId) {
        CoreShare originData = queryByResource(resourceId);
        if (ObjectUtils.isNotEmpty(originData)) {
            coreShareMapper.deleteById(originData.getId());
            shareTicketManage.deleteByShare(originData.getUuid());
            return;
        }
        TokenUserBO user = AuthUtils.getUser();
        Long userId = user.getUserId();
        CoreShare coreShare = new CoreShare();
        coreShare.setId(IDUtils.snowID());
        coreShare.setCreator(userId);
        coreShare.setTime(System.currentTimeMillis());
        coreShare.setResourceId(resourceId);
        coreShare.setUuid(RandomStringUtils.secure().nextAlphanumeric(8));
        coreShare.setOid(user.getDefaultOid());
        String dType = shareExtMapper.visualizationType(resourceId);
        coreShare.setType(Strings.CI.equals("dataV", dType) ? 2 : 1);
        coreShareMapper.insert(coreShare);
    }

    @Transactional
    public String editUuid(ShareUuidEditor editor) {
        Long resourceId = editor.getResourceId();
        String uuid = editor.getUuid();
        CoreShare originData = queryByResource(resourceId);
        if (ObjectUtils.isEmpty(originData)) {
            return "公共链接不存在，请先创建！";
        }
        if (StringUtils.isBlank(uuid)) {
            return "不能为空！";
        }
        if (Strings.CS.equals(uuid, originData.getUuid())) {
            return "";
        }
        QueryWrapper<CoreShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        if (coreShareMapper.selectCount(queryWrapper) > 0) {
            return "已存在相同的链接，请重新输入！";
        }
        String regex = "^[a-zA-Z0-9]{8,16}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        if (!matcher.matches()) {
            return "仅支持8-16位(字母数字)，请重新输入！";
        }
        shareTicketManage.updateByUuidChange(originData.getUuid(), uuid);
        originData.setUuid(uuid);
        coreShareMapper.updateById(originData);
        return "";
    }

    public void editExp(Long resourceId, Long exp) {
        CoreShare originData = queryByResource(resourceId);
        if (ObjectUtils.isEmpty(originData)) {
            DEException.throwException("share instance not exist");
        }
        originData.setExp(exp);
        if (ObjectUtils.isEmpty(exp)) {
            originData.setExp(0L);
        }
        coreShareMapper.updateById(originData);
    }

    public void editPwd(Long resourceId, String pwd, Boolean autoPwd) {
        CoreShare originData = queryByResource(resourceId);
        if (ObjectUtils.isEmpty(originData)) {
            DEException.throwException("share instance not exist");
        }
        originData.setPwd(pwd);
        originData.setAutoPwd(ObjectUtils.isEmpty(autoPwd) || autoPwd);
        coreShareMapper.updateById(originData);
    }


    public IPage<SharePO> querySharePage(int goPage, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Long uid = AuthUtils.getUser().getUserId();
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        if (!CrestPermissionUtils.currentUserIsAdmin()) {
            queryWrapper.eq("s.creator", uid);
        }
        if (StringUtils.isNotBlank(request.getType())) {
            BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getType().toUpperCase());
            if (ObjectUtils.isEmpty(busiResourceEnum)) {
                DEException.throwException("type is invalid");
            }
            String resourceType = convertResourceType(request.getType());
            if (StringUtils.isNotBlank(resourceType)) {
                queryWrapper.eq("v.type", resourceType);
            }
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            queryWrapper.like("v.name", request.getKeyword());
        }
        String info = CommunityUtils.getInfo();
        if (StringUtils.isNotBlank(info)) {
            queryWrapper.notExists(String.format(info, "s.resource_id"));
        }
        queryWrapper.orderBy(true, request.isAsc(), "s.time");
        Page<SharePO> page = new Page<>(goPage, pageSize);
        return shareExtMapper.query(page, queryWrapper);
    }

    private String convertResourceType(String busiFlag) {
        return switch (busiFlag) {
            case "panel" -> "dashboard";
            case "screen" -> "dataV";
            default -> null;
        };
    }
    public IPage<ShareGridVO> query(int pageNum, int pageSize, VisualizationWorkbranchQueryRequest request) {
        IPage<SharePO> poiPage = proxy().querySharePage(pageNum, pageSize, request);
        List<ShareGridVO> vos = proxy().formatResult(poiPage.getRecords());
        if (!org.springframework.util.CollectionUtils.isEmpty(vos)) {
            vos.forEach(item -> {
                item.setCreator(Strings.CS.equals(item.getCreator(), "1") ? Translator.get("i18n_sys_admin") : item.getCreator());
            });
        }
        IPage<ShareGridVO> ipage = new Page<>();
        ipage.setSize(poiPage.getSize());
        ipage.setCurrent(poiPage.getCurrent());
        ipage.setPages(poiPage.getPages());
        ipage.setTotal(poiPage.getTotal());
        ipage.setRecords(vos);
        return ipage;
    }

    public List<ShareGridVO> formatResult(List<SharePO> pos) {
        if (CollectionUtils.isEmpty(pos)) return new ArrayList<>();
        return pos.stream().map(po ->
                new ShareGridVO(
                        po.getShareId(), po.getResourceId(), po.getName(), po.getCreator().toString(),
                        po.getTime(), po.getExp(), 9, po.getExtFlag(), po.getExtFlag1(), po.getType())).toList();
    }

    private ShareManage proxy() {
        return CommonBeanFactory.getBean(this.getClass());
    }

    private boolean peRequireValid(ShareBaseVO sharedBase, CoreShare share) {
        if (ObjectUtils.isEmpty(sharedBase) || !sharedBase.isPeRequire()) return true;
        Long exp = share.getExp();
        String pwd = share.getPwd();
        return StringUtils.isNotBlank(pwd) && ObjectUtils.isNotEmpty(exp) && exp > 0L;
    }

    public ShareProxyVO proxyInfo(ShareProxyRequest request) {
        ShareBaseVO sharedBase = sysParameterManage.shareBase();
        if (ObjectUtils.isNotEmpty(sharedBase) && sharedBase.isDisable()) {
            ShareProxyVO vo = new ShareProxyVO();
            vo.setShareDisable(true);
            return vo;
        }
        boolean inIframeError = request.isInIframe() && false;
        if (inIframeError) {
            return new ShareProxyVO();
        }
        QueryWrapper<CoreShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", request.getUuid());
        CoreShare coreShare = coreShareMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(coreShare))
            return null;
        if (!peRequireValid(sharedBase, coreShare)) {
            ShareProxyVO vo = new ShareProxyVO();
            vo.setPeRequireValid(false);
            vo.setInIframeError(false);
            return vo;
        }
        String defaultPwd = shareSecretManage.getDefaultPwd();
        String secret = StringUtils.isBlank(coreShare.getPwd()) ? defaultPwd : coreShare.getPwd();
        String linkToken = LinkTokenUtil.generate(coreShare.getCreator(), coreShare.getResourceId(), coreShare.getExp(), secret, coreShare.getOid());
        HttpServletResponse response = ServletUtils.response();
        response.addHeader(AuthConstant.LINK_TOKEN_KEY, linkToken);
        Integer type = coreShare.getType();
        String typeText = (ObjectUtils.isNotEmpty(type) && type == 1) ? "dashboard" : "dataV";
        TicketValidVO validVO = shareTicketManage.validateTicket(request.getTicket(), coreShare);
        return new ShareProxyVO(coreShare.getResourceId(), coreShare.getCreator(), linkExp(coreShare), pwdValid(coreShare, request.getCiphertext()), typeText, inIframeError, false, true, validVO);
    }

    private boolean linkExp(CoreShare coreShare) {
        if (ObjectUtils.isEmpty(coreShare.getExp()) || coreShare.getExp().equals(0L)) return false;
        return System.currentTimeMillis() > coreShare.getExp();
    }

    private boolean pwdValid(CoreShare coreShare, String ciphertext) {
        if (StringUtils.isBlank(coreShare.getPwd())) return true;
        if (StringUtils.isBlank(ciphertext)) return false;
        String text = RsaUtils.decryptStr(ciphertext);
        int splitIndex = text.indexOf(",");
        String pwd;
        if (splitIndex == -1) {
            splitIndex = 8;
            pwd = text.substring(splitIndex);
        } else {
            pwd = text.substring(splitIndex + 1);
        }
        String uuid = text.substring(0, splitIndex);
        return Strings.CS.equals(coreShare.getUuid(), uuid) && Strings.CS.equals(coreShare.getPwd(), pwd);
    }

    public boolean validatePwd(SharePwdValidator validator) {
        String ciphertext = RsaUtils.decryptStr(validator.getCiphertext());
        String pwd;
        int splitIndex = ciphertext.indexOf(",");
        if (splitIndex == -1) {
            splitIndex = 8;
            pwd = ciphertext.substring(splitIndex);
        } else {
            pwd = ciphertext.substring(splitIndex + 1);
        }
        String uuid = ciphertext.substring(0, splitIndex);
        QueryWrapper<CoreShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        CoreShare coreShare = coreShareMapper.selectOne(queryWrapper);
        return Strings.CS.equals(coreShare.getUuid(), uuid) && Strings.CS.equals(coreShare.getPwd(), pwd);
    }

    public Map<String, String> queryRelationByUserId(Long uid) {
        QueryWrapper<CoreShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("creator", uid);
        List<CoreShare> result = coreShareMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(result)) {
            return result.stream()
                    .collect(Collectors.toMap(coreShare -> String.valueOf(coreShare.getResourceId()), CoreShare::getUuid));
        }
        return new HashMap<>();
    }
}
