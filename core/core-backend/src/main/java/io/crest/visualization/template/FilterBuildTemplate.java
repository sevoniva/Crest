package io.crest.visualization.template;

import io.crest.extensions.view.dto.ChartExtFilterDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterBuildTemplate {

    public static Map<String, List<ChartExtFilterDTO>> buildEmpty(List<Map<String, Object>> components) {
        Map<String, List<ChartExtFilterDTO>> result = new HashMap<>();
        components.forEach(element -> {
            if (Strings.CS.equals(element.get("component").toString(), "UserView")) {
                String viewId = element.get("id").toString();
                result.put(viewId, new ArrayList<>());
            }
        });
        return result;
    }
}
