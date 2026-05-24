package io.crest.chart.charts.impl.map;

import io.crest.chart.charts.impl.ExtQuotaChartHandler;
import io.crest.extensions.view.dto.AxisFormatResult;
import io.crest.extensions.view.dto.ChartAxis;
import io.crest.chart.charts.impl.DefaultChartHandler;
import io.crest.extensions.view.dto.ChartViewDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class MapHandler extends ExtQuotaChartHandler {
    @Getter
    private String type = "map";
}
