package io.dataease.api.free.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class FreeBatchDelRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -296464652281011661L;

    private List<Long> idList;

    private int rtId;
}
