package io.crest.exportCenter.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.crest.api.exportCenter.ExportCenterApi;
import io.crest.exception.DEException;
import io.crest.exportCenter.manage.ExportCenterManage;
import io.crest.exportCenter.util.ExportCenterUtils;
import io.crest.model.ExportTaskDTO;
import io.crest.result.ResultMessage;
import io.crest.utils.JsonUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exportCenter")
@Transactional(rollbackFor = Exception.class)
public class ExportCenterServer implements ExportCenterApi {
    @Resource
    private ExportCenterManage exportCenterManage;

    @Override
    public Map<String, Long> exportTasks() {
        return exportCenterManage.exportTasks();
    }

    @Override
    public IPage<ExportTaskDTO> pager(int goPage, int pageSize, String status) {
        Page<ExportTaskDTO> page = new Page<>(goPage, pageSize);
        return exportCenterManage.pager(page, status);
    }

    @Override
    public void delete(String id) {
        exportCenterManage.delete(id);
    }

    @Override
    public void delete(List<String> ids) {
        exportCenterManage.delete(ids);
    }

    @Override
    public void deleteAll(String type) {
        exportCenterManage.deleteAll(type);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void download(String id, String ticket, HttpServletResponse response) throws Exception {
        try {
            exportCenterManage.download(id, ticket, response);
        } catch (DEException e) {
            writeForbidden(response, e);
        }
    }

    private void writeForbidden(HttpServletResponse response, DEException e) throws Exception {
        if (response.isCommitted()) {
            return;
        }
        response.resetBuffer();
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JsonUtil.toJSONString(new ResultMessage(e.getCode(), e.getMessage())).toString());
    }

    @Override
    public String generateDownloadUri(String id) throws Exception {
        return exportCenterManage.generateDownloadUri(id);
    }

    @Override
    public void retry(String id) {
        exportCenterManage.retry(id);
    }

    public String exportLimit() {
        return String.valueOf(ExportCenterUtils.getExportLimit("dataset"));
    }
}
