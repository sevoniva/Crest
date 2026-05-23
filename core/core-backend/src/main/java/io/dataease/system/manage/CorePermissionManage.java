package io.dataease.system.manage;

import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import org.springframework.stereotype.Component;

@Component
public class CorePermissionManage {
    public boolean checkAuth(BusiPerCheckDTO dto) {
        return true;
    }
}
