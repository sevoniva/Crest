package io.crest.extensions.view.dto;

import io.crest.extensions.datasource.dto.DatasetTableFieldDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class ChartFieldCustomFilterDTO extends ChartViewFieldBaseDTO {
    private List<ChartCustomFilterItemDTO> filter;
    private DatasetTableFieldDTO field;
    private List<String> enumCheckField;
}
