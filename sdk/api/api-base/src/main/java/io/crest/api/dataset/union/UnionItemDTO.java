package io.crest.api.dataset.union;

import io.crest.extensions.datasource.dto.DatasetTableFieldDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author gin
 */
@Data
public class UnionItemDTO implements Serializable {
    private DatasetTableFieldDTO parentField;
    private DatasetTableFieldDTO currentField;
}
