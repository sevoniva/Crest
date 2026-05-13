package io.dataease.api.free.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FreeRelationNode implements Serializable {
    @Serial
    private static final long serialVersionUID = 5396869322229400707L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private Integer category;

    private Integer x;

    private Integer y;

    public FreeRelationNode(Long id, String name, Integer category) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.x = 0;
        this.y = 0;
    }
}
