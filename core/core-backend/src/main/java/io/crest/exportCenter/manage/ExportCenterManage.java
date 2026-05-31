package io.crest.exportCenter.manage;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.crest.api.chart.request.ChartExcelRequest;
import io.crest.api.dataset.dto.DataSetExportRequest;
import io.crest.api.dataset.union.DatasetGroupInfoDTO;
import io.crest.api.export.BaseExportApi;
import io.crest.auth.bo.TokenUserBO;
import io.crest.auth.config.SubstituleLoginConfig;
import io.crest.commons.utils.ExcelWatermarkUtils;
import io.crest.dataset.manage.*;
import io.crest.exception.DEException;
import io.crest.exportCenter.dao.auto.entity.CoreExportDownloadTask;
import io.crest.exportCenter.dao.auto.entity.CoreExportTask;
import io.crest.exportCenter.dao.auto.mapper.CoreExportDownloadTaskMapper;
import io.crest.exportCenter.dao.auto.mapper.CoreExportTaskMapper;
import io.crest.exportCenter.dao.ext.mapper.ExportTaskExtMapper;
import io.crest.i18n.Translator;
import io.crest.model.ExportTaskDTO;
import io.crest.system.manage.SysParameterManage;
import io.crest.utils.*;
import io.crest.visualization.dao.auto.entity.VisualizationWatermark;
import io.crest.visualization.dao.auto.mapper.VisualizationWatermarkMapper;
import io.crest.visualization.dao.ext.mapper.ExtDataVisualizationMapper;
import io.crest.visualization.server.DataVisualizationServer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.crest.visualization.dto.WatermarkContentDTO;
import io.crest.api.permissions.user.vo.UserFormVO;

import java.io.File;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Future;

@Component
@Transactional(rollbackFor = Exception.class)
public class ExportCenterManage implements BaseExportApi {
    @Resource
    private CoreExportTaskMapper exportTaskMapper;
    @Resource
    private CoreExportDownloadTaskMapper coreExportDownloadTaskMapper;
    @Resource
    private ExportTaskExtMapper exportTaskExtMapper;
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    DataVisualizationServer dataVisualizationServer;
    @Resource
    private ExportCenterDownLoadManage exportCenterDownLoadManage;
    @Resource
    private SysParameterManage sysParameterManage;
    @Value("${crest.export.core.size:10}")
    private int core;
    @Value("${crest.export.max.size:10}")
    private int max;

    @Value("${crest.path.exportData:/opt/crest/data/exportData/}")
    private String exportData_path;
    @Resource
    private VisualizationWatermarkMapper watermarkMapper;
    @Resource
    private ExtDataVisualizationMapper visualizationMapper;
    static private List<String> STATUS = Arrays.asList("SUCCESS", "FAILED", "PENDING", "IN_PROGRESS", "ALL");
    private Map<String, Future> Running_Task = new HashMap<>();
    public void download(String id, String ticket, HttpServletResponse response) throws Exception {
        CoreExportTask exportTask = validateDownloadTask(id, ticket);
        exportCenterDownLoadManage.download(exportTask, response);
    }

    public void delete(String id) {
        CoreExportTask exportTask = getCurrentUserExportTask(id);
        deleteTask(exportTask);
    }

