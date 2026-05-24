package io.crest.system.manage;

import org.springframework.stereotype.Component;

@Component
public class CoreUserManage {
    public String getUserName(Long uid) {
        return "管理员";
    }
}
