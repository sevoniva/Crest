package io.crest.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crest.api.dataset.dto.PreviewSqlDTO;
import io.crest.api.ds.DatasourceApi;
import io.crest.api.ds.vo.*;
import io.crest.api.permissions.relation.api.RelationApi;
import io.crest.commons.constants.TaskStatus;
import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.constant.SQLConstants;
import io.crest.dataset.manage.DatasetDataManage;
import io.crest.dataset.utils.TableUtils;
import io.crest.datasource.dao.auto.entity.*;
import io.crest.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.crest.datasource.dao.auto.mapper.CoreDsFinishPageMapper;
import io.crest.datasource.dao.auto.mapper.QrtzSchedulerStateMapper;
import io.crest.datasource.dao.ext.mapper.DataSourceExtMapper;
import io.crest.datasource.dao.ext.mapper.TaskLogExtMapper;
import io.crest.datasource.manage.DataSourceManage;
import io.crest.datasource.manage.DatasourceSyncManage;
import io.crest.datasource.manage.EngineManage;
import io.crest.datasource.provider.CalciteProvider;
import io.crest.datasource.provider.EngineProvider;
import io.crest.datasource.provider.ExcelUtils;
import io.crest.datasource.request.EngineRequest;
import io.crest.exception.DEException;
import io.crest.extensions.datasource.api.PluginManageApi;
import io.crest.extensions.datasource.dto.*;
import io.crest.extensions.datasource.factory.ProviderFactory;
import io.crest.extensions.datasource.provider.Provider;
import io.crest.extensions.datasource.vo.DatasourceConfiguration;
import io.crest.extensions.datasource.vo.PluginDatasourceVO;
import io.crest.i18n.Translator;
import io.crest.job.schedule.CheckDsStatusJob;
import io.crest.job.schedule.ScheduleManager;
import io.crest.log.DeLog;
import io.crest.model.BusiNodeRequest;
import io.crest.model.BusiNodeVO;
import io.crest.result.ResultCode;
import io.crest.system.dao.auto.entity.CoreSysSetting;
import io.crest.system.manage.CoreUserManage;
import io.crest.utils.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.crest.datasource.server.DatasourceTaskServer.ScheduleType.MANUAL;
import static io.crest.datasource.server.DatasourceTaskServer.ScheduleType.RIGHTNOW;


@RestController
@RequestMapping("/datasource")
@SuppressWarnings({"deprecation", "unchecked"})
public class DatasourceServer implements DatasourceApi {
    private static final Pattern ORACLE_RECYCLE_BIN_TABLE_PATTERN = Pattern.compile("^BIN\\$.*\\$[0-9]+$", Pattern.CASE_INSENSITIVE);
    private static final String EXCEL_ROW_ID_FIELD = "dataease_uuid";
    private static final String EXCEL_EDIT_ROW_ID = "_rowId";
    private static final int MAX_EXCEL_EDIT_PAGE_SIZE = 500;
    private static final int MAX_EXCEL_EDIT_BATCH_SIZE = 5000;
    private static final List<String> EXCEL_UPLOAD_SUFFIXES = List.of("xlsx", "xls", "csv");

    @Resource
    private CoreDatasourceMapper datasourceMapper;
    @Resource
    private EngineManage engineManage;
    @Resource
    private DatasourceTaskServer datasourceTaskServer;
    @Resource
    private CalciteProvider calciteProvider;
    @Resource
    private DatasourceSyncManage datasourceSyncManage;
    @Resource
    private TaskLogExtMapper taskLogExtMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private DataSourceManage dataSourceManage;
    @Resource
    private QrtzSchedulerStateMapper qrtzSchedulerStateMapper;
    @Resource
    private DataSourceExtMapper dataSourceExtMapper;
    @Resource
    private CoreDsFinishPageMapper coreDsFinishPageMapper;
    @Resource
    private DatasetDataManage datasetDataManage;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private CoreUserManage coreUserManage;
    @Autowired(required = false)
    private PluginManageApi pluginManage;
    @Autowired(required = false)
    private RelationApi relationManage;

