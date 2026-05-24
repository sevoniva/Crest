package io.crest.relation.dto;

import lombok.Data;

@Data
public class RelationEdgeDTO {
    private String source;
    private String target;
    private String type;
    private String label;
}
