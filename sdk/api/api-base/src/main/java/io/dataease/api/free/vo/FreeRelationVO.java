package io.dataease.api.free.vo;

import io.dataease.api.free.dto.FreeRelationCategory;
import io.dataease.api.free.dto.FreeRelationLink;
import io.dataease.api.free.dto.FreeRelationNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FreeRelationVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7087187548660162237L;

    private List<FreeRelationCategory> categories;

    private List<FreeRelationLink> links;

    private List<FreeRelationNode> nodes;

    private int maxNodeSize;
}
