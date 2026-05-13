package io.dataease.api.ds.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DsSimpleVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 46446424188194481L;

    private String name;

    private String type;

    private String description;

    private String host;
}
