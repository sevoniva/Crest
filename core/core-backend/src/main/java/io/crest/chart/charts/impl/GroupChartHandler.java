package io.crest.chart.charts.impl;


import io.crest.extensions.view.dto.AxisFormatResult;
import io.crest.extensions.view.dto.ChartAxis;
import io.crest.extensions.view.dto.ChartViewDTO;
import io.crest.extensions.view.dto.ChartViewFieldDTO;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class GroupChartHandler extends YoyChartHandler {
    @Override
    public AxisFormatResult formatAxis(ChartViewDTO view) {
        var result = super.formatAxis(view);
        var xAxis = new ArrayList<ChartViewFieldDTO>(view.getXAxis());
        xAxis.addAll(view.getXAxisExt());
        result.getAxisMap().put(ChartAxis.xAxis, xAxis);
        return result;
    }
}
