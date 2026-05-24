package io.crest.chart.charts.impl.others;

import io.crest.chart.charts.impl.GroupChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class SankeyHandler extends GroupChartHandler {
    @Getter
    private String type = "sankey";
}
