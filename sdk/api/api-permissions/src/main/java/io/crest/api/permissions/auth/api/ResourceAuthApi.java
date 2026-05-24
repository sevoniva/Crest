package io.crest.api.permissions.auth.api;

import io.crest.api.permissions.auth.dto.ResourcePermissionRequest;
import io.crest.api.permissions.auth.vo.ResourcePermissionVO;

import java.util.List;

public interface ResourceAuthApi {

    List<ResourcePermissionVO> queryResourcePermission(ResourcePermissionRequest request);
}
