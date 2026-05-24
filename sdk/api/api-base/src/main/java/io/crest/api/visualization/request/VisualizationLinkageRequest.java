package io.crest.api.visualization.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.crest.api.visualization.dto.VisualizationLinkageDTO;
import io.crest.api.visualization.vo.VisualizationLinkageVO;
import io.crest.constant.CommonConstants;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2023/7/13
 */
@Data
public class VisualizationLinkageRequest extends VisualizationLinkageVO {

    /**
     * 仪表板 or 大屏ID
     * */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dvId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceViewId;

    private Boolean ActiveStatus;

    private List<String> targetViewIds;

    private String resourceTable = CommonConstants.RESOURCE_TABLE.CORE;

    private List<VisualizationLinkageDTO> linkageInfo = new ArrayList<>();

}
