package io.crest.api.threshold.dto;

import io.crest.constant.CommonConstants;
import io.crest.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ThresholdInstanceRequest extends KeywordRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 7146083160815300271L;

    private Long thresholdId;
}
