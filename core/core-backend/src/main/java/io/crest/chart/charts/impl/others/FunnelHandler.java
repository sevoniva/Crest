package io.crest.chart.charts.impl.others;

import io.crest.chart.charts.impl.ExtQuotaChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class FunnelHandler extends ExtQuotaChartHandler {
    @Getter
    private String type = "funnel";

    @Override
    public void init() {
        chartHandlerManager.registerChartHandler(this.getRender(), this.getType(), this);
        chartHandlerManager.registerChartHandler(this.getRender(), "stage-funnel", this);
    }
}
