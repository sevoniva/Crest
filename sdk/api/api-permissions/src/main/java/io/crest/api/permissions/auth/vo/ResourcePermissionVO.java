package io.crest.api.permissions.auth.vo;

import io.crest.api.permissions.user.vo.UserReciVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResourcePermissionVO extends UserReciVO implements Serializable {
    private Long resourceId;
    private boolean enable;
}
