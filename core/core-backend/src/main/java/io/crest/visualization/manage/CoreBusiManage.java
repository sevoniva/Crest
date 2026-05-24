package io.crest.visualization.manage;

import io.crest.dataset.manage.DatasetGroupManage;
import io.crest.datasource.manage.DataSourceManage;
import io.crest.model.BusiNodeRequest;
import io.crest.model.BusiNodeVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("coreBusiManage")
public class CoreBusiManage {

    @Resource
    private CoreVisualizationManage coreVisualizationManage;

    @Resource
    private DataSourceManage dataSourceManage;

    @Resource
    private DatasetGroupManage datasetGroupManage;

    public Map<String, List<BusiNodeVO>> interactiveTree(Map<String, BusiNodeRequest> requestMap) {
        Map<String, List<BusiNodeVO>> result = new HashMap<>();
        for (Map.Entry<String, BusiNodeRequest> entry : requestMap.entrySet()) {
            BusiNodeRequest busiNodeRequest = entry.getValue();
            String key = entry.getKey();
            try {
                if (Strings.CI.equals(key, "datasource")) {
                    result.put(key, dataSourceManage.tree(busiNodeRequest));
                } else if (Strings.CI.equals(key, "dataset")) {
                    result.put(key, datasetGroupManage.tree(busiNodeRequest));
                } else if (Strings.CI.equalsAny(key, "dashboard", "dataV")) {
                    result.put(key, coreVisualizationManage.tree(busiNodeRequest));
                }
            } catch (Exception ignored) {
                result.put(key, new ArrayList<>());
            }
        }
        return result;
    }
}
