package io.crest.system.sso;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SsoIdentityDecision {
    private SsoIdentityAction action;
    private Long userId;
    private SsoIdentityProfile profile;
}
