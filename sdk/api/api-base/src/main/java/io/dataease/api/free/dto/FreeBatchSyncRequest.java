package io.dataease.api.free.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FreeBatchSyncRequest extends FreeSyncRequest {
    @Serial
    private static final long serialVersionUID = 7068735824164921251L;

    private List<Long> idList;

    private int rtId;
}
