package io.dataease.api.free.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FreeRelationLink implements Serializable {
    @Serial
    private static final long serialVersionUID = 8574916923164645781L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long source;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long target;
}
