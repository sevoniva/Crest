package io.dataease.substitute.permissions.user.model;

import lombok.Data;

@Data
public class CrestUser {
    private Long id;
    private String account;
    private String name;
    private String email;
    private String phonePrefix;
    private String phone;
    private String passwordHash;
    private Boolean enable;
    private Boolean admin;
    private Integer origin;
    private Long createTime;
    private Long updateTime;
}
