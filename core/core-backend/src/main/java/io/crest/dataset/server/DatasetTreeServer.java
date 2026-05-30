package io.crest.dataset.server;


import io.crest.api.dataset.DatasetTreeApi;
import io.crest.api.dataset.dto.DataSetExportRequest;
import io.crest.api.dataset.dto.DatasetNodeDTO;
import io.crest.api.dataset.union.DatasetGroupInfoDTO;
import io.crest.api.dataset.vo.DataSetBarVO;
import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.dataset.manage.DatasetGroupManage;
import io.crest.exportCenter.manage.ExportCenterDownLoadManage;
import io.crest.exportCenter.manage.ExportCenterManage;
import io.crest.extensions.datasource.dto.DatasetTableDTO;
import io.crest.extensions.view.dto.*;
import io.crest.log.DeLog;
import io.crest.model.BusiNodeRequest;
import io.crest.model.BusiNodeVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.*;


@RestController
@RequestMapping("datasetTree")
public class DatasetTreeServer implements DatasetTreeApi {
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Resource
    private ExportCenterManage exportCenterManage;
    @Resource
    private ExportCenterDownLoadManage exportCenterDownLoadManage;


    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASET)
    @Override
    public DatasetGroupInfoDTO save(DatasetGroupInfoDTO datasetNodeDTO) throws Exception {
        return datasetGroupManage.save(datasetNodeDTO, false, true);
    }

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASET)
    @Override
    public DatasetNodeDTO rename(DatasetGroupInfoDTO dto) throws Exception {
        return datasetGroupManage.save(dto, true, false);
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.CREATE, st = LogST.DATASET)
    @Override
    public DatasetNodeDTO create(DatasetGroupInfoDTO dto) throws Exception {
        return datasetGroupManage.save(dto, false, true);
    }

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASET)
    @Override
    public DatasetNodeDTO move(DatasetGroupInfoDTO dto) throws Exception {
        return datasetGroupManage.move(dto);
    }

    @Override
    public boolean perDelete(Long id) {
        return datasetGroupManage.perDelete(id);
    }

    @DeLog(id = "#p0", ot = LogOT.DELETE, st = LogST.DATASET)
    @Override
    public void delete(Long id) {
        datasetGroupManage.delete(id);
    }


    @DeLog(ot = LogOT.READ, st = LogST.DATASET)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        return datasetGroupManage.tree(request);
    }

    @Override
    public DataSetBarVO barInfo(Long id) {
        return datasetGroupManage.queryBarInfo(id);
    }

    @Override
    public DatasetGroupInfoDTO get(Long id) throws Exception {
        return datasetGroupManage.getDatasetGroupInfoDTO(id, "preview");
    }

    @Override
    public DatasetGroupInfoDTO details(Long id) throws Exception {
        return datasetGroupManage.getDetail(id);
    }

    @Override
    public List<DatasetTableDTO> panelGetDsDetails(List<Long> ids) throws Exception {
        return datasetGroupManage.getDetail(ids);
    }

    @Override
    public List<SqlVariableDetails> getSqlParams(List<Long> ids) throws Exception {
        return datasetGroupManage.getSqlParams(ids);
    }

    @Override
    public List<DatasetTableDTO> detailWithPerm(List<Long> ids) throws Exception {
        return datasetGroupManage.getDetailWithPerm(ids);
    }

    @Override
    public void exportDataset(DataSetExportRequest request, HttpServletResponse response) throws Exception {
        if (request.isDataEaseBi()) {
            exportCenterDownLoadManage.downloadDataset(request, response);
        } else {
            exportCenterManage.addTask(request.getId(), "dataset", request);
        }
    }

}
