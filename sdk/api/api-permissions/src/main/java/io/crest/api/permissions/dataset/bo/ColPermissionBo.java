package io.crest.api.permissions.dataset.bo;

import io.crest.api.permissions.dataset.vo.ColPermissionInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ColPermissionBo implements Serializable {

    private boolean enable;

    private List<ColPermissionInfo> columns;
}
