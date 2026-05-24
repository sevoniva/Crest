package io.crest.api.dataset.union;

import io.crest.extensions.datasource.dto.DatasetTableDTO;
import io.crest.extensions.datasource.model.SQLObj;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author gin
 */
@Data
public class UnionParamDTO implements Serializable {
    private String unionType;
    private List<UnionItemDTO> unionFields;
    private DatasetTableDTO parentDs;
    private DatasetTableDTO currentDs;
    private SQLObj parentSQLObj;
    private SQLObj currentSQLObj;
}
