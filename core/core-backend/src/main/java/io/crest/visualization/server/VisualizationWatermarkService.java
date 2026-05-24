package io.crest.visualization.server;

import io.crest.api.visualization.VisualizationWatermarkApi;
import io.crest.api.visualization.request.VisualizationWatermarkRequest;
import io.crest.api.visualization.vo.VisualizationWatermarkVO;
import io.crest.utils.BeanUtils;
import io.crest.visualization.dao.auto.entity.VisualizationWatermark;
import io.crest.visualization.dao.auto.mapper.VisualizationWatermarkMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : WangJiaHao
 * @date : 2024/1/10 16:59
 */
@RestController
@RequestMapping("/watermark")
public class VisualizationWatermarkService implements VisualizationWatermarkApi {

    private final static String DEFAULT_ID ="system_default";

    @Resource
    private VisualizationWatermarkMapper watermarkMapper;

    @Override
    public VisualizationWatermarkVO getWatermarkInfo() {
        VisualizationWatermark watermark =  watermarkMapper.selectById(DEFAULT_ID);
        VisualizationWatermarkVO watermarkVO = new VisualizationWatermarkVO();
        return BeanUtils.copyBean(watermarkVO,watermark);
    }

    @Override
    public void saveWatermarkInfo(VisualizationWatermarkRequest watermarkRequest) {
        VisualizationWatermark watermark =  new VisualizationWatermark();
        BeanUtils.copyBean(watermark,watermarkRequest);
        watermark.setId(DEFAULT_ID);
        watermarkMapper.updateById(watermark);
    }
}
