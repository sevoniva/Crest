package io.dataease.api.permissions.auth.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BusiBatchAuthorizeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -5219199153835271350L;

    private List<BusiBatchAuthorizeNode> nodeList;

    private Long oid;
}
