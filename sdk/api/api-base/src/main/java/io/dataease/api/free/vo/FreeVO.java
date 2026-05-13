package io.dataease.api.free.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.model.TreeResultModel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class FreeVO implements Serializable, TreeResultModel<FreeVO> {
    @Serial
    private static final long serialVersionUID = -7055936995493939929L;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long pid;

    private String name;

    private boolean leaf;

    private Long createTime;

    private int rtId;

    private int extraFlag;

    private List<FreeVO> children;
}
