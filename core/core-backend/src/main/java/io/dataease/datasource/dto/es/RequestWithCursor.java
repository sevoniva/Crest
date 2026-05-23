package io.dataease.datasource.dto.es;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RequestWithCursor extends Request {
    private String cursor;
}
