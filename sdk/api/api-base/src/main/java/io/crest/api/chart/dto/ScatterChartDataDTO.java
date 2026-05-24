package io.crest.api.chart.dto;

import io.crest.extensions.view.dto.ChartDimensionDTO;
import io.crest.extensions.view.dto.ChartQuotaDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 */
@Data
public class ScatterChartDataDTO {
    private Object[] value;
    private List<ChartDimensionDTO> dimensionList;
    private List<ChartQuotaDTO> quotaList;
}
