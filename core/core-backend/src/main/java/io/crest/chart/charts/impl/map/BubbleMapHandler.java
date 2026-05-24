package io.crest.chart.charts.impl.map;

import io.crest.chart.charts.impl.ExtQuotaChartHandler;
import io.crest.extensions.view.dto.AxisFormatResult;
import io.crest.extensions.view.dto.ChartViewDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class BubbleMapHandler extends ExtQuotaChartHandler {
    @Getter
    private String type = "bubble-map";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        return super.formatAxis(view);
    }
}


