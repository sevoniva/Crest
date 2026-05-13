package io.dataease.api.free.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FreeSyncRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 4845269683316801334L;

    private Long oid;
}
