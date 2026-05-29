package io.crest.substitute.permissions.user.model;

import lombok.Data;

@Data
public class SsoUserProfile {
    private String externalId;
    private String account;
    private String name;
    private String email;
}
