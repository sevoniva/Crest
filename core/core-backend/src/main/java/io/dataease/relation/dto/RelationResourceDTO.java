package io.dataease.relation.dto;

import lombok.Data;

@Data
public class RelationResourceDTO {
    private String id;
    private String name;
    private String type;
    private String subType;
    private Long updateTime;
}
