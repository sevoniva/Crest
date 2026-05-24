package io.crest.chart.charts.impl.numeric;

import io.crest.extensions.view.dto.AxisFormatResult;
import io.crest.extensions.view.dto.ChartAxis;
import io.crest.extensions.view.dto.ChartViewDTO;
import io.crest.extensions.view.dto.ChartViewFieldDTO;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("unchecked")
public class LiquidHandler extends NumericalChartHandler {
    @Getter
    private String type = "liquid";

    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var axisMap = new HashMap<ChartAxis, List<ChartViewFieldDTO>>();
        var yAxis = new ArrayList<>(view.getYAxis());
        Map<String, Object> customAttr = view.getCustomAttr();
        Map<String, Object> misc = (Map<String, Object>) customAttr.get("misc");
        ChartViewFieldDTO liquidMaxViewField = getDynamicField(misc, "liquidMaxType", "liquidMaxField");
        if (liquidMaxViewField != null) {
            yAxis.add(liquidMaxViewField);
        }
        axisMap.put(ChartAxis.xAxis, new ArrayList<>());
        axisMap.put(ChartAxis.yAxis, yAxis);
        var context = new HashMap<String, Object>();
        var result = new AxisFormatResult(axisMap, context);
        return result;
    }
}
