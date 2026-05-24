package io.crest.api.permissions.dataset.api;

import io.crest.api.permissions.dataset.vo.RowColPermissionItem;

import java.util.List;

public interface RowColPermissionApi {

    List<RowColPermissionItem> query(List<Long> datasetIds);
}
