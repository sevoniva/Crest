package io.crest.api.chart.dto;

import io.crest.extensions.datasource.dto.DatasetTableFieldDTO;
import lombok.Data;

@Data
public class DeSortField extends DatasetTableFieldDTO {

    private String orderDirection;
}
