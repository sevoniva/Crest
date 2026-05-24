package io.crest.defeign.permissions.user;

import io.crest.api.permissions.user.api.UserApi;
import io.crest.feign.DeFeign;

@DeFeign(value = "permissions", path = "/user")
public interface UserFeignService extends UserApi {
}
