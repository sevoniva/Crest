package io.crest.chart.charts.impl.bar;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class BidirectionalBarHandler extends ProgressBarHandler {
    @Getter
    private String type = "bidirectional-bar";
}
