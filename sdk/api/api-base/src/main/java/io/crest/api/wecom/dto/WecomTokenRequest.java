package io.crest.api.wecom.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WecomTokenRequest implements Serializable {

    private String code;

    private String state;
}
