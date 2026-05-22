package io.dataease.license.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class F2CLicResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public enum Status {
        valid, expired, invalid
    }

    private Status status;
}