    public void deleteAll(String type) {
        if (!STATUS.contains(type)) {
            DEException.throwException("无效的状态");
        }
        Long currentUserId = currentUserId();
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId);
        if (!type.equalsIgnoreCase("ALL")) {
            queryWrapper.eq("export_status", type);
        }
        List<CoreExportTask> exportTasks = exportTaskMapper.selectList(queryWrapper);
        exportTasks.parallelStream().forEach(this::deleteTask);

    }

    public void delete(List<String> ids) {
        ids.forEach(this::delete);
    }

    public void retry(String id) {
        CoreExportTask exportTask = getCurrentUserExportTask(id);
        if (!exportTask.getExportStatus().equalsIgnoreCase("FAILED")) {
            DEException.throwException("正在导出中!");
        }
        exportTask.setExportStatus("PENDING");
        exportTask.setExportProgress("0");
        exportTask.setExportMachineName(hostName());
        exportTask.setExportTime(System.currentTimeMillis());
        exportTaskMapper.updateById(exportTask);
        FileUtils.deleteDirectoryRecursively(taskDirectoryPath(id));
        if (exportTask.getExportFromType().equalsIgnoreCase("chart")) {
            ChartExcelRequest request = JsonUtil.parseObject(exportTask.getParams(), ChartExcelRequest.class);
            exportCenterDownLoadManage.startViewTask(exportTask, request);
        }
        if (exportTask.getExportFromType().equalsIgnoreCase("dataset")) {
            DataSetExportRequest request = JsonUtil.parseObject(exportTask.getParams(), DataSetExportRequest.class);
            try {
                prepareDatasetExportRequest(exportTask.getExportFrom(), request);
                exportTask.setFileName(request.getFilename() + ".xlsx");
                exportTask.setParams(JsonUtil.toJSONString(request).toString());
            } catch (Exception e) {
                exportTask.setMsg(e.getMessage());
                exportTask.setExportStatus("FAILED");
                exportTaskMapper.updateById(exportTask);
                DEException.throwException(e.getMessage());
            }
            exportCenterDownLoadManage.startDatasetTask(exportTask, request);
        }
    }

    public IPage<ExportTaskDTO> pager(Page<ExportTaskDTO> page, String status) {
        if (!STATUS.contains(status)) {
            DEException.throwException("Invalid status: " + status);
        }

        Long currentUserId = currentUserId();
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId);
        if (!status.equalsIgnoreCase("ALL")) {
            queryWrapper.eq("export_status", status);
        }
        queryWrapper.orderByDesc("export_time");
        IPage<ExportTaskDTO> pager = exportTaskExtMapper.pager(page, queryWrapper);

        List<ExportTaskDTO> records = pager.getRecords();
        records.forEach(exportTask -> {
            if (status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(exportTask.getExportStatus())) {
                setExportFromAbsName(exportTask);
            }
            if (status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(exportTask.getExportStatus())) {
                proxy().setOrg(exportTask);
            }
        });

        return pager;
    }

    public Map<String, Long> exportTasks() {
        Long currentUserId = currentUserId();
        Map<String, Long> result = new HashMap<>();
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId);
        queryWrapper.eq("export_status", "IN_PROGRESS");
        result.put("IN_PROGRESS", exportTaskMapper.selectCount(queryWrapper));

        queryWrapper.clear();
        queryWrapper.eq("user_id", currentUserId);
        queryWrapper.eq("export_status", "SUCCESS");
        result.put("SUCCESS", exportTaskMapper.selectCount(queryWrapper));

        queryWrapper.clear();
        queryWrapper.eq("user_id", currentUserId);
        queryWrapper.eq("export_status", "FAILED");
        result.put("FAILED", exportTaskMapper.selectCount(queryWrapper));

        queryWrapper.clear();
        queryWrapper.eq("user_id", currentUserId);
        queryWrapper.eq("export_status", "PENDING");
        result.put("PENDING", exportTaskMapper.selectCount(queryWrapper));

        queryWrapper.clear();
        queryWrapper.eq("user_id", currentUserId);
        result.put("ALL", exportTaskMapper.selectCount(queryWrapper));
        return result;
    }

    public void setOrg(ExportTaskDTO exportTaskDTO) {
    }

    private ExportCenterManage proxy() {
        return CommonBeanFactory.getBean(ExportCenterManage.class);
    }

    private void setExportFromAbsName(ExportTaskDTO exportTaskDTO) {
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("chart")) {
            exportTaskDTO.setExportFromName(dataVisualizationServer.getAbsPath(exportTaskDTO.getExportFrom()));
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("dataset")) {
            List<String> fullName = new ArrayList<>();
            datasetGroupManage.geFullName(Long.valueOf(exportTaskDTO.getExportFrom()), fullName);
            Collections.reverse(fullName);
            List<String> finalFullName = fullName;
            exportTaskDTO.setExportFromName(String.join("/", finalFullName));
        }
    }

    private String hostName() {
        String hostname = null;
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            hostname = localMachine.getHostName();
        } catch (Exception e) {
            DEException.throwException("请设置主机名！");
        }
        return hostname;
    }

    private String taskDirectoryPath(String taskId) {
        return new File(exportData_path, taskId).getPath();
    }

    public void addTask(String exportFrom, String exportFromType, ChartExcelRequest request, String busiFlag) {
        Long currentUserId = currentUserId();
        CoreExportTask exportTask = new CoreExportTask();
        exportTask.setId(IDUtils.snowID().toString());
        exportTask.setUserId(currentUserId);
        exportTask.setExportFrom(Long.valueOf(exportFrom));
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.getViewName() + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        exportTaskMapper.insert(exportTask);
        if (busiFlag.equalsIgnoreCase("dashboard")) {
            exportCenterDownLoadManage.startPanelViewTask(exportTask, request);
        } else {
            exportCenterDownLoadManage.startDataVViewTask(exportTask, request);
        }

    }

    public void addTask(Long exportFrom, String exportFromType, DataSetExportRequest request) throws Exception {
        prepareDatasetExportRequest(exportFrom, request);
        Long currentUserId = currentUserId();
        CoreExportTask exportTask = new CoreExportTask();
        exportTask.setId(IDUtils.snowID().toString());
        exportTask.setUserId(currentUserId);
        exportTask.setExportFrom(exportFrom);
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.getFilename() + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        exportTaskMapper.insert(exportTask);
        exportCenterDownLoadManage.startDatasetTask(exportTask, request);
    }

    private void prepareDatasetExportRequest(Long exportFrom, DataSetExportRequest request) throws Exception {
        DatasetGroupInfoDTO dataset = datasetGroupManage.getDatasetGroupInfoDTO(exportFrom, null);
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(dataset, null);
        if (sqlMap == null || StringUtils.isBlank((String) sqlMap.get("sql"))) {
            DEException.throwException("数据集配置不完整，无法导出");
        }
        if (StringUtils.isBlank(request.getFilename())) {
            request.setFilename(StringUtils.defaultIfBlank(dataset.getName(), "dataset-" + exportFrom));
        }
    }

    @Override
    public void addTask(String exportFromId, String exportFromType, HashMap<String, Object> request, Long userId, Long org) {
        CoreExportTask exportTask = new CoreExportTask();
        request.put("org", org);
        exportTask.setId(IDUtils.snowID().toString());
        exportTask.setUserId(userId);
        exportTask.setExportFrom(Long.valueOf(exportFromId));
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.get("name") + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        exportTaskMapper.insert(exportTask);
    }

    public void cleanLog() {
        String key = "basic.exportFileLiveTime";
        String val = sysParameterManage.singleVal(key);
        if (StringUtils.isBlank(val)) {
            DEException.throwException("未获取到文件保留时间");
        }
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        long expTime = Long.parseLong(val) * 24L * 3600L * 1000L;
        long threshold = System.currentTimeMillis() - expTime;
        queryWrapper.lt("export_time", threshold);
        exportTaskMapper.selectList(queryWrapper).forEach(coreExportTask -> {
            deleteTask(coreExportTask);
        });

    }

    public void addWatermarkTools(Workbook wb) {
        VisualizationWatermark watermark = watermarkMapper.selectById("system_default");
        WatermarkContentDTO watermarkContent = JsonUtil.parseObject(watermark.getSettingContent(), WatermarkContentDTO.class);
        if (watermarkContent.getEnable() && watermarkContent.getExcelEnable()) {
            UserFormVO userInfo = visualizationMapper.queryInnerUserInfo(currentUserId());
            // 在主逻辑中添加水印
            int watermarkPictureIdx = ExcelWatermarkUtils.addWatermarkImage(wb, watermarkContent, userInfo); // 生成水印图片并获取 ID
            for (Sheet sheet : wb) {
                ExcelWatermarkUtils.addWatermarkToSheet(sheet, watermarkPictureIdx); // 为每个 Sheet 添加水印
            }
        }
    }

    public String generateDownloadUri(String id) {
        CoreExportTask exportTask = getCurrentUserExportTask(id);
        long createTime = System.currentTimeMillis();
        CoreExportDownloadTask coreExportDownloadTask = coreExportDownloadTaskMapper.selectById(id);
        if (coreExportDownloadTask != null) {
            coreExportDownloadTask.setCreateTime(createTime);
            coreExportDownloadTaskMapper.updateById(coreExportDownloadTask);
        } else {
            coreExportDownloadTask = new CoreExportDownloadTask();
            coreExportDownloadTask.setId(id);
            coreExportDownloadTask.setCreateTime(createTime);
            coreExportDownloadTask.setValidTime(5L);
            coreExportDownloadTaskMapper.insert(coreExportDownloadTask);
        }
        return "/exportCenter/download/" + id + "?ticket=" + buildDownloadTicket(exportTask, createTime, coreExportDownloadTask.getValidTime());
    }


    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void checkDownLoadInfos() {
        coreExportDownloadTaskMapper.selectList(null).forEach(downLoadInfo -> {
            if (System.currentTimeMillis() - downLoadInfo.getCreateTime() > downLoadInfo.getValidTime() * 60 * 1000) {
                coreExportDownloadTaskMapper.deleteById(downLoadInfo.getId());
            }
        });
    }

    @Data
    public class DownLoadInfo {
        String id;
        Long validTime; // 单位：minutes
        Long createTime;
    }

    private CoreExportTask getCurrentUserExportTask(String id) {
        Long currentUserId = currentUserId();
        CoreExportTask exportTask = exportTaskMapper.selectById(id);
        if (exportTask == null || !Objects.equals(exportTask.getUserId(), currentUserId)) {
            DEException.throwException("任务不存在");
        }
        return exportTask;
    }

    private void deleteTask(CoreExportTask exportTask) {
        if (exportTask == null) {
            return;
        }
        String id = exportTask.getId();
        Iterator<Map.Entry<String, Future>> iterator = Running_Task.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Future> entry = iterator.next();
            if (entry.getKey().equalsIgnoreCase(id)) {
                entry.getValue().cancel(true);
                iterator.remove();
            }
        }
        FileUtils.deleteDirectoryRecursively(taskDirectoryPath(id));
        exportTaskMapper.deleteById(id);
    }

    private CoreExportTask validateDownloadTask(String id, String ticket) {
        if (StringUtils.isBlank(ticket)) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        CoreExportDownloadTask coreExportDownloadTask = coreExportDownloadTaskMapper.selectById(id);
        CoreExportTask exportTask = exportTaskMapper.selectById(id);
        if (coreExportDownloadTask == null || exportTask == null) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(resolveTicketSecret(exportTask));
            Verification verification = JWT.require(algorithm);
            JWTVerifier verifier = verification.build();
            DecodedJWT jwt = verifier.verify(ticket);
            String taskId = jwt.getClaim("taskId").asString();
            Long uid = jwt.getClaim("uid").asLong();
            Long ticketTime = jwt.getClaim("ts").asLong();
            if (!Objects.equals(id, taskId)
                    || !Objects.equals(uid, exportTask.getUserId())
                    || !Objects.equals(ticketTime, coreExportDownloadTask.getCreateTime())
                    || System.currentTimeMillis() - coreExportDownloadTask.getCreateTime() > coreExportDownloadTask.getValidTime() * 60 * 1000) {
                DEException.throwException(Translator.get("i18n_download_link_invalid"));
            }
        } catch (Exception e) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        coreExportDownloadTaskMapper.deleteById(id);
        return exportTask;
    }

    private String buildDownloadTicket(CoreExportTask exportTask, long createTime, Long validTime) {
        Algorithm algorithm = Algorithm.HMAC256(resolveTicketSecret(exportTask));
        return JWT.create()
                .withClaim("taskId", exportTask.getId())
                .withClaim("uid", exportTask.getUserId())
                .withClaim("ts", createTime)
                .withExpiresAt(new Date(createTime + validTime * 60 * 1000))
                .sign(algorithm);
    }

    private String resolveTicketSecret(CoreExportTask exportTask) {
        return SubstituleLoginConfig.getTokenSecret() + ":" + exportTask.getUserId();
    }

    private Long currentUserId() {
        TokenUserBO user = AuthUtils.getUser();
        if (user == null || user.getUserId() == null) {
            DEException.throwException("user not found");
        }
        return user.getUserId();
    }
}
