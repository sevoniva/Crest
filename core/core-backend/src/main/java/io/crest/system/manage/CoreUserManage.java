package io.crest.system.manage;

import io.crest.substitute.permissions.user.CrestUserManage;
import io.crest.substitute.permissions.user.model.CrestUser;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CoreUserManage {

    @Resource
    private CrestUserManage crestUserManage;

    public String getUserName(Long uid) {
        if (uid == null) {
            return null;
        }
        CrestUser user = crestUserManage.queryById(uid);
        if (user == null) {
            return null;
        }
        return StringUtils.defaultIfBlank(user.getName(), user.getAccount());
    }
}
