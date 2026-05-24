package io.crest.api.threshold.dto;

import io.crest.constant.CommonConstants;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ThresholdSwitchRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -805688257417787452L;

    private Long id;

    private Boolean enable;

    private String resourceTable = CommonConstants.RESOURCE_TABLE.CORE;
}
