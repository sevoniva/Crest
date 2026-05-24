package io.crest.dataset.server;

import io.crest.api.dataset.DataAssistantApi;
import io.crest.api.dataset.vo.DataSQLBotAssistantVO;
import io.crest.api.dataset.vo.DataSQLBotDatasetVO;
import io.crest.dataset.manage.DatasetSQLBotManage;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sqlbot")
@ConditionalOnProperty(prefix = "crest.internal-lite", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DatasetSQLBotServer implements DataAssistantApi {

    @Resource
    private DatasetSQLBotManage datasetSQLBotManage;
    @Override
    public List<DataSQLBotAssistantVO> getDatasourceList(Long dsId, Long tableId) {
        return datasetSQLBotManage.getDatasourceList(dsId, tableId);
    }

    @Override
    public List<DataSQLBotDatasetVO> getDatasetList(String dvInfo) {
        return datasetSQLBotManage.getDatasetList(dvInfo);
    }
}
