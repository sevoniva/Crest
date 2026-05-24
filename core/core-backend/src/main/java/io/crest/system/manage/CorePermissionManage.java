package io.crest.system.manage;

import io.crest.api.permissions.auth.dto.BusiPerCheckDTO;
import org.springframework.stereotype.Component;

@Component
public class CorePermissionManage {
    public boolean checkAuth(BusiPerCheckDTO dto) {
        return true;
    }
}
