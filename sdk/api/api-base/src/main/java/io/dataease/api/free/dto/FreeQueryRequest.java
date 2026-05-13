package io.dataease.api.free.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FreeQueryRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 7951259501228286914L;

    private int rtId;
}
