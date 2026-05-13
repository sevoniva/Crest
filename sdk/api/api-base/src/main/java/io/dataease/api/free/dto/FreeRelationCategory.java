package io.dataease.api.free.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FreeRelationCategory implements Serializable {
    @Serial
    private static final long serialVersionUID = 2235520960613140213L;

    private String name;
}
