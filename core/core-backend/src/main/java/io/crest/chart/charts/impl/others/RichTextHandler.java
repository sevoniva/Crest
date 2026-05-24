package io.crest.chart.charts.impl.others;

import io.crest.chart.charts.impl.YoyChartHandler;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class RichTextHandler extends YoyChartHandler {
    @Getter
    private String type = "rich-text";
    @Getter
    private String render = "custom";
}
