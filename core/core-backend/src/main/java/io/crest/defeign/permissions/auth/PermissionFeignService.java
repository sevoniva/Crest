package io.crest.defeign.permissions.auth;

import io.crest.feign.DeFeign;
import io.crest.api.permissions.auth.api.AuthApi;


@DeFeign(value = "permissions", path = "/auth")
public interface PermissionFeignService extends AuthApi {

}
