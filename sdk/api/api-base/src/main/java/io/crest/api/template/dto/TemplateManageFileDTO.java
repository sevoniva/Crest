package io.crest.api.template.dto;


import io.crest.api.template.vo.VisualizationTemplateVO;
import lombok.Data;

import java.util.List;


@Data
public class TemplateManageFileDTO extends VisualizationTemplateVO {

    /**
     * 样式数据
     */
    private String canvasStyleData;

    /**
     * 组件数据
     */
    private String componentData;


    private String staticResource;

}
