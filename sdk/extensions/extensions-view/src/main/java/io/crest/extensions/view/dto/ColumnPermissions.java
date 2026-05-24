package io.crest.extensions.view.dto;

import lombok.Data;

import java.util.List;

@Data
public class ColumnPermissions {
    private Boolean enable;
    private List<ColumnPermissionItem> columns;
}
