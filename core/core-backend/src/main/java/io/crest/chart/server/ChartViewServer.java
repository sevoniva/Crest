package io.crest.chart.server;

import io.crest.api.chart.ChartViewApi;
import io.crest.api.chart.vo.ChartBaseVO;
import io.crest.api.chart.vo.ViewSelectorVO;
import io.crest.chart.manage.ChartViewManege;
import io.crest.constant.CommonConstants;
import io.crest.dataset.utils.DatasetUtils;
import io.crest.exception.DEException;
import io.crest.extensions.view.dto.ChartViewDTO;
import io.crest.extensions.view.dto.ChartViewFieldDTO;
import io.crest.result.ResultCode;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("chart")
public class ChartViewServer implements ChartViewApi {
    @Resource
    private ChartViewManege chartViewManege;

    @Override
    public ChartViewDTO getData(Long id) throws Exception {
        try {
            return chartViewManege.getChart(id, CommonConstants.RESOURCE_TABLE.CORE);
        } catch (Exception e) {
            DEException.throwException(ResultCode.DATA_IS_WRONG.code(), e.getMessage());
        }
        return null;
    }

    @Override
    public Map<String, List<ChartViewFieldDTO>> listByDQ(Long id, Long chartId, ChartViewDTO dto) {
        Map<String, List<ChartViewFieldDTO>> stringListMap = chartViewManege.listByDQ(id, chartId, dto);
        DatasetUtils.listEncode(stringListMap.get("dimensionList"));
        DatasetUtils.listEncode(stringListMap.get("quotaList"));
        return stringListMap;
    }

    @Override
    public ChartViewDTO save(ChartViewDTO dto) throws Exception {
        return chartViewManege.save(dto);
    }

    @Override
    public String checkSameDataSet(String viewIdSource, String viewIdTarget) {
        return chartViewManege.checkSameDataSet(viewIdSource, viewIdTarget);
    }

    @Override
    public ChartViewDTO getDetail(Long id, String resourceTable) {
        return chartViewManege.getDetails(id, resourceTable);
    }

    @Override
    public List<ViewSelectorVO> viewOption(Long resourceId) {
        return chartViewManege.viewOption(resourceId);
    }

    @Override
    public void copyField(Long id, Long chartId) {
        chartViewManege.copyField(id, chartId);
    }

    @Override
    public void deleteField(Long id) {
        chartViewManege.deleteField(id);
    }

    @Override
    public void deleteFieldByChart(Long chartId) {
        chartViewManege.deleteFieldByChartId(chartId);
    }

    @Override
    public ChartBaseVO chartBaseInfo(Long id, String resourceTable) {
        return chartViewManege.chartBaseInfo(id, resourceTable);
    }
}