    private static String decodeBase64RequestValue(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            DEException.throwException(ResultCode.PARAM_IS_INVALID.code(), fieldName + "格式无效");
            return "";
        }
    }

    public enum UpdateType {
        all_scope, add_scope
    }

    public static final List<String> notFullDs = List.of("folder", "Excel", "API");

    private TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
    };
    @Resource
    private CommonThreadPool commonThreadPool;
    private boolean isUpdatingStatus = false;
    private static List<Long> syncDsIds = new ArrayList<>();


    @Override
    public List<DatasourceDTO> query(String keyWord) {
        return null;
    }

    public boolean checkRepeat(@RequestBody BusiDsRequest dataSourceDTO) {
        if (Arrays.asList("folder", "es").contains(dataSourceDTO.getType()) || dataSourceDTO.getType().contains("API") || dataSourceDTO.getType().contains("Excel")) {
            return false;
        }
        BusiNodeRequest request = new BusiNodeRequest();
        request.setBusiFlag("datasource");
        List<BusiNodeVO> busiNodeVOS = dataSourceManage.tree(request);
        List<Long> ids = new ArrayList<>();
        filterDs(busiNodeVOS, ids, dataSourceDTO.getType(), dataSourceDTO.getId());

        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        QueryWrapper<CoreDatasource> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);

        List<CoreDatasource> datasources = datasourceMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(datasources)) {
            return false;
        }
        dataSourceDTO.setConfiguration(decodeBase64RequestValue(dataSourceDTO.getConfiguration(), "数据源配置"));
        DatasourceConfiguration configuration = JsonUtil.parseObject(dataSourceDTO.getConfiguration(), DatasourceConfiguration.class);
        boolean hasRepeat = false;
        for (CoreDatasource datasource : datasources) {
            if (notFullDs.stream().anyMatch(e -> datasource.getType().contains(e))) {
                continue;
            }
            if (StringUtils.isEmpty(datasource.getConfiguration())) {
                continue;
            }
            DatasourceConfiguration compare = JsonUtil.parseObject(datasource.getConfiguration(), DatasourceConfiguration.class);
            if (compare == null) {
                continue;
            }
            switch (dataSourceDTO.getType()) {
                case "sqlServer":
                case "db2":
                case "oracle":
                case "obOracle":
                case "pg":
                case "redshift":
                    if (configuration.getHost().equalsIgnoreCase(compare.getHost()) && Objects.equals(configuration.getPort(), compare.getPort()) && configuration.getDataBase().equalsIgnoreCase(compare.getDataBase()) && configuration.getSchema().equalsIgnoreCase(compare.getSchema())) {
                        hasRepeat = true;
                    } else {
                        hasRepeat = false;
                    }
                    break;
                default:
                    if (configuration.getHost().equalsIgnoreCase(compare.getHost()) && Objects.equals(configuration.getPort(), compare.getPort()) && configuration.getDataBase().equalsIgnoreCase(compare.getDataBase())) {
                        hasRepeat = true;
                    } else {
                        hasRepeat = false;
                    }
                    break;
            }
        }
        return hasRepeat;
    }

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASOURCE)
    @Transactional
    public DatasourceDTO move(BusiCreateFolderRequest busiDsRequest) {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        if (dataSourceDTO.getPid() == null) {
            DEException.throwException("目录必选！");
        }
        if (Objects.equals(dataSourceDTO.getId(), dataSourceDTO.getPid())) {
            DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
        }
        requireDatasourceAccess(dataSourceDTO.getId());
        if (dataSourceDTO.getPid() != 0) {
            requireDatasourceAccess(dataSourceDTO.getPid());
            List<Long> pidList = dataSourceManage.getPidList(dataSourceDTO.getPid());
            if (pidList.contains(dataSourceDTO.getId())) {
                DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
            }
        }
        dataSourceManage.move(dataSourceDTO);
        return dataSourceDTO;
    }

    @Transactional
    public DatasourceDTO reName(BusiRenameRequest busiDsRequest) {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        if (StringUtils.isEmpty(dataSourceDTO.getName())) {
            DEException.throwException("名称不能为空！");
        }
        requireDatasourceAccess(dataSourceDTO.getId());
        CoreDatasource datasource = dataSourceManage.getDatasource(dataSourceDTO.getId());
        datasource.setName(dataSourceDTO.getName());
        dataSourceDTO.setPid(datasource.getPid());
        dataSourceManage.checkName(dataSourceDTO);
        dataSourceManage.innerEditName(datasource);
        return dataSourceDTO;
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.CREATE, st = LogST.DATASOURCE)
    @Transactional
    public DatasourceDTO createFolder(BusiCreateFolderRequest busiDsRequest) {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        if (ObjectUtils.isNotEmpty(dataSourceDTO.getPid()) && !Objects.equals(dataSourceDTO.getPid(), 0L)) {
            requireDatasourceAccess(dataSourceDTO.getPid());
        }
        dataSourceDTO.setCreateTime(System.currentTimeMillis());
        dataSourceDTO.setUpdateTime(System.currentTimeMillis());
        dataSourceDTO.setType(dataSourceDTO.getNodeType());
        dataSourceDTO.setId(IDUtils.snowID());
        dataSourceDTO.setConfiguration("");
        dataSourceManage.innerSave(dataSourceDTO);
        return dataSourceDTO;
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.CREATE, st = LogST.DATASOURCE)
    @Transactional
    @Override
    public DatasourceDTO save(BusiDsRequest busiDsRequest) throws DEException {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        if (dataSourceDTO.getId() != null && dataSourceDTO.getId() > 0) {
            return update(busiDsRequest);
        }
        if (StringUtils.isNotEmpty(dataSourceDTO.getConfiguration())) {
            dataSourceDTO.setConfiguration(decodeBase64RequestValue(dataSourceDTO.getConfiguration(), "数据源配置"));
        }
        if (ObjectUtils.isNotEmpty(dataSourceDTO.getPid()) && !Objects.equals(dataSourceDTO.getPid(), 0L)) {
            requireDatasourceAccess(dataSourceDTO.getPid());
        }
        preCheckDs(dataSourceDTO);
        dataSourceDTO.setId(IDUtils.snowID());
        dataSourceDTO.setCreateTime(System.currentTimeMillis());
        dataSourceDTO.setUpdateTime(System.currentTimeMillis());
        try {
            checkDatasourceStatus(dataSourceDTO);
        } catch (Exception ignore) {
            dataSourceDTO.setStatus("Error");
        }
        dataSourceDTO.setTaskStatus(TaskStatus.WaitingForExecution.name());
        dataSourceDTO.setCreateBy(AuthUtils.getUser().getUserId().toString());
        dataSourceDTO.setUpdateBy(AuthUtils.getUser().getUserId());

        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        dataSourceManage.innerSave(dataSourceDTO);

        if (!dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.name())
                && !dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.Excel.name())) {
            calciteProvider.update(dataSourceDTO);
        }

        if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(dataSourceDTO);
            List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
            for (DatasetTableDTO table : tables) {
                datasourceRequest.setTable(table.getTableName());
                List<TableField> tableFields = ExcelUtils.getTableFields(datasourceRequest);
                try {
                    datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
                } catch (Exception e) {
                    if (e.getMessage().toLowerCase().contains("Row size too large".toLowerCase())) {
                        DEException.throwException("文本内容超出最大支持范围： " + datasourceRequest.getTable() + ", " + e.getMessage());
                    } else {
                        DEException.throwException("Failed to create table " + datasourceRequest.getTable() + ", " + e.getMessage());
                    }
                }
            }
            datasourceSyncManage.extractExcelData(coreDatasource, "all_scope");
        } else if (dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(dataSourceDTO);
            List<DatasetTableDTO> tables = (List<DatasetTableDTO>) invokeMethod(coreDatasource.getType(), "getApiTables", DatasourceRequest.class, datasourceRequest);
            checkName(tables.stream().map(DatasetTableDTO::getName).collect(Collectors.toList()));
            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                List<TableField> tableFields = (List<TableField>) invokeMethod(coreDatasource.getType(), "getTableFields", DatasourceRequest.class, datasourceRequest);
                try {
                    datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
                } catch (Exception e) {
                    DEException.throwException("Failed to create table " + datasourceRequest.getTable() + ": " + e.getMessage());
                }
            }

            CoreDatasourceTask coreDatasourceTask = new CoreDatasourceTask();
            BeanUtils.copyBean(coreDatasourceTask, dataSourceDTO.getSyncSetting());
            coreDatasourceTask.setName(coreDatasource.getName() + "-task");
            coreDatasourceTask.setDsId(coreDatasource.getId());
            if (coreDatasourceTask.getStartTime() == null) {
                coreDatasourceTask.setStartTime(System.currentTimeMillis() - 20 * 1000);
            }
            if (Strings.CI.equals(coreDatasourceTask.getSyncRate(), RIGHTNOW.toString())) {
                coreDatasourceTask.setCron(null);
            } else {
                if (coreDatasourceTask.getEndTime() != null && coreDatasourceTask.getEndTime() > 0 && coreDatasourceTask.getStartTime() > coreDatasourceTask.getEndTime()) {
                    DEException.throwException("结束时间不能小于开始时间！");
                }
            }
            coreDatasourceTask.setTaskStatus(TaskStatus.WaitingForExecution.name());
            datasourceTaskServer.insert(coreDatasourceTask);
            datasourceSyncManage.addSchedule(coreDatasourceTask);
        } else if (dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.ExcelRemote.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(dataSourceDTO);
            List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
            for (DatasetTableDTO table : tables) {
                datasourceRequest.setTable(table.getTableName());
                List<TableField> tableFields = ExcelUtils.getTableFields(datasourceRequest);
                try {
                    datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
                } catch (Exception e) {
                    if (e.getMessage().toLowerCase().contains("Row size too large".toLowerCase())) {
                        DEException.throwException("文本内容超出最大支持范围： " + datasourceRequest.getTable() + ", " + e.getMessage());
                    } else {
                        DEException.throwException("Failed to create table " + datasourceRequest.getTable() + ", " + e.getMessage());
                    }
                }
            }
            CoreDatasourceTask coreDatasourceTask = new CoreDatasourceTask();
            BeanUtils.copyBean(coreDatasourceTask, dataSourceDTO.getSyncSetting());
            coreDatasourceTask.setName(coreDatasource.getName() + "-task");
            coreDatasourceTask.setDsId(coreDatasource.getId());
            if (coreDatasourceTask.getStartTime() == null) {
                coreDatasourceTask.setStartTime(System.currentTimeMillis() - 20 * 1000);
            }
            if (Strings.CI.equals(coreDatasourceTask.getSyncRate(), RIGHTNOW.toString())) {
                coreDatasourceTask.setCron(null);
            } else {
                if (coreDatasourceTask.getEndTime() != null && coreDatasourceTask.getEndTime() > 0 && coreDatasourceTask.getStartTime() > coreDatasourceTask.getEndTime()) {
                    DEException.throwException("结束时间不能小于开始时间！");
                }
            }
            coreDatasourceTask.setTaskStatus(TaskStatus.WaitingForExecution.name());
            datasourceTaskServer.insert(coreDatasourceTask);
            datasourceSyncManage.addSchedule(coreDatasourceTask);
        } else {
            checkParams(dataSourceDTO.getConfiguration());
            calciteProvider.update(dataSourceDTO);
        }
        return dataSourceDTO;
    }

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASOURCE)
    @Transactional
    @Override
    public DatasourceDTO update(BusiDsRequest busiDsRequest) throws DEException {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        Long pk = null;
        if (ObjectUtils.isEmpty(pk = dataSourceDTO.getId())) {
            return save(busiDsRequest);
        }
        DatasourceDTO sourceData = dataSourceManage.getDs(pk);
        requireDatasourceAccess(pk);
        dataSourceDTO.setConfiguration(decodeBase64RequestValue(dataSourceDTO.getConfiguration(), "数据源配置"));
        dataSourceDTO.setPid(sourceData.getPid());
        preCheckDs(dataSourceDTO);

        dataSourceDTO.setUpdateTime(System.currentTimeMillis());
        dataSourceDTO.setUpdateBy(AuthUtils.getUser().getUserId());
        try {
            checkDatasourceStatus(dataSourceDTO);
        } catch (Exception e) {
            dataSourceDTO.setStatus("Error");
        }

        CoreDatasource requestDatasource = new CoreDatasource();
        BeanUtils.copyBean(requestDatasource, dataSourceDTO);

        DatasourceRequest sourceTableRequest = new DatasourceRequest();
        sourceTableRequest.setDatasource(sourceData);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dataSourceDTO);
        List<String> toCreateTables = new ArrayList<>();
        List<String> toDeleteTables = new ArrayList<>();
        if (dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.name()) || dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.ExcelRemote.name())) {
            requestDatasource.setEnableDataFill(null);
            List<DatasetTableDTO> sourceTableDTOs = dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.name()) ? (List<DatasetTableDTO>) invokeMethod(sourceData.getType(), "getApiTables", DatasourceRequest.class, sourceTableRequest) : ExcelUtils.getTables(sourceTableRequest);
            List<String> sourceTables = sourceTableDTOs.stream().map(DatasetTableDTO::getTableName).toList();
            List<DatasetTableDTO> datasetTableDTOS = dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.name()) ? (List<DatasetTableDTO>) invokeMethod(sourceData.getType(), "getApiTables", DatasourceRequest.class, datasourceRequest) : ExcelUtils.getTables(datasourceRequest);
            List<String> tables = datasetTableDTOS.stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());


            checkName(datasetTableDTOS.stream().map(DatasetTableDTO::getName).collect(Collectors.toList()));
            toCreateTables = tables.stream().filter(table -> !sourceTables.contains(table)).collect(Collectors.toList());
            toDeleteTables = sourceTables.stream().filter(table -> !tables.contains(table)).collect(Collectors.toList());
            for (String table : tables) {
                for (String sourceTable : sourceTables) {
                    if (table.equals(sourceTable)) {
                        datasourceRequest.setTable(table);
                        List<TableField> tableFieldList = dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.name()) ? (List<TableField>) invokeMethod(datasourceRequest.getDatasource().getType(), "getTableFields", DatasourceRequest.class, datasourceRequest) : ExcelUtils.getTableFields(datasourceRequest);
                        List<String> tableFields = tableFieldList.stream().map(TableField::getName).sorted().collect(Collectors.toList());
                        sourceTableRequest.setTable(sourceTable);
                        List<TableField> sourceTableFieldList = dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.name()) ? (List<TableField>) invokeMethod(sourceTableRequest.getDatasource().getType(), "getTableFields", DatasourceRequest.class, sourceTableRequest) : ExcelUtils.getTableFields(sourceTableRequest);
                        List<String> sourceTableFields = sourceTableFieldList.stream().map(TableField::getName).sorted().collect(Collectors.toList());
                        if (!String.join(",", tableFields).equals(String.join(",", sourceTableFields))) {
                            toDeleteTables.add(table);
                            toCreateTables.add(table);
                        }
                    }
                }
            }
            CoreDatasourceTask coreDatasourceTask = new CoreDatasourceTask();
            BeanUtils.copyBean(coreDatasourceTask, dataSourceDTO.getSyncSetting());
            coreDatasourceTask.setName(requestDatasource.getName() + "-task");
            coreDatasourceTask.setDsId(requestDatasource.getId());
            if (Strings.CI.equals(coreDatasourceTask.getSyncRate(), RIGHTNOW.toString())) {
                coreDatasourceTask.setStartTime(System.currentTimeMillis() - 20 * 1000);
                coreDatasourceTask.setCron(null);
            } else {
                if (coreDatasourceTask.getEndTime() != null && coreDatasourceTask.getEndTime() > 0 && coreDatasourceTask.getStartTime() > coreDatasourceTask.getEndTime()) {
                    DEException.throwException("结束时间不能小于开始时间！");
                }
            }
            coreDatasourceTask.setTaskStatus(TaskStatus.WaitingForExecution.toString());
            datasourceTaskServer.update(coreDatasourceTask);
            for (String deleteTable : toDeleteTables) {
                try {
                    datasourceSyncManage.dropEngineTable(deleteTable);
                } catch (Exception e) {
                    DEException.throwException("Failed to drop table " + deleteTable);
                }
            }
            for (String toCreateTable : toCreateTables) {
                datasourceRequest.setTable(toCreateTable);
                try {
                    datasourceSyncManage.createEngineTable(toCreateTable, dataSourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.name()) ? (List<TableField>) invokeMethod(sourceTableRequest.getDatasource().getType(), "getTableFields", DatasourceRequest.class, datasourceRequest) : ExcelUtils.getTableFields(datasourceRequest));
                } catch (Exception e) {
                    DEException.throwException("Failed to create table " + toCreateTable + ", " + e.getMessage());
                }
            }
            datasourceSyncManage.deleteSchedule(datasourceTaskServer.selectByDSId(dataSourceDTO.getId()));
            datasourceSyncManage.addSchedule(coreDatasourceTask);
            dataSourceManage.checkName(dataSourceDTO);
            dataSourceManage.innerEdit(requestDatasource);
        } else if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            requestDatasource.setEnableDataFill(null);
            List<String> sourceTables = ExcelUtils.getTables(sourceTableRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            List<String> tables = ExcelUtils.getTables(datasourceRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            if (Objects.equals(dataSourceDTO.getEditType(), replace)) {
                toCreateTables = tables;
                toDeleteTables = sourceTables.stream().filter(s -> tables.contains(s)).collect(Collectors.toList());
                for (String deleteTable : toDeleteTables) {
                    try {
                        datasourceSyncManage.dropEngineTable(deleteTable);
                    } catch (Exception ignore) {
                    }
                }
                for (String toCreateTable : toCreateTables) {
                    datasourceRequest.setTable(toCreateTable);
                    try {
                        datasourceSyncManage.createEngineTable(toCreateTable, ExcelUtils.getTableFields(datasourceRequest));
                    } catch (Exception e) {
                        DEException.throwException("Failed to create table " + toCreateTable + ", " + e.getMessage());
                    }
                }
                datasourceSyncManage.extractExcelData(requestDatasource, "all_scope");
                dataSourceManage.checkName(dataSourceDTO);
                ExcelUtils.mergeSheets(requestDatasource, sourceData);
                dataSourceManage.innerEdit(requestDatasource);
            } else {
                datasourceSyncManage.extractExcelData(requestDatasource, "add_scope");
                ExcelUtils.mergeSheets(requestDatasource, sourceData);
                dataSourceManage.checkName(dataSourceDTO);
                dataSourceManage.innerEdit(requestDatasource);
            }
        } else {
            checkParams(dataSourceDTO.getConfiguration());
            dataSourceManage.checkName(dataSourceDTO);
            dataSourceManage.innerEdit(requestDatasource);
            calciteProvider.update(dataSourceDTO);
        }
        return dataSourceDTO;
    }


    @Override
    public List<DatasourceConfiguration.DatasourceType> datasourceTypes() {
        return Arrays.asList(DatasourceConfiguration.DatasourceType.values());
    }

    @Override
    public DatasourceDTO validate(BusiDsRequest busiDsRequest) throws DEException {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        dataSourceDTO.setConfiguration(decodeBase64RequestValue(dataSourceDTO.getConfiguration(), "数据源配置"));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        checkDatasourceConfigurationComplete(dataSourceDTO);
        checkDatasourceStatus(dataSourceDTO);
        DatasourceDTO result = new DatasourceDTO();
        result.setType(dataSourceDTO.getType());
        result.setStatus(dataSourceDTO.getStatus());
        return result;
    }

    @Override
    public List<String> getSchema(BusiDsRequest busiDsRequest) throws DEException {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        dataSourceDTO.setConfiguration(decodeBase64RequestValue(dataSourceDTO.getConfiguration(), "数据源配置"));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dataSourceDTO);
        Provider provider = ProviderFactory.getProvider(dataSourceDTO.getType());
        return provider.getSchema(datasourceRequest);
    }

    @Override
    public DatasourceDTO hidePw(Long datasourceId) throws DEException {
        return getDatasourceDTOById(datasourceId, true);
    }

    @Override
    public DatasourceDTO getSimpleDs(Long datasourceId) throws DEException {
        CoreDatasource datasource = requireDatasourceAccess(datasourceId);
        if (datasource == null) {
            DEException.throwException(Translator.get("i18n_datasource_not_exists"));
        }
        if (datasource.getType().contains("API")) {
            datasource.setConfiguration("[]");
        } else {
            datasource.setConfiguration("");
        }
        datasource.setConfiguration("");
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, datasource);
        return datasourceDTO;
    }

    @Override
    public DatasourceDTO get(Long datasourceId) throws DEException {
        return getDatasourceDTOById(datasourceId, false);
    }

    @Override
    public DatasourceDTO innerGet(Long datasourceId) throws DEException {
        return getDatasourceDTOById(datasourceId, false);
    }

    @Override
    public String getName(Long datasourceId) throws DEException {
        CoreDatasource datasource = dataSourceManage.getCoreDatasource(datasourceId);
        if (datasource == null) {
            DEException.throwException(Translator.get("i18n_datasource_not_exists"));
        }
        return datasource.getName();
    }

    @Override
    public List<DatasourceDTO> innerList(List<Long> ids, List<String> types) throws DEException {
        List<DatasourceDTO> list = new ArrayList<>();
        LambdaQueryWrapper<CoreDatasource> queryWrapper = new LambdaQueryWrapper<>();
        if (ids != null) {
            if (ids.isEmpty()) {
                return list;
            } else {
                queryWrapper.in(CoreDatasource::getId, ids);
            }
        }
        if (types != null) {
            if (types.isEmpty()) {
                return list;
            } else {
                queryWrapper.in(CoreDatasource::getType, types);
            }
        }
        List<CoreDatasource> dsList = datasourceMapper.selectList(queryWrapper);

        for (CoreDatasource datasource : dsList) {
            DatasourceDTO datasourceDTO = new DatasourceDTO();
            BeanUtils.copyBean(datasourceDTO, datasource);

            if (datasourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.toString())) {
                List<ApiDefinition> apiDefinitionList = JsonUtil.parseList(datasourceDTO.getConfiguration(), listTypeReference);
                int success = 0;
                for (ApiDefinition apiDefinition : apiDefinitionList) {
                    String status = null;
                    if (StringUtils.isNotEmpty(datasourceDTO.getStatus())) {
                        JsonNode jsonNode = null;
                        try {
                            jsonNode = objectMapper.readTree(datasourceDTO.getStatus());
                            for (JsonNode node : jsonNode) {
                                if (node.get("name").asText().equals(apiDefinition.getName())) {
                                    status = node.get("status").asText();
                                }
                            }
                            apiDefinition.setStatus(status);
                        } catch (Exception ignore) {
                        }
                    }
                    if (StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")) {
                        success++;
                    }
                }
                if (success == apiDefinitionList.size()) {
                    datasourceDTO.setStatus("Success");
                } else {
                    if (success > 0 && success < apiDefinitionList.size()) {
                        datasourceDTO.setStatus("Warning");
                    } else {
                        datasourceDTO.setStatus("Error");
                    }
                }
            }

            list.add(datasourceDTO);
        }
        return list;
    }

    @Override
    public boolean perDelete(Long id) {
        if (relationManage != null) {
            Long count = relationManage.getDsResource(id);
            if (count > 0) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    @DeLog(id = "#p0", ot = LogOT.DELETE, st = LogST.DATASOURCE)
    @Override
    public void delete(Long datasourceId) throws DEException {
        requireDatasourceAccess(datasourceId);
        Objects.requireNonNull(io.crest.utils.CommonBeanFactory.getBean(DatasourceServer.class)).recursionDel(datasourceId);
    }

    public void recursionDel(Long datasourceId) throws DEException {
        CoreDatasource coreDatasource = dataSourceManage.getDatasource(datasourceId);
        if (ObjectUtils.isEmpty(coreDatasource)) {
            return;
        }
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasourceDTO);
            List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
            for (DatasetTableDTO table : tables) {
                datasourceRequest.setTable(table.getTableName());
                try {
                    datasourceSyncManage.dropEngineTable(datasourceRequest.getTable());
                } catch (Exception e) {
                    DEException.throwException("Failed to drop table " + datasourceRequest.getTable());
                }
            }
        }
        if (coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.API.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasourceDTO);
            List<DatasetTableDTO> tables = (List<DatasetTableDTO>) invokeMethod(coreDatasource.getType(), "getApiTables", DatasourceRequest.class, datasourceRequest);
            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                try {
                    datasourceSyncManage.dropEngineTable(datasourceRequest.getTable());
                } catch (Exception e) {
                    DEException.throwException("Failed to drop table " + datasourceRequest.getTable());
                }

            }

            datasourceTaskServer.deleteByDSId(datasourceId);
        }
        datasourceMapper.deleteById(datasourceId);
        if (notFullDs.stream().allMatch(e -> !coreDatasource.getType().contains(e))) {
            calciteProvider.delete(coreDatasource);
        }

        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.folder.name())) {
            QueryWrapper<CoreDatasource> wrapper = new QueryWrapper<>();
            wrapper.eq("pid", datasourceId);
            List<CoreDatasource> coreDatasources = datasourceMapper.selectList(wrapper);
            if (ObjectUtils.isNotEmpty(coreDatasources)) {
                for (CoreDatasource record : coreDatasources) {
                    delete(record.getId());
                }
            }
        }
    }


    @Override
    public DatasourceDTO validate(Long datasourceId) throws DEException {
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceManage.getCoreDatasource(datasourceId));
        return validate(coreDatasource);
    }

    public void addJob(List<CoreSysSetting> sysSettings) {
        String type = "minute";
        String interval = "30";
        for (CoreSysSetting sysSetting : sysSettings) {
            if (sysSetting.getPkey().equalsIgnoreCase("basic.dsExecuteTime")) {
                type = sysSetting.getPval();
            }
            if (sysSetting.getPkey().equalsIgnoreCase("basic.dsIntervalTime")) {
                interval = sysSetting.getPval();
            }
        }
        String cron = "";
        switch (type) {
            case "hour":
                cron = "0 0 0/hour *  * ? *".replace("hour", interval.toString());
                break;
            default:
                cron = "0 0/minute * *  * ? *".replace("minute", interval.toString());
        }
        scheduleManager.addOrUpdateCronJob(new JobKey("Datasource", "check_status"),
                new TriggerKey("Datasource", "check_status"),
                CheckDsStatusJob.class,
                cron, new Date(System.currentTimeMillis()), null, new JobDataMap());
    }

    @Override
    public List<BusiNodeVO> tree(BusiNodeRequest request) throws DEException {
        return dataSourceManage.tree(request);
    }

    @Override
    public List<DatasetTableDTO> getTables(DatasetTableDTO datasetTableDTO) throws DEException {
        CoreDatasource coreDatasource = requireDatasourceAccess(datasetTableDTO.getDatasourceId());
        if (coreDatasource == null) {
            DEException.throwException("无效数据源！");
        }
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasourceDTO);
        if (coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.API.name())) {
            List<DatasetTableDTO> datasetTableDTOS = (List<DatasetTableDTO>) invokeMethod(coreDatasource.getType(), "getApiTables", DatasourceRequest.class, datasourceRequest);
            return datasetTableDTOS;
        }
        if (coreDatasource.getType().contains("Excel")) {
            return ExcelUtils.getTables(datasourceRequest);
        }
        Provider provider = ProviderFactory.getProvider(datasourceDTO.getType());
        List<DatasetTableDTO> tables = provider.getTables(datasourceRequest);
        if (StringUtils.endsWithIgnoreCase(coreDatasource.getType(), DatasourceConfiguration.DatasourceType.oracle.name())) {
            return tables.stream().filter(table -> !isOracleRecycleBinTable(table)).collect(Collectors.toList());
        }
        return tables;
    }

    private boolean isOracleRecycleBinTable(DatasetTableDTO table) {
        if (table == null) {
            return false;
        }
        return isOracleRecycleBinName(table.getTableName()) || isOracleRecycleBinName(table.getName());
    }

    private boolean isOracleRecycleBinName(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return false;
        }
        String normalized = StringUtils.removeEnd(StringUtils.removeStart(tableName.trim(), "\""), "\"");
        return ORACLE_RECYCLE_BIN_TABLE_PATTERN.matcher(normalized).matches();
    }

    @Override
    public List<DatasetTableDTO> getTableStatus(DatasetTableDTO datasetTableDTO) throws DEException {
        CoreDatasource coreDatasource = dataSourceManage.getCoreDatasource(datasetTableDTO.getDatasourceId());
        if (coreDatasource == null) {
            DEException.throwException("无效数据源！");
        }
        List<DatasetTableDTO> datasetTableDTOS = new ArrayList<>();
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasourceDTO);
        if (coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.API.name())) {
            datasetTableDTOS = (List<DatasetTableDTO>) invokeMethod(coreDatasource.getType(), "getApiTables", DatasourceRequest.class, datasourceRequest);
        }
        if (coreDatasource.getType().equalsIgnoreCase(DatasourceConfiguration.DatasourceType.ExcelRemote.name())) {
            datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
        }
        datasetTableDTOS.forEach(datasetTableDTO1 -> {
            CoreDatasourceTaskLog log = datasourceTaskServer.lastSyncLogForTable(datasetTableDTO.getDatasourceId(), datasetTableDTO1.getTableName());
            if (log != null) {
                datasetTableDTO1.setLastUpdateTime(log.getStartTime());
                datasetTableDTO1.setStatus(log.getTaskStatus());
            }
        });
        return datasetTableDTOS;
    }

    @Override
    public List<TableField> getTableField(Map<String, String> req) throws DEException {
        String tableName = req.get("tableName");
        String datasourceId = req.get("datasourceId");
        DatasetTableDTO datasetTableDTO = new DatasetTableDTO();
        datasetTableDTO.setDatasourceId(Long.valueOf(datasourceId));
        if (!getTables(datasetTableDTO).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList()).contains(tableName)) {
            DEException.throwException("无效的表名！");
        }
        CoreDatasource coreDatasource = dataSourceManage.getCoreDatasource(Long.parseLong(datasourceId));
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(transDTO(coreDatasource));
        if (coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.API.name()) || coreDatasource.getType().contains("Excel")) {
            datasourceRequest.setDatasource(transDTO(engineManage.getDeEngine()));
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            BeanUtils.copyBean(datasourceSchemaDTO, engineManage.getDeEngine());
            datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
            datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
            datasourceRequest.setQuery(TableUtils.tableName2Sql(datasourceSchemaDTO, tableName) + " LIMIT 0 OFFSET 0");
            datasourceRequest.setTable(tableName);
            Provider provider = ProviderFactory.getProvider(datasourceSchemaDTO.getType());
            List<TableField> tableFields = (List<TableField>) provider.fetchTableField(datasourceRequest);
            return tableFields.stream().filter(tableField -> {
                return !tableField.getOriginName().equalsIgnoreCase("dataease_uuid");
            }).collect(Collectors.toList());
        }

        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
        datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
        datasourceRequest.setQuery(TableUtils.tableName2Sql(datasourceSchemaDTO, tableName) + " LIMIT 0 OFFSET 0");
        datasourceRequest.setTable(tableName);
        Provider provider = ProviderFactory.getProvider(datasourceSchemaDTO.getType());
        return (List<TableField>) provider.fetchTableField(datasourceRequest);
    }

    @Override
    public void syncApiTable(Map<String, String> req) throws DEException {
        String tableName = req.get("tableName");
        String name = req.get("name");
        Long datasourceId = Long.valueOf(req.get("datasourceId"));
        datasourceSyncManage.extractDataForTable(datasourceId, name, tableName, datasourceTaskServer.selectByDSId(datasourceId).getUpdateType());
    }

    @Override
    public void syncApiDs(Map<String, String> req) throws Exception {
        Long datasourceId = Long.valueOf(req.get("datasourceId"));
        CoreDatasourceTask coreDatasourceTask = datasourceTaskServer.selectByDSId(datasourceId);
        CoreDatasource coreDatasource = dataSourceManage.getCoreDatasource(datasourceId);
        DatasourceServer.UpdateType updateType = DatasourceServer.UpdateType.valueOf(coreDatasourceTask.getUpdateType());
        datasourceSyncManage.extractedData(null, coreDatasource, updateType, MANUAL.toString());
    }

    private static final Integer replace = 0;
    private static final Integer append = 1;

    public ExcelFileData uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("id") long datasourceId, @RequestParam("editType") Integer editType) throws DEException {
        validateExcelUploadFile(file);
        CoreDatasource coreDatasource = null;
        if (ObjectUtils.isNotEmpty(datasourceId) && 0L != datasourceId) {
            coreDatasource = dataSourceManage.getCoreDatasource(datasourceId);
        }
        ExcelUtils excelUtils = new ExcelUtils();
        ExcelFileData excelFileData = excelUtils.excelSaveAndParse(file, String.valueOf(AuthUtils.getUser().getUserId()));

        if (Objects.equals(editType, append)) { //按照excel sheet 名称匹配，替换：0；追加：1
            if (coreDatasource != null) {
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(transDTO(coreDatasource));
                List<DatasetTableDTO> datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
                List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
                for (ExcelSheetData sheet : excelFileData.getSheets()) {
                    for (DatasetTableDTO datasetTableDTO : datasetTableDTOS) {
                        if (excelDataTableName(datasetTableDTO.getTableName()).equals(sheet.getTableName())) {
                            List<TableField> newTableFields = sheet.getFields();
                            datasourceRequest.setTable(datasetTableDTO.getTableName());
                            List<TableField> oldTableFields = ExcelUtils.getTableFields(datasourceRequest);
                            if (isEqual(newTableFields, oldTableFields)) {
                                sheet.setDeTableName(datasetTableDTO.getTableName());
                                excelSheetDataList.add(sheet);
                            }
                        }
                    }
                }
                excelFileData.setSheets(excelSheetDataList);
            }
        } else {
            // 替换
            if (coreDatasource != null) {
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(transDTO(coreDatasource));
                List<DatasetTableDTO> datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
                for (ExcelSheetData sheet : excelFileData.getSheets()) {
                    for (DatasetTableDTO datasetTableDTO : datasetTableDTOS) {
                        if (excelDataTableName(datasetTableDTO.getTableName()).equals(sheet.getTableName())) {
                            sheet.setDeTableName(datasetTableDTO.getTableName());
                        }
                    }
                }
            }
        }

        for (ExcelSheetData sheet : excelFileData.getSheets()) {
            for (int i = 0; i < sheet.getFields().size() - 1; i++) {
                for (int j = i + 1; j < sheet.getFields().size(); j++) {
                    if (sheet.getFields().get(i).getName().equalsIgnoreCase(sheet.getFields().get(j).getName())) {
                        DEException.throwException(sheet.getExcelLabel() + Translator.get("i18n_field_name_repeat") + sheet.getFields().get(i).getName());
                    }
                }
            }
        }
        return excelFileData;
    }

    public ExcelFileData loadRemoteFile(RemoteExcelRequest remoteExcelRequest) throws DEException, IOException {
        // SSRF 防护：验证 URL 安全性
        SsrfProtection.validateUrl(remoteExcelRequest.getUrl());

        remoteExcelRequest.setUserName(decodeBase64RequestValue(remoteExcelRequest.getUserName(), "远程 Excel 用户名"));
        remoteExcelRequest.setPasswd(decodeBase64RequestValue(remoteExcelRequest.getPasswd(), "远程 Excel 密码"));
        ExcelFileData excelFileData = new ExcelUtils().parseRemoteExcel(remoteExcelRequest);
        CoreDatasource coreDatasource = null;
        if (ObjectUtils.isNotEmpty(remoteExcelRequest.getDatasourceId()) && 0L != remoteExcelRequest.getDatasourceId()) {
            coreDatasource = dataSourceManage.getCoreDatasource(remoteExcelRequest.getDatasourceId());
        }
        if (coreDatasource != null) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(transDTO(coreDatasource));
            List<DatasetTableDTO> datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
            for (ExcelSheetData sheet : excelFileData.getSheets()) {
                for (DatasetTableDTO datasetTableDTO : datasetTableDTOS) {
                    if (excelDataTableName(datasetTableDTO.getTableName()).equals(sheet.getTableName())) {
                        sheet.setDeTableName(datasetTableDTO.getTableName());
                    }
                }
            }
        }
        for (ExcelSheetData sheet : excelFileData.getSheets()) {
            for (int i = 0; i < sheet.getFields().size() - 1; i++) {
                for (int j = i + 1; j < sheet.getFields().size(); j++) {
                    if (sheet.getFields().get(i).getName().equalsIgnoreCase(sheet.getFields().get(j).getName())) {
                        DEException.throwException(sheet.getExcelLabel() + Translator.get("i18n_field_name_repeat") + sheet.getFields().get(i).getName());
                    }
                }
            }
        }
        return excelFileData;
    }

    private void validateExcelUploadFile(MultipartFile file) {
        String fileName = file == null ? null : file.getOriginalFilename();
        String suffix = StringUtils.substringAfterLast(StringUtils.defaultString(fileName), ".").toLowerCase(Locale.ROOT);
        if (!EXCEL_UPLOAD_SUFFIXES.contains(suffix)) {
            DEException.throwException(Translator.get("i18n_unsupported_file_format"));
        }
        FileUtils.validateUploadFilename(fileName);
    }


    private boolean isEqual(List<TableField> newTableFields, List<TableField> oldTableFields) {
        if (CollectionUtils.isEmpty(newTableFields) || CollectionUtils.isEmpty(oldTableFields)) {
            return false;
        }
        boolean isHistory = oldTableFields.stream().filter(tableField -> !tableField.isChecked()).collect(Collectors.toList()).size() == oldTableFields.size();
        if (isHistory) {
            oldTableFields.forEach(tableField -> tableField.setChecked(true));
        }
        newTableFields.forEach(tableField -> tableField.setChecked(false));
        for (TableField oldField : oldTableFields) {
            if (!oldField.isChecked()) {
                continue;
            }
            boolean find = false;
            for (TableField newField : newTableFields) {
                if (oldField.getName().equals(newField.getName())) {
                    find = true;
                    newField.setChecked(oldField.isChecked());
                    newField.setPrimaryKey(oldField.isPrimaryKey());
                    newField.setLength(oldField.getLength());
                    break;
                }
            }
            if (!find) {
                return find;
            }
        }
        return true;
    }

    private void mergeFields(List<TableField> oldFields, List<TableField> newFields) {
        newFields.forEach(tableField -> tableField.setChecked(false));
        boolean isHistory = oldFields.stream().filter(tableField -> !tableField.isChecked()).collect(Collectors.toList()).size() == oldFields.size();
        if (isHistory) {
            oldFields.forEach(tableField -> tableField.setChecked(true));
        }
        for (TableField newField : newFields) {
            for (TableField oldField : oldFields) {
                if (oldField.getName().equals(newField.getName())) {
                    newField.setChecked(oldField.isChecked());
                    newField.setPrimaryKey(oldField.isPrimaryKey());
                    newField.setLength(oldField.getLength());
                }
            }
        }
    }

    private boolean isCsv(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return suffix.equalsIgnoreCase("csv");
    }

    public ApiDefinition checkApiDatasource(Map<String, String> request) throws DEException {
        ApiDefinition apiDefinition = JsonUtil.parseObject(decodeBase64RequestValue(request.get("data"), "API 数据源配置"), ApiDefinition.class);
        apiDefinition.setType("table");
        if (request.keySet().contains("type") && request.get("type").equals("apiStructure")) {
            apiDefinition.setShowApiStructure(true);
        }
        List<ApiDefinition> paramsList = JsonUtil.parseList(decodeBase64RequestValue(request.get("paramsList"), "API 参数配置"), listTypeReference);
        paramsList.add(apiDefinition);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        DatasourceDTO datasource = new DatasourceDTO();
        datasource.setConfiguration(JsonUtil.toJSONString(paramsList).toString());
        datasourceRequest.setDatasource(datasource);

        apiDefinition = (ApiDefinition) invokeMethod(request.get("dsType"), "checkApiDefinition", DatasourceRequest.class, datasourceRequest);
        if (apiDefinition.getRequest().getAuthManager() != null && StringUtils.isNotBlank(apiDefinition.getRequest().getAuthManager().getUsername()) && StringUtils.isNotBlank(apiDefinition.getRequest().getAuthManager().getPassword()) && apiDefinition.getRequest().getAuthManager().getVerification().equals("Basic Auth")) {
            apiDefinition.getRequest().getAuthManager().setUsername(new String(Base64.getEncoder().encode(apiDefinition.getRequest().getAuthManager().getUsername().getBytes())));
            apiDefinition.getRequest().getAuthManager().setPassword(new String(Base64.getEncoder().encode(apiDefinition.getRequest().getAuthManager().getPassword().getBytes())));
        }
        return apiDefinition;
    }

    private Map<String, Object> buildAccessTokenResult(String json) {
        if (ObjectUtils.isEmpty(json)) {
            DEException.throwException("get access token error");
        }
        Map<String, Object> resultMap = JsonUtil.parse(json, Map.class);
        if (Integer.parseInt(resultMap.get("code").toString()) != 0) {
            DEException.throwException(resultMap.get("msg").toString());
        }
        return resultMap;
    }

    private String buildAccessTokenParam(String appId, String appSecret) {
        Map<String, String> param = new HashMap<>();
        param.put("app_id", appId);
        param.put("app_secret", appSecret);
        return Objects.requireNonNull(JsonUtil.toJSONString(param)).toString();
    }

    private void preCheckDs(DatasourceDTO datasource) throws DEException {
        List<String> list = datasourceTypes().stream().map(DatasourceConfiguration.DatasourceType::getType).collect(Collectors.toList());
        if (pluginManage != null) {
            List<PluginDatasourceVO> pluginDatasourceList = pluginManage.queryPluginDs();
            pluginDatasourceList.forEach(ele -> list.add(ele.getType()));
        }

        if (!list.contains(datasource.getType())) {
            DEException.throwException("Datasource type not supported.");
        }
    }

    public void checkDatasourceStatus(DatasourceDTO coreDatasource) {
        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name()) || coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.folder.name())) {
            return;
        }
        try {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            String status = null;
            if (coreDatasource.getType().startsWith("API")) {
                status = (String) invokeMethod(coreDatasource.getType(), "checkAPIStatus", DatasourceRequest.class, datasourceRequest);
            } else if (coreDatasource.getType().startsWith("Excel")) {
                status = ExcelUtils.checkStatus(datasourceRequest);
            } else {
                Provider provider = ProviderFactory.getProvider(coreDatasource.getType());
                status = provider.checkStatus(datasourceRequest);
            }
            coreDatasource.setStatus(status);
        } catch (DEException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.debug(StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()));
            DEException.throwException(ResultCode.PARAM_IS_INVALID.code(), "数据源配置不完整或无法连接");
        }
    }


    @Override
    public Map<String, Object> previewDataWithLimit(Map<String, Object> req) throws DEException {
        String tableName = req.get("table").toString();
        Long id = Long.valueOf(req.get("id").toString());
        if (ObjectUtils.isEmpty(tableName) || ObjectUtils.isEmpty(id)) {
            return null;
        }
        CoreDatasource coreDatasource = requireDatasourceAccess(id);
        DatasetTableDTO datasetTableDTO = new DatasetTableDTO();
        datasetTableDTO.setDatasourceId(id);
        if (!getTables(datasetTableDTO).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList()).contains(tableName)) {
            DEException.throwException(Translator.get("i18n_invalid_table_name"));
        }
        String sql = "SELECT * FROM " + quoteIdentifier(tableName);
        if (coreDatasource.getType().contains("Excel")) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(transDTO(coreDatasource));
            datasourceRequest.setTable(tableName);
            List<TableField> tableFields = checkedExcelFields(ExcelUtils.getTableFields(datasourceRequest));
            String columns = tableFields.stream()
                    .map(TableField::getName)
                    .map(this::quoteIdentifier)
                    .collect(Collectors.joining(", "));
            if (StringUtils.isNotEmpty(columns)) {
                sql = "SELECT " + columns + " FROM " + quoteIdentifier(tableName);
            }
        }
        sql = new String(Base64.getEncoder().encode(sql.getBytes()));
        PreviewSqlDTO previewSqlDTO = new PreviewSqlDTO();
        previewSqlDTO.setSql(sql);
        previewSqlDTO.setDatasourceId(id);
        previewSqlDTO.setIsCross(false);
        return datasetDataManage.previewSql(previewSqlDTO);
    }

    @Override
    public ExcelDataPageVO excelDataPage(ExcelDataPageRequest request) throws DEException {
        if (request == null) {
            DEException.throwException("无效的 Excel 数据表");
        }
        ExcelEditContext context = buildExcelEditContext(request.getDatasourceId(), request.getTableName(), true);
        int page = Math.max(Optional.ofNullable(request.getPage()).orElse(1), 1);
        int pageSize = Math.max(Optional.ofNullable(request.getPageSize()).orElse(100), 1);
        pageSize = Math.min(pageSize, MAX_EXCEL_EDIT_PAGE_SIZE);
        int offset = (page - 1) * pageSize;

        String columns = context.fields.stream()
                .map(TableField::getName)
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));
        String query = "SELECT " + quoteIdentifier(EXCEL_ROW_ID_FIELD)
                + (StringUtils.isEmpty(columns) ? "" : ", " + columns)
                + " FROM " + quoteIdentifier(context.tableName)
                + " ORDER BY " + quoteIdentifier(EXCEL_ROW_ID_FIELD)
                + " LIMIT " + pageSize + " OFFSET " + offset;

        EngineRequest engineRequest = buildEngineRequest(context.engine, query);
        Map<String, Object> result;
        try {
            result = calciteProvider.fetchResultField(engineRequest);
        } catch (Exception e) {
            DEException.throwException(e);
            return null;
        }

        List<String[]> dataList = (List<String[]>) result.get("data");
        List<Map<String, Object>> rows = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dataList)) {
            for (String[] row : dataList) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put(EXCEL_EDIT_ROW_ID, row.length > 0 ? row[0] : null);
                for (int i = 0; i < context.fields.size(); i++) {
                    item.put(context.fields.get(i).getName(), i + 1 < row.length ? row[i + 1] : null);
                }
                rows.add(item);
            }
        }

        ExcelDataPageVO vo = new ExcelDataPageVO();
        vo.setFields(context.fields);
        vo.setRows(rows);
        vo.setTotal(fetchExcelEditTotal(context.engine, context.tableName));
        vo.setPage(page);
        vo.setPageSize(pageSize);
        return vo;
    }

    @Override
    public void saveExcelData(ExcelDataSaveRequest request) throws DEException {
        if (request == null) {
            DEException.throwException("无效的 Excel 数据表");
        }
        ExcelEditContext context = buildExcelEditContext(request.getDatasourceId(), request.getTableName(), true);
        List<Map<String, Object>> updates = Optional.ofNullable(request.getUpdates()).orElseGet(ArrayList::new);
        List<Map<String, Object>> inserts = Optional.ofNullable(request.getInserts()).orElseGet(ArrayList::new);
        List<String> deletes = Optional.ofNullable(request.getDeletes()).orElseGet(ArrayList::new);
        int changeCount = updates.size() + inserts.size() + deletes.size();
        if (changeCount == 0) {
            return;
        }
        if (changeCount > MAX_EXCEL_EDIT_BATCH_SIZE) {
            DEException.throwException("单次保存的数据行数不能超过 " + MAX_EXCEL_EDIT_BATCH_SIZE + " 行");
        }

        EngineRequest engineRequest = buildEngineRequest(context.engine, "");
        try {
            calciteProvider.execWithEngineTransaction(engineRequest, (connection, queryTimeout) -> {
                executeExcelDeletes(connection, queryTimeout, context.tableName, deletes);
                executeExcelUpdates(connection, queryTimeout, context.tableName, context.fields, updates);
                executeExcelInserts(connection, queryTimeout, context.tableName, context.fields, inserts);
            });
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    private ExcelEditContext buildExcelEditContext(Long datasourceId, String tableName, boolean ensureRowId) {
        if (ObjectUtils.isEmpty(datasourceId) || StringUtils.isBlank(tableName)) {
            DEException.throwException("无效的 Excel 数据表");
        }
        CoreDatasource coreDatasource = requireDatasourceAccess(datasourceId);
        if (coreDatasource == null || !Strings.CI.equals(coreDatasource.getType(), DatasourceConfiguration.DatasourceType.Excel.name())) {
            DEException.throwException("仅支持本地 Excel 数据源在线编辑");
        }
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(transDTO(coreDatasource));
        List<String> tableNames = ExcelUtils.getTables(datasourceRequest).stream()
                .map(DatasetTableDTO::getTableName)
                .collect(Collectors.toList());
        if (!tableNames.contains(tableName)) {
            DEException.throwException(Translator.get("i18n_invalid_table_name"));
        }
        datasourceRequest.setTable(tableName);
        List<TableField> tableFields = ExcelUtils.getTableFields(datasourceRequest);
        if (tableFields.stream().anyMatch(field -> Strings.CI.equals(field.getName(), EXCEL_ROW_ID_FIELD))) {
            DEException.throwException("字段名 " + EXCEL_ROW_ID_FIELD + " 为系统保留字段，暂不支持在线编辑");
        }
        CoreDeEngine engine = engineManage.info();
        if (ensureRowId) {
            ensureExcelRowId(engine, tableName);
        }
        return new ExcelEditContext(engine, tableName, checkedExcelFields(tableFields));
    }

    private List<TableField> checkedExcelFields(List<TableField> tableFields) {
        if (CollectionUtils.isEmpty(tableFields)) {
            return new ArrayList<>();
        }
        List<TableField> checkedFields = tableFields.stream()
                .filter(TableField::isChecked)
                .filter(field -> !Strings.CI.equals(field.getName(), EXCEL_ROW_ID_FIELD))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(checkedFields)) {
            return checkedFields;
        }
        return tableFields.stream()
                .filter(field -> !Strings.CI.equals(field.getName(), EXCEL_ROW_ID_FIELD))
                .collect(Collectors.toList());
    }

    private void ensureExcelRowId(CoreDeEngine engine, String tableName) {
        if (!hasEngineColumn(engine, tableName, EXCEL_ROW_ID_FIELD)) {
            executeEngineSql(engine, "ALTER TABLE " + quoteIdentifier(tableName)
                    + " ADD COLUMN " + quoteIdentifier(EXCEL_ROW_ID_FIELD) + " VARCHAR(64)");
        }
        String uuidExpression = Strings.CI.equals(engine.getType(), "mysql") ? "UUID()" : "RANDOM_UUID()";
        executeEngineSql(engine, "UPDATE " + quoteIdentifier(tableName)
                + " SET " + quoteIdentifier(EXCEL_ROW_ID_FIELD) + " = " + uuidExpression
                + " WHERE " + quoteIdentifier(EXCEL_ROW_ID_FIELD) + " IS NULL OR "
                + quoteIdentifier(EXCEL_ROW_ID_FIELD) + " = ''");
    }

    private boolean hasEngineColumn(CoreDeEngine engine, String tableName, String columnName) {
        try {
            Map<String, Object> result = calciteProvider.fetchResultField(buildEngineRequest(engine,
                    "SELECT * FROM " + quoteIdentifier(tableName) + " WHERE 1 = 0"));
            List<TableField> fields = (List<TableField>) result.get("fields");
            return fields.stream().anyMatch(field -> Strings.CI.equals(field.getOriginName(), columnName)
                    || Strings.CI.equals(field.getName(), columnName));
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return false;
    }

    private Long fetchExcelEditTotal(CoreDeEngine engine, String tableName) {
        try {
            Map<String, Object> result = calciteProvider.fetchResultField(buildEngineRequest(engine,
                    "SELECT COUNT(1) FROM " + quoteIdentifier(tableName)));
            List<String[]> data = (List<String[]>) result.get("data");
            if (CollectionUtils.isEmpty(data) || data.get(0).length == 0) {
                return 0L;
            }
            return Long.parseLong(data.get(0)[0]);
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return 0L;
    }

    private void executeEngineSql(CoreDeEngine engine, String sql) {
        try {
            calciteProvider.exec(buildEngineRequest(engine, sql));
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    private EngineRequest buildEngineRequest(CoreDeEngine engine, String sql) {
        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        engineRequest.setQuery(sql);
        return engineRequest;
    }

    private void executeExcelDeletes(Connection connection, int queryTimeout, String tableName, List<String> deletes) throws Exception {
        if (CollectionUtils.isEmpty(deletes)) {
            return;
        }
        // Identifiers are validated in quoteIdentifier; JDBC placeholders cannot bind table or column names.
        // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
        String sql = "DELETE FROM " + quoteIdentifier(tableName)
                + " WHERE " + quoteIdentifier(EXCEL_ROW_ID_FIELD) + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeout);
            for (String rowId : deletes) {
                statement.setString(1, normalizeRowId(rowId));
                // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
                statement.addBatch();
            }
            // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
            statement.executeBatch();
        }
    }

    private void executeExcelUpdates(Connection connection, int queryTimeout, String tableName, List<TableField> fields, List<Map<String, Object>> updates) throws Exception {
        if (CollectionUtils.isEmpty(updates)) {
            return;
        }
        if (CollectionUtils.isEmpty(fields)) {
            DEException.throwException("Excel 数据表没有可编辑字段");
        }
        // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
        String assignments = fields.stream()
                .map(field -> quoteIdentifier(field.getName()) + " = ?")
                .collect(Collectors.joining(", "));
        // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
        String sql = "UPDATE " + quoteIdentifier(tableName) + " SET " + assignments
                + " WHERE " + quoteIdentifier(EXCEL_ROW_ID_FIELD) + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeout);
            for (Map<String, Object> row : updates) {
                int index = bindExcelFieldValues(statement, fields, row, 1);
                statement.setString(index, normalizeRowId(row.get(EXCEL_EDIT_ROW_ID)));
                // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
                statement.addBatch();
            }
            // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
            statement.executeBatch();
        }
    }

    private void executeExcelInserts(Connection connection, int queryTimeout, String tableName, List<TableField> fields, List<Map<String, Object>> inserts) throws Exception {
        if (CollectionUtils.isEmpty(inserts)) {
            return;
        }
        if (CollectionUtils.isEmpty(fields)) {
            DEException.throwException("Excel 数据表没有可编辑字段");
        }
        List<String> columnNames = fields.stream().map(TableField::getName).collect(Collectors.toCollection(ArrayList::new));
        columnNames.add(EXCEL_ROW_ID_FIELD);
        // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
        String columns = columnNames.stream().map(this::quoteIdentifier).collect(Collectors.joining(", "));
        String placeholders = columnNames.stream().map(name -> "?").collect(Collectors.joining(", "));
        // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
        String sql = "INSERT INTO " + quoteIdentifier(tableName) + " (" + columns + ") VALUES (" + placeholders + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeout);
            for (Map<String, Object> row : inserts) {
                int index = bindExcelFieldValues(statement, fields, row, 1);
                statement.setString(index, UUID.randomUUID().toString());
                // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
                statement.addBatch();
            }
            // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
            statement.executeBatch();
        }
    }

    private int bindExcelFieldValues(PreparedStatement statement, List<TableField> fields, Map<String, Object> row, int startIndex) throws Exception {
        int index = startIndex;
        for (TableField field : fields) {
            Object normalized = normalizeExcelCellValue(field, excelCellValue(row, field));
            statement.setObject(index++, normalized);
        }
        return index;
    }

    private Object excelCellValue(Map<String, Object> row, TableField field) {
        if (row == null) {
            return null;
        }
        if (row.containsKey(field.getName())) {
            return row.get(field.getName());
        }
        return row.get(field.getOriginName());
    }

    private Object normalizeExcelCellValue(TableField field, Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        Integer type = field.getDeExtractType() == null ? field.getDeType() : field.getDeExtractType();
        try {
            if (Objects.equals(type, 2)) {
                return new BigDecimal(text).toBigIntegerExact().longValueExact();
            }
            if (Objects.equals(type, 3)) {
                return new BigDecimal(text);
            }
            if (Objects.equals(type, 4)) {
                if (Strings.CI.equalsAny(text, "true", "1", "是", "yes")) {
                    return 1;
                }
                if (Strings.CI.equalsAny(text, "false", "0", "否", "no")) {
                    return 0;
                }
                DEException.throwException("字段 " + field.getName() + " 需要布尔值");
            }
        } catch (ArithmeticException | NumberFormatException e) {
            DEException.throwException("字段 " + field.getName() + " 的数值格式不正确");
        }
        return text;
    }

    private String normalizeRowId(Object rowId) {
        if (rowId == null || StringUtils.isBlank(String.valueOf(rowId))) {
            DEException.throwException("缺少数据行标识，请刷新后重试");
        }
        return String.valueOf(rowId);
    }

    private String quoteIdentifier(String identifier) {
        if (StringUtils.isBlank(identifier)) {
            DEException.throwException("Illegal table name");
        }
        EngineProvider.validateSqlInjectionRisk(identifier);
        return "`" + identifier.replace("`", "``") + "`";
    }

    private static class ExcelEditContext {
        private final CoreDeEngine engine;
        private final String tableName;
        private final List<TableField> fields;

        private ExcelEditContext(CoreDeEngine engine, String tableName, List<TableField> fields) {
            this.engine = engine;
            this.tableName = tableName;
            this.fields = fields;
        }
    }

    @Override
    public List<String> latestUse() {
        List<String> types = new ArrayList<>();
        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("create_by", AuthUtils.getUser().getUserId());
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last(" limit 5");
        List<CoreDatasource> coreDatasources = datasourceMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(coreDatasources)) {
            return types;
        }
        for (CoreDatasource coreDatasource : coreDatasources) {
            if (!coreDatasource.getType().equalsIgnoreCase("folder") && !types.contains(coreDatasource.getType())) {
                types.add(coreDatasource.getType());
            }
        }
        return types;
    }

    public IPage<CoreDatasourceTaskLogDTO> listSyncRecord(int goPage, int pageSize, Long dsId) {
        QueryWrapper<CoreDatasourceTaskLogDTO> wrapper = new QueryWrapper<>();
        wrapper.eq("ds_id", dsId);
        wrapper.orderByDesc("start_time");
        Page<CoreDatasourceTaskLogDTO> page = new Page<>(goPage, pageSize);
        IPage<CoreDatasourceTaskLogDTO> pager = taskLogExtMapper.pager(page, wrapper);
        CoreDatasource coreDatasource = dataSourceManage.getCoreDatasource(dsId);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(transDTO(coreDatasource));
        List<DatasetTableDTO> datasetTableDTOS = new ArrayList<>();
        if (coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.API.toString())) {
            datasetTableDTOS = (List<DatasetTableDTO>) invokeMethod(coreDatasource.getType(), "getApiTables", DatasourceRequest.class, datasourceRequest);
        } else {
            datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
        }
        for (int i = 0; i < pager.getRecords().size(); i++) {
            for (int i1 = 0; i1 < datasetTableDTOS.size(); i1++) {
                if (pager.getRecords().get(i).getTableName().equalsIgnoreCase(datasetTableDTOS.get(i1).getTableName())) {
                    pager.getRecords().get(i).setName(datasetTableDTOS.get(i1).getName());
                }
            }
        }
        return pager;
    }


    public void updateDatasourceStatus() {
        QueryWrapper<CoreDatasource> wrapper = new QueryWrapper<>();
        wrapper.notIn("type", Arrays.asList("Excel", "folder"));
        List<CoreDatasource> datasources = datasourceMapper.selectList(wrapper);
        datasources.forEach(datasource -> {
            if (!syncDsIds.contains(datasource.getId())) {
                syncDsIds.add(datasource.getId());
                commonThreadPool.addTask(() -> {
                    try {
                        validate(datasource);
                    } catch (Exception e) {
                        LogUtil.error(e.getMessage(), e);
                    } finally {
                        syncDsIds.removeIf(id -> id.equals(datasource.getId()));
                    }
                });
            }
        });
    }

    public void updateStopJobStatus() {
        if (this.isUpdatingStatus) {
            return;
        } else {
            this.isUpdatingStatus = true;
        }

        try {
            doUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.isUpdatingStatus = false;
        }
    }

    private void doUpdate() {
        List<QrtzSchedulerState> qrtzSchedulerStates = qrtzSchedulerStateMapper.selectList(null);
        List<String> activeQrtzInstances = qrtzSchedulerStates.stream().filter(qrtzSchedulerState -> qrtzSchedulerState.getLastCheckinTime() + qrtzSchedulerState.getCheckinInterval() + 1000 > dataSourceExtMapper.selectTimestamp().getCurrentTimestamp() * 1000).map(QrtzSchedulerState::getInstanceName).collect(Collectors.toList());

        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_status", TaskStatus.UnderExecution.name());
        List<CoreDatasource> datasources = datasourceMapper.selectList(queryWrapper);

        List<CoreDatasource> syncCoreDatasources = new ArrayList<>();
        List<CoreDatasource> jobStoppedCoreDatasources = new ArrayList<>();
        datasources.forEach(coreDatasource -> {
            if (StringUtils.isNotEmpty(coreDatasource.getQrtzInstance()) && !activeQrtzInstances.contains(coreDatasource.getQrtzInstance().substring(0, coreDatasource.getQrtzInstance().length() - 13))) {
                jobStoppedCoreDatasources.add(coreDatasource);
            } else {
                syncCoreDatasources.add(coreDatasource);
            }
        });

        if (CollectionUtils.isEmpty(jobStoppedCoreDatasources)) {
            return;
        }

        queryWrapper.clear();
        queryWrapper.in("id", jobStoppedCoreDatasources.stream().map(CoreDatasource::getId).collect(Collectors.toList()));
        CoreDatasource record = new CoreDatasource();
        record.setTaskStatus(TaskStatus.WaitingForExecution.name());
        datasourceMapper.update(record, queryWrapper);
        //Task
        datasourceTaskServer.updateByDsIds(jobStoppedCoreDatasources.stream().map(CoreDatasource::getId).collect(Collectors.toList()));
    }

    public boolean showFinishPage() throws DEException {
        return coreDsFinishPageMapper.selectById(AuthUtils.getUser().getUserId()) == null;
    }


    public void setShowFinishPage() throws DEException {
        CoreDsFinishPage coreDsFinishPage = new CoreDsFinishPage();
        coreDsFinishPage.setId(AuthUtils.getUser().getUserId());
        coreDsFinishPageMapper.insert(coreDsFinishPage);
    }

    private DatasourceDTO transDTO(CoreDatasource record) {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, record);
        return datasourceDTO;
    }


    private void filterDs(List<BusiNodeVO> busiNodeVOS, List<Long> ids, String type, Long id) {
        for (BusiNodeVO busiNodeVO : busiNodeVOS) {
            if (busiNodeVO.getType() != null && busiNodeVO.getType().equalsIgnoreCase(type)) {
                if (id != null) {
                    if (!busiNodeVO.getId().equals(id)) {
                        ids.add(busiNodeVO.getId());
                    }
                } else {
                    ids.add(busiNodeVO.getId());
                }
            }
            if (CollectionUtils.isNotEmpty(busiNodeVO.getChildren())) {
                filterDs(busiNodeVO.getChildren(), ids, type, id);
            }
        }
    }

    private static void checkParams(String configurationStr) {
        DatasourceConfiguration configuration = JsonUtil.parseObject(configurationStr, DatasourceConfiguration.class);
        if (configuration == null) {
            DEException.throwException(ResultCode.PARAM_IS_INVALID.code(), "数据源配置格式无效");
        }
        if (configuration.getInitialPoolSize() < configuration.getMinPoolSize()) {
            DEException.throwException("初始连接数不能小于最小连接数！");
        }
        if (configuration.getInitialPoolSize() > configuration.getMaxPoolSize()) {
            DEException.throwException("初始连接数不能大于最大连接数！");
        }
        if (configuration.getMaxPoolSize() < configuration.getMinPoolSize()) {
            DEException.throwException("最大连接数不能小于最小连接数！");
        }
        if (configuration.getQueryTimeout() < 0) {
            DEException.throwException("查询超时不能小于0！");
        }
    }

    private static void checkDatasourceConfigurationComplete(DatasourceDTO datasource) {
        String type = datasource.getType();
        if (StringUtils.isBlank(type)) {
            DEException.throwException(ResultCode.PARAM_IS_INVALID.code(), "数据源类型不能为空");
        }
        if (notFullDs.stream().anyMatch(item -> Strings.CI.contains(type, item))) {
            return;
        }
        DatasourceConfiguration configuration = JsonUtil.parseObject(datasource.getConfiguration(), DatasourceConfiguration.class);
        if (configuration == null) {
            DEException.throwException(ResultCode.PARAM_IS_INVALID.code(), "数据源配置格式无效");
        }
        boolean customJdbcUrl = StringUtils.isNotBlank(configuration.getUrlType())
                && !Strings.CI.equals(configuration.getUrlType(), "hostName");
        if (customJdbcUrl) {
            if (StringUtils.isBlank(configuration.getJdbcUrl())) {
                DEException.throwException(ResultCode.PARAM_IS_INVALID.code(), "数据源 JDBC 地址不能为空");
            }
            return;
        }
        if (StringUtils.isBlank(configuration.getHost())) {
            DEException.throwException(ResultCode.PARAM_IS_INVALID.code(), "数据源地址不能为空");
        }
        if (configuration.getPort() == null) {
            DEException.throwException(ResultCode.PARAM_IS_INVALID.code(), "数据源端口不能为空");
        }
        if (StringUtils.isBlank(configuration.getDataBase())) {
            DEException.throwException(ResultCode.PARAM_IS_INVALID.code(), "数据库名称不能为空");
        }
    }

    private static void checkName(List<String> tables) {
        for (int i = 0; i < tables.size() - 1; i++) {
            for (int j = i + 1; j < tables.size(); j++) {
                if (tables.get(i).equalsIgnoreCase(tables.get(j))) {
                    DEException.throwException(Translator.get("i18n_table_name_repeat") + tables.get(i));
                }
            }
        }
    }

    private String excelDataTableName(String name) {
        return StringUtils.substring(name, 6, name.length() - 11);
    }

    private DatasourceDTO getDatasourceDTOById(Long datasourceId, boolean hidePw) throws DEException {
        CoreDatasource datasource = requireDatasourceAccess(datasourceId);
        if (datasource == null) {
            DEException.throwException(Translator.get("i18n_datasource_not_exists"));
        }
        return convertCoreDatasource(datasourceId, hidePw, datasource);
    }

    private DatasourceDTO convertCoreDatasource(Long datasourceId, boolean hidePw, CoreDatasource datasource) {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, datasource);

        if (datasourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.toString())) {
            List<ApiDefinition> apiDefinitionList = JsonUtil.parseList(datasourceDTO.getConfiguration(), listTypeReference);
            List<ApiDefinition> apiDefinitionListWithStatus = new ArrayList<>();
            List<ApiDefinition> params = new ArrayList<>();
            int success = 0;
            for (ApiDefinition apiDefinition : apiDefinitionList) {
                String status = null;
                if (StringUtils.isNotEmpty(datasourceDTO.getStatus())) {
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(datasourceDTO.getStatus());
                        for (JsonNode node : jsonNode) {
                            if (node.get("name").asText().equals(apiDefinition.getName())) {
                                status = node.get("status").asText();
                            }
                        }
                        apiDefinition.setStatus(status);
                    } catch (Exception ignore) {
                    }
                }
                if (StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")) {
                    success++;
                }
                CoreDatasourceTaskLog log = datasourceTaskServer.lastSyncLogForTable(datasourceId, apiDefinition.getDeTableName());
                if (log != null) {
                    apiDefinition.setUpdateTime(log.getStartTime());
                }


                if (StringUtils.isEmpty(apiDefinition.getType()) || apiDefinition.getType().equalsIgnoreCase("table")) {
                    apiDefinitionListWithStatus.add(apiDefinition);
                } else {
                    params.add(apiDefinition);
                }
            }
            if (CollectionUtils.isNotEmpty(params)) {
                datasourceDTO.setParamsStr(RsaUtils.symmetricEncrypt(JsonUtil.toJSONString(params).toString()));
            }
            if (CollectionUtils.isNotEmpty(apiDefinitionListWithStatus)) {
                datasourceDTO.setApiConfigurationStr(RsaUtils.symmetricEncrypt(JsonUtil.toJSONString(apiDefinitionListWithStatus).toString()));
            }
            if (success == apiDefinitionList.size()) {
                datasourceDTO.setStatus("Success");
            } else {
                if (success > 0 && success < apiDefinitionList.size()) {
                    datasourceDTO.setStatus("Warning");
                } else {
                    datasourceDTO.setStatus("Error");
                }
            }
        } else {
            if (hidePw) {
                Provider provider = ProviderFactory.getProvider(datasourceDTO.getType());
                provider.hidePW(datasourceDTO);
            }
        }
        if (datasourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.Excel.toString())) {
            datasourceDTO.setFileName(ExcelUtils.getFileName(datasource));
            datasourceDTO.setSize(ExcelUtils.getSize(datasource));
        }
        if (datasourceDTO.getType().equalsIgnoreCase(DatasourceConfiguration.DatasourceType.ExcelRemote.name()) || datasourceDTO.getType().contains(DatasourceConfiguration.DatasourceType.API.toString())) {
            CoreDatasourceTask coreDatasourceTask = datasourceTaskServer.selectByDSId(datasourceDTO.getId());
            TaskDTO taskDTO = new TaskDTO();
            BeanUtils.copyBean(taskDTO, coreDatasourceTask);
            datasourceDTO.setSyncSetting(taskDTO);
            CoreDatasourceTask task = datasourceTaskServer.selectByDSId(datasourceDTO.getId());
            if (task != null) {
                datasourceDTO.setLastSyncTime(task.getStartTime());
            }
        }
        datasourceDTO.setConfiguration(RsaUtils.symmetricEncrypt(datasourceDTO.getConfiguration()));
        datasourceDTO.setCreator(coreUserManage.getUserName(Long.valueOf(datasourceDTO.getCreateBy())));
        return datasourceDTO;
    }

    private DatasourceDTO validate(CoreDatasource coreDatasource) {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        try {
            checkDatasourceStatus(datasourceDTO);
            if (!Arrays.asList("API", "Excel", "folder").contains(coreDatasource.getType()) && !coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.API.name()) && !coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.ExcelRemote.name())) {
                calciteProvider.updateDsPoolAfterCheckStatus(datasourceDTO);
            }
        } catch (DEException e) {
            datasourceDTO.setStatus("Error");
            throw e;
        } catch (Exception e) {
            datasourceDTO.setStatus("Error");
            DEException.throwException(e);
        } finally {
            coreDatasource.setStatus(datasourceDTO.getStatus());
            dataSourceManage.innerEditStatus(coreDatasource);
        }
        datasourceDTO.setConfiguration("");
        return datasourceDTO;
    }

    @Override
    public DsSimpleVO simple(Long id) {
        if (ObjectUtils.isEmpty(id)) DEException.throwException("id is null");
        CoreDatasource coreDatasource = requireDatasourceAccess(id);
        if (ObjectUtils.isEmpty(coreDatasource)) return null;
        DsSimpleVO vo = new DsSimpleVO();
        vo.setName(coreDatasource.getName());
        vo.setType(coreDatasource.getType());
        vo.setDescription(coreDatasource.getDescription());
        String configuration = coreDatasource.getConfiguration();
        DatasourceConfiguration config = null;
        String host = null;
        if (StringUtils.isBlank(configuration)
                || Strings.CI.equals("[]", configuration)
                || ObjectUtils.isEmpty(config = JsonUtil.parseObject(configuration, DatasourceConfiguration.class))
                || StringUtils.isBlank(host = config.getHost())) {
            return vo;
        }
        vo.setHost(host);
        return vo;
    }

    private CoreDatasource requireDatasourceAccess(Long datasourceId) {
        CoreDatasource datasource = dataSourceManage.getCoreDatasource(datasourceId);
        if (datasource == null) {
            DEException.throwException(Translator.get("i18n_datasource_not_exists"));
        }
        CrestPermissionUtils.requireCreator(datasource.getCreateBy());
        return datasource;
    }

    @Override
    public List<Map<String, String>> multidimensionalTables(Map<String, String> request) throws DEException {
        List<ApiDefinition> paramsList = new ArrayList<>();
        ApiDefinition apiDefinition = JsonUtil.parseObject(decodeBase64RequestValue(request.get("data"), "API 数据源配置"), ApiDefinition.class);
        paramsList.add(apiDefinition);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        DatasourceDTO datasource = new DatasourceDTO();
        datasource.setConfiguration(JsonUtil.toJSONString(paramsList).toString());
        datasourceRequest.setDatasource(datasource);
        List<Map<String, String>> result = new ArrayList<>();
        if (request.keySet().contains("type") && request.get("type").equals("tables")) {
            result = (List<Map<String, String>>) invokeMethod(request.get("dsType"), "listTables", DatasourceRequest.class, datasourceRequest);
        }
        if (request.keySet().contains("type") && request.get("type").equals("views")) {
            result = (List<Map<String, String>>) invokeMethod(request.get("dsType"), "listViews", DatasourceRequest.class, datasourceRequest);
        }
        return result;
    }

    private Method getMethod(String dsType, String methodName, Class<?> classes) {
        Method method = null;
        try {
            String ClassName = "io.crest.datasource.provider.ApiUtils";
            if (!dsType.equals(DatasourceConfiguration.DatasourceType.API.name())) {
                Provider provider = ProviderFactory.getProvider(dsType);
                method = provider.getClass().getMethod(methodName, classes);
            } else {
                Class<?> clazz = Class.forName(ClassName);
                method = clazz.getMethod(methodName, classes);
            }

        } catch (Exception e) {
            DEException.throwException("Cant find method: " + e.getMessage());
        }
        return method;
    }

    public Object invokeMethod(String dsType, String methodName, Class<?> classes, Object object) {
        Object resObj = null;
        try {
            Method method = getMethod(dsType, methodName, classes);
            resObj = method.invoke(null, object);
        } catch (Exception e) {
            DEException.throwException(msg(e));
        }
        return resObj;
    }

    private String msg(Throwable e) {
        Throwable exception = e;
        while (true) {
            if (exception.getCause() == null) {
                return exception.getMessage();
            }
            if (exception instanceof DEException && (!(exception.getCause() instanceof DEException) && !(exception.getCause() instanceof InvocationTargetException))) {
                return exception.getMessage();
            }
            exception = exception.getCause();

        }
    }
}
