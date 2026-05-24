package io.crest.exportCenter.manage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("exportCenterLimitManage")
public class ExportCenterLimitManage {

    @Value("${crest.export.dataset.limit:100000}")
    private Long datasetLimit;

    @Value("${crest.export.views.limit:100000}")
    private Long viewLimit;

    public Long getExportLimit(String type) {
        if (StringUtils.isBlank(type) || Strings.CS.equals("view", type)) {
            return viewLimit;
        }
        return datasetLimit;
    }
}
