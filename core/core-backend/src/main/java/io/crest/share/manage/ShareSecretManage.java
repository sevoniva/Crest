package io.crest.share.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.crest.exception.DEException;
import io.crest.share.dao.auto.entity.CoreShare;
import io.crest.share.dao.auto.mapper.CoreShareMapper;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("shareSecretManage")
public class ShareSecretManage {


    @Getter
    @Value("${crest.default-link-pwd:crest-link-pwd}")
    private String defaultPwd;


    @Resource
    private CoreShareMapper coreShareMapper;

    public String getSecret(Long resourceId, Long uid) {
        QueryWrapper<CoreShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("creator", uid);
        queryWrapper.eq("resource_id", resourceId);
        CoreShare coreShare = coreShareMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(coreShare)) DEException.throwException("Share resource do not exist");
        String sharePwd = coreShare.getPwd();
        return StringUtils.isNotBlank(sharePwd) ? sharePwd : defaultPwd;
    }
}
