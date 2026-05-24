package io.crest.relation.dto;

import lombok.Data;

@Data
public class RelationNodeDTO {
    private String id;
    private String resourceId;
    private String name;
    private String type;
    private String subType;
    private String description;
    private Long createTime;
    private Long updateTime;
    private Integer level;
}
