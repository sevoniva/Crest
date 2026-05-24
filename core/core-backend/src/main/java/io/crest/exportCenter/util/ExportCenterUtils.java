package io.crest.exportCenter.util;

import io.crest.exportCenter.manage.ExportCenterLimitManage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ExportCenterUtils {


    private static ExportCenterLimitManage exportCenterLimitManage;

    @Resource(name = "exportCenterLimitManage")
    public void setExportCenterLimitManage(ExportCenterLimitManage exportCenterLimitManage) {
        ExportCenterUtils.exportCenterLimitManage = exportCenterLimitManage;
    }

    public static long getExportLimit(String type) {
        return exportCenterLimitManage.getExportLimit(type);
    }
}
