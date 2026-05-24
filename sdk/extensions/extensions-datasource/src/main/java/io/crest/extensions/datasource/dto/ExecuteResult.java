package io.crest.extensions.datasource.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExecuteResult {

    private int count;

    private List<String> generatedKeys;
}
