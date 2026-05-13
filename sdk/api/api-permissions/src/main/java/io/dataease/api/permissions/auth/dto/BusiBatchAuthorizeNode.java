package io.dataease.api.permissions.auth.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BusiBatchAuthorizeNode implements Serializable {

    @Serial
    private static final long serialVersionUID = 5804450226135199435L;
    private List<Long> idList;

    private int flag;
}
