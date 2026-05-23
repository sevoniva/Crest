package io.dataease.relation.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RelationGraphDTO {
    private List<RelationNodeDTO> nodes = new ArrayList<>();
    private List<RelationEdgeDTO> edges = new ArrayList<>();
    private RelationSummaryDTO summary = new RelationSummaryDTO();
}
