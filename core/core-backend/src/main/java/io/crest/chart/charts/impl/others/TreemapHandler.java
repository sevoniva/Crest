package io.crest.chart.charts.impl.others;

import io.crest.chart.charts.impl.ExtQuotaChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class TreemapHandler extends ExtQuotaChartHandler {
    @Getter
    private String type = "treemap";
}
