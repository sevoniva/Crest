package io.crest.api.dataset.dto;

import io.crest.api.dataset.union.DatasetGroupInfoDTO;
import io.crest.extensions.datasource.dto.DatasetTableFieldDTO;
import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class EnumObj {
    private DatasetTableFieldDTO field;
    private DatasetGroupInfoDTO dataset;
}
