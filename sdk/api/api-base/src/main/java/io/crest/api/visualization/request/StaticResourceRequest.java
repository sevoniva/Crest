package io.crest.api.visualization.request;


import lombok.Data;
import java.util.List;

@Data
public class StaticResourceRequest {
    private List<String> resourcePathList;

}
