package io.dataease.extensions.datafilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtFormSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = -6236922011567180831L;

    private String id;
    private boolean disable;
    private List<NumberRule> numberInputRules;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NumberRule implements Serializable {
        @Serial
        private static final long serialVersionUID = -8841727448573594811L;
        private String  column;
        private String  term;

    }
}
