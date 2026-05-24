package io.crest.defeign.permissions.auth;

import io.crest.api.permissions.auth.api.InteractiveAuthApi;
import io.crest.feign.DeFeign;

@DeFeign(value = "permissions", path = "/interactive")
public interface InteractiveAuthFeignService extends InteractiveAuthApi {
}
