package io.crest.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.crest.api.dataset.union.DatasetGroupInfoDTO;
import io.crest.api.dataset.union.UnionDTO;
import io.crest.api.dataset.vo.DataSetBarVO;
import io.crest.api.permissions.relation.api.RelationApi;
import io.crest.commons.constants.OptConstants;
import io.crest.dataset.dao.auto.entity.CoreDatasetGroup;
import io.crest.dataset.dao.auto.entity.CoreDatasetTable;
import io.crest.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.crest.dataset.dao.auto.mapper.CoreDatasetTableMapper;
import io.crest.dataset.dao.ext.mapper.CoreDataSetExtMapper;
import io.crest.dataset.dao.ext.po.DataSetNodePO;
import io.crest.dataset.dto.DataSetNodeBO;
import io.crest.dataset.sync.DatasetSyncTaskManage;
import io.crest.dataset.utils.DatasetUtils;
import io.crest.dataset.utils.FieldUtils;
import io.crest.dataset.utils.TableUtils;
import io.crest.datasource.dao.auto.entity.CoreDatasource;
import io.crest.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.crest.engine.constant.ExtFieldConstant;
import io.crest.engine.func.FunctionConstant;
import io.crest.engine.utils.Utils;
import io.crest.exception.DEException;
import io.crest.extensions.datasource.api.PluginManageApi;
import io.crest.extensions.datasource.dto.DatasetTableDTO;
import io.crest.extensions.datasource.dto.DatasetTableFieldDTO;
import io.crest.extensions.datasource.dto.DatasourceDTO;
import io.crest.extensions.datasource.model.SQLObj;
import io.crest.extensions.view.dto.SqlVariableDetails;
import io.crest.i18n.Translator;
import io.crest.model.BusiNodeRequest;
import io.crest.model.BusiNodeVO;
import io.crest.operation.manage.CoreOptRecentManage;
import io.crest.system.manage.CoreUserManage;
import io.crest.utils.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
@Transactional(rollbackFor = Exception.class)
@SuppressWarnings("unchecked")
public class DatasetGroupManage {
    @Resource
    private CoreDatasetGroupMapper coreDatasetGroupMapper;
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private DatasetDataManage datasetDataManage;
    @Resource
    private DatasetTableManage datasetTableManage;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private PermissionManage permissionManage;
    @Resource
    private CoreDataSetExtMapper coreDataSetExtMapper;
    @Resource
    private CoreDatasetTableMapper coreDatasetTableMapper;
    @Resource
    private CoreDatasourceMapper coreDatasourceMapper;
    @Resource
    private DatasetSyncTaskManage datasetSyncTaskManage;


    @Resource
    private CoreUserManage coreUserManage;

    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    @Autowired(required = false)
    private RelationApi relationManage;
    @Autowired(required = false)
    private PluginManageApi pluginManage;

    private static final String leafType = "dataset";

    private Lock lock = new ReentrantLock();


    @Transactional
    public DatasetGroupInfoDTO save(DatasetGroupInfoDTO datasetGroupInfoDTO, boolean rename, boolean encode) throws Exception {
        try {
            boolean isCreate;
            // 用于重命名获取pid
            if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getPid()) && ObjectUtils.isNotEmpty(datasetGroupInfoDTO.getId())) {
                requireDatasetAccess(datasetGroupInfoDTO.getId());
                CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(datasetGroupInfoDTO.getId());
                datasetGroupInfoDTO.setPid(coreDatasetGroup.getPid());
            }
            if (ObjectUtils.isNotEmpty(datasetGroupInfoDTO.getId())) {
                requireDatasetAccess(datasetGroupInfoDTO.getId());
            }
            if (ObjectUtils.isNotEmpty(datasetGroupInfoDTO.getPid()) && !Objects.equals(datasetGroupInfoDTO.getPid(), 0L)) {
                requireDatasetAccess(datasetGroupInfoDTO.getPid());
            }
            datasetGroupInfoDTO.setUpdateBy(AuthUtils.getUser().getUserId() + "");
            datasetGroupInfoDTO.setLastUpdateTime(System.currentTimeMillis());
            if (Strings.CI.equals(datasetGroupInfoDTO.getNodeType(), leafType)) {
                if (!rename && ObjectUtils.isEmpty(datasetGroupInfoDTO.getAllFields())) {
                    DEException.throwException(Translator.get("i18n_no_fields"));
                }
                // get union sql
                Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, null);
                if (ObjectUtils.isNotEmpty(sqlMap)) {
                    String sql = (String) sqlMap.get("sql");
                    datasetGroupInfoDTO.setUnionSql(sql);
                    datasetGroupInfoDTO.setInfo(Objects.requireNonNull(JsonUtil.toJSONString(datasetGroupInfoDTO.getUnion())).toString());
                }
            }
            // save dataset/group
            long time = System.currentTimeMillis();
            if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getId())) {
                isCreate = true;
                datasetGroupInfoDTO.setId(IDUtils.snowID());
                datasetGroupInfoDTO.setCreateBy(AuthUtils.getUser().getUserId() + "");
                datasetGroupInfoDTO.setUpdateBy(AuthUtils.getUser().getUserId() + "");
                datasetGroupInfoDTO.setCreateTime(time);
                datasetGroupInfoDTO.setLastUpdateTime(time);
                datasetGroupInfoDTO.setPid(datasetGroupInfoDTO.getPid() == null ? 0L : datasetGroupInfoDTO.getPid());
                Objects.requireNonNull(CommonBeanFactory.getBean(this.getClass())).innerSave(datasetGroupInfoDTO);
            } else {
                isCreate = false;
                if (Objects.equals(datasetGroupInfoDTO.getId(), datasetGroupInfoDTO.getPid())) {
                    DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
                }
                Objects.requireNonNull(CommonBeanFactory.getBean(this.getClass())).innerEdit(datasetGroupInfoDTO);
            }
            // node_type=dataset需要创建dataset_table和field
            if (Strings.CI.equals(datasetGroupInfoDTO.getNodeType(), "dataset")) {
                if (encode) {
                    DatasetUtils.dsDecode(datasetGroupInfoDTO);
                }
                List<Long> tableIds = new ArrayList<>();
                List<Long> fieldIds = new ArrayList<>();
                // 解析tree，保存
                saveTable(datasetGroupInfoDTO, datasetGroupInfoDTO.getUnion(), tableIds, isCreate);
                saveField(datasetGroupInfoDTO, fieldIds);
                // 删除不要的table和field
                datasetTableManage.deleteByDatasetGroupUpdate(datasetGroupInfoDTO.getId(), tableIds);
                datasetTableFieldManage.deleteByDatasetGroupUpdate(datasetGroupInfoDTO.getId(), fieldIds);
                if (encode) {
                    DatasetUtils.dsEncode(datasetGroupInfoDTO);
                }
            }
            if (StringUtils.isNotEmpty(datasetGroupInfoDTO.getUnionSql())) {
                datasetGroupInfoDTO.setUnionSql(DatasetUtils.getEncode(datasetGroupInfoDTO.getUnionSql()));
            }
            return datasetGroupInfoDTO;
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return null;
    }
    public void innerEdit(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        requireDatasetAccess(datasetGroupInfoDTO.getId());
        checkName(datasetGroupInfoDTO);
        CoreDatasetGroup coreDatasetGroup = BeanUtils.copyBean(new CoreDatasetGroup(), datasetGroupInfoDTO);
        coreDatasetGroup.setLastUpdateTime(System.currentTimeMillis());
        coreDatasetGroupMapper.updateById(coreDatasetGroup);
        coreOptRecentManage.saveOpt(datasetGroupInfoDTO.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASET, OptConstants.OPT_TYPE.UPDATE);
    }
    public void innerSave(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        checkName(datasetGroupInfoDTO);
        CoreDatasetGroup coreDatasetGroup = BeanUtils.copyBean(new CoreDatasetGroup(), datasetGroupInfoDTO);
        coreDatasetGroupMapper.insert(coreDatasetGroup);
        coreOptRecentManage.saveOpt(coreDatasetGroup.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASET, OptConstants.OPT_TYPE.NEW);
    }
    public DatasetGroupInfoDTO move(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        requireDatasetAccess(datasetGroupInfoDTO.getId());
        if (ObjectUtils.isNotEmpty(datasetGroupInfoDTO.getPid()) && !Objects.equals(datasetGroupInfoDTO.getPid(), 0L)) {
            requireDatasetAccess(datasetGroupInfoDTO.getPid());
        }
        checkName(datasetGroupInfoDTO);
        if (datasetGroupInfoDTO.getPid() != 0) {
            checkMove(datasetGroupInfoDTO);
        }
        // save dataset/group
        long time = System.currentTimeMillis();
        CoreDatasetGroup coreDatasetGroup = new CoreDatasetGroup();
        BeanUtils.copyBean(coreDatasetGroup, datasetGroupInfoDTO);
        datasetGroupInfoDTO.setUpdateBy(AuthUtils.getUser().getUserId() + "");
        coreDatasetGroup.setLastUpdateTime(time);
        coreDatasetGroupMapper.updateById(coreDatasetGroup);
        coreOptRecentManage.saveOpt(coreDatasetGroup.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASET, OptConstants.OPT_TYPE.UPDATE);
        return datasetGroupInfoDTO;
    }

    public boolean perDelete(Long id) {
        if (relationManage != null) {
            Long count = relationManage.getDatasetResource(id);
            if (count > 0) {
                return true;
            }
        }
        return false;
    }
    public void delete(Long id) {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
        if (ObjectUtils.isEmpty(coreDatasetGroup)) {
            DEException.throwException("resource not exist");
        }
        requireDatasetTreeAccess(id);
        Objects.requireNonNull(CommonBeanFactory.getBean(this.getClass())).recursionDel(id);
        coreOptRecentManage.saveOpt(coreDatasetGroup.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASET, OptConstants.OPT_TYPE.DELETE);
    }

    public void recursionDel(Long id) {
        datasetSyncTaskManage.deleteByDatasetGroupId(id);
        coreDatasetGroupMapper.deleteById(id);
        datasetTableManage.deleteByDatasetGroupDelete(id);
        datasetTableFieldManage.deleteByDatasetGroupDelete(id);

        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", id);
        List<CoreDatasetGroup> coreDatasetGroups = coreDatasetGroupMapper.selectList(wrapper);
        if (ObjectUtils.isNotEmpty(coreDatasetGroups)) {
            for (CoreDatasetGroup record : coreDatasetGroups) {
                recursionDel(record.getId());
            }
        }
    }
    public List<BusiNodeVO> tree(BusiNodeRequest request) {

        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        if (ObjectUtils.isNotEmpty(request.getLeaf())) {
            queryWrapper.eq("node_type", request.getLeaf() ? "dataset" : "folder");
        }
        String info = CommunityUtils.getInfo();
        if (StringUtils.isNotBlank(info)) {
            queryWrapper.notExists(String.format(info, "core_dataset_group.id"));
        }
        queryWrapper.orderByDesc("create_time");
        List<DataSetNodePO> pos = coreDataSetExtMapper.query(queryWrapper);
        List<DataSetNodeBO> nodes = new ArrayList<>();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) nodes.add(rootNode());
        List<DataSetNodeBO> bos = pos.stream().map(this::convert).toList();
        if (CollectionUtils.isNotEmpty(bos)) {
            nodes.addAll(bos);
        }
        return TreeUtils.mergeTree(nodes, BusiNodeVO.class, false);
    }

    public DataSetBarVO queryBarInfo(Long id) {
        requireDatasetAccess(id);
        DataSetBarVO dataSetBarVO = coreDataSetExtMapper.queryBarInfo(id);
        // get creator
        String userName = coreUserManage.getUserName(Long.valueOf(dataSetBarVO.getCreateBy()));
        if (StringUtils.isNotBlank(userName)) {
            dataSetBarVO.setCreator(userName);
        }
        String updateUserName = coreUserManage.getUserName(Long.valueOf(dataSetBarVO.getUpdateBy()));
        if (StringUtils.isNotBlank(updateUserName)) {
            dataSetBarVO.setUpdater(updateUserName);
        }
        dataSetBarVO.setDatasourceDTOList(getDatasource(id));
        return dataSetBarVO;
    }

    private List<DatasourceDTO> getDatasource(Long datasetId) {
        requireDatasetAccess(datasetId);
        QueryWrapper<CoreDatasetTable> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", datasetId);
        List<CoreDatasetTable> coreDatasetTables = coreDatasetTableMapper.selectList(wrapper);
        Set<Long> ids = new LinkedHashSet();
        coreDatasetTables.forEach(ele -> ids.add(ele.getDatasourceId()));
        if (CollectionUtils.isEmpty(ids)) {
            DEException.throwException(Translator.get("i18n_dataset_create_error"));
        }

        QueryWrapper<CoreDatasource> datasourceQueryWrapper = new QueryWrapper<>();
        datasourceQueryWrapper.in("id", ids);
        List<DatasourceDTO> datasourceDTOList = coreDatasourceMapper.selectList(datasourceQueryWrapper).stream().map(ele -> {
            DatasourceDTO dto = new DatasourceDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setConfiguration(null);
            return dto;
        }).collect(Collectors.toList());
        if (ids.size() != datasourceDTOList.size()) {
            DEException.throwException(Translator.get("i18n_dataset_ds_delete"));
        }
        return datasourceDTOList;
    }

    private DataSetNodeBO rootNode() {
        return new DataSetNodeBO(0L, "root", false, 7, -1L, 0);
    }

    private DataSetNodeBO convert(DataSetNodePO po) {
        return new DataSetNodeBO(po.getId(), po.getName(), Strings.CS.equals(po.getNodeType(), leafType), 9, po.getPid(), 0);
    }

    public void checkName(DatasetGroupInfoDTO dto) {
    }

    public void saveTable(DatasetGroupInfoDTO datasetGroupInfoDTO, List<UnionDTO> union, List<Long> tableIds, boolean isCreate) {
        // table和field均由前端生成id（如果没有id）
        Long datasetGroupId = datasetGroupInfoDTO.getId();
        if (ObjectUtils.isNotEmpty(union)) {
            for (UnionDTO unionDTO : union) {
                DatasetTableDTO currentDs = unionDTO.getCurrentDs();
                if (ObjectUtils.isNotEmpty(currentDs.getDatasourceId())) {
                    requireDatasourceAccess(currentDs.getDatasourceId());
                }
                CoreDatasetTable coreDatasetTable = datasetTableManage.selectById(currentDs.getId());
                if (coreDatasetTable != null && isCreate) {
                    DEException.throwException(Translator.get("i18n_table_duplicate"));
                }
                currentDs.setDatasetGroupId(datasetGroupId);
                datasetTableManage.save(currentDs);
                tableIds.add(currentDs.getId());

                saveTable(datasetGroupInfoDTO, unionDTO.getChildrenDs(), tableIds, isCreate);
            }
        }
    }

    public void saveField(DatasetGroupInfoDTO datasetGroupInfoDTO, List<Long> fieldIds) throws Exception {
        if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getUnion())) {
            return;
        }
        datasetDataManage.previewDataWithLimit(datasetGroupInfoDTO, 0, 1, false, false);
        // table和field均由前端生成id（如果没有id）
        Long datasetGroupId = datasetGroupInfoDTO.getId();
        List<DatasetTableFieldDTO> allFields = datasetGroupInfoDTO.getAllFields();
        if (ObjectUtils.isNotEmpty(allFields)) {
            // 获取内层union sql和字段
            Map<String, Object> map = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, null);
            List<DatasetTableFieldDTO> unionFields = (List<DatasetTableFieldDTO>) map.get("field");

            for (DatasetTableFieldDTO datasetTableFieldDTO : allFields) {
                DatasetTableFieldDTO dto = datasetTableFieldManage.selectById(datasetTableFieldDTO.getId());
                if (ObjectUtils.isEmpty(dto)) {
                    if (Objects.equals(datasetTableFieldDTO.getExtField(), ExtFieldConstant.EXT_NORMAL)) {
                        for (DatasetTableFieldDTO fieldDTO : unionFields) {
                            if (Objects.equals(datasetTableFieldDTO.getDatasetTableId(), fieldDTO.getDatasetTableId())
                                    && Objects.equals(datasetTableFieldDTO.getOriginName(), fieldDTO.getOriginName())) {
                                datasetTableFieldDTO.setDataeaseName(fieldDTO.getDataeaseName());
                                datasetTableFieldDTO.setFieldShortName(fieldDTO.getFieldShortName());
                            }
                        }
                    }
                    if (Objects.equals(datasetTableFieldDTO.getExtField(), ExtFieldConstant.EXT_CALC)) {
                        String dataeaseName = TableUtils.fieldNameShort(datasetTableFieldDTO.getId() + "_" + datasetTableFieldDTO.getOriginName());
                        datasetTableFieldDTO.setDataeaseName(dataeaseName);
                        datasetTableFieldDTO.setFieldShortName(dataeaseName);
                        datasetTableFieldDTO.setDeExtractType(datasetTableFieldDTO.getDeType());
                    }
                    if (Objects.equals(datasetTableFieldDTO.getExtField(), ExtFieldConstant.EXT_GROUP)) {
                        String dataeaseName = TableUtils.fieldNameShort(datasetTableFieldDTO.getId() + "_" + datasetTableFieldDTO.getOriginName());
                        datasetTableFieldDTO.setDataeaseName(dataeaseName);
                        datasetTableFieldDTO.setFieldShortName(dataeaseName);
                        datasetTableFieldDTO.setDeExtractType(0);
                        datasetTableFieldDTO.setDeType(0);
                        datasetTableFieldDTO.setGroupType("d");
                    }
                    datasetTableFieldDTO.setDatasetGroupId(datasetGroupId);
                } else {
                    datasetTableFieldDTO.setDataeaseName(dto.getDataeaseName());
                    datasetTableFieldDTO.setFieldShortName(dto.getFieldShortName());
                }
                datasetTableFieldDTO = datasetTableFieldManage.save(datasetTableFieldDTO);
                fieldIds.add(datasetTableFieldDTO.getId());
            }
        }
    }

    public DatasetGroupInfoDTO getForCount(Long id) throws Exception {
        requireDatasetAccess(id);
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
        if (coreDatasetGroup == null) {
            return null;
        }
        DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(dto, coreDatasetGroup);
        if (Strings.CI.equals(dto.getNodeType(), "dataset")) {
            dto.setUnion(JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
            }));
            // 获取field
            List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(id);
            List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                BeanUtils.copyBean(datasetTableFieldDTO, ele);
                datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                return datasetTableFieldDTO;
            }).collect(Collectors.toList());

            dto.setAllFields(allFields);
        }
        return dto;
    }

    public DatasetGroupInfoDTO getDetail(Long id) throws Exception {
        requireDatasetAccess(id);
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
        if (coreDatasetGroup == null) {
            return null;
        }
        DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(dto, coreDatasetGroup);
        // get creator
        String userName = coreUserManage.getUserName(Long.valueOf(dto.getCreateBy()));
        if (StringUtils.isNotBlank(userName)) {
            dto.setCreator(userName);
        }
        String updateUserName = coreUserManage.getUserName(Long.valueOf(dto.getUpdateBy()));
        if (StringUtils.isNotBlank(updateUserName)) {
            dto.setUpdater(updateUserName);
        }
        dto.setUnionSql(null);
        if (Strings.CI.equals(dto.getNodeType(), "dataset")) {
            List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
            });
            dto.setUnion(unionDTOList);

            // 获取field
            List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(id);
            List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                BeanUtils.copyBean(datasetTableFieldDTO, ele);
                datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                return datasetTableFieldDTO;
            }).collect(Collectors.toList());

            DatasetUtils.listEncode(allFields);

            dto.setAllFields(allFields);
        }
        return dto;
    }

    public DatasetGroupInfoDTO getDatasetGroupInfoDTO(Long id, String type) throws Exception {
        requireDatasetAccess(id);
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
        if (coreDatasetGroup == null) {
            return null;
        }
        DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(dto, coreDatasetGroup);
        // get creator
        String userName = coreUserManage.getUserName(Long.valueOf(dto.getCreateBy()));
        if (StringUtils.isNotBlank(userName)) {
            dto.setCreator(userName);
        }
        String updateUserName = coreUserManage.getUserName(Long.valueOf(dto.getUpdateBy()));
        if (StringUtils.isNotBlank(updateUserName)) {
            dto.setUpdater(updateUserName);
        }
        dto.setUnionSql(null);
        if (Strings.CI.equals(dto.getNodeType(), "dataset")) {
            List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
            });
            dto.setUnion(unionDTOList);

            // 获取field
            List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(id);
            List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                BeanUtils.copyBean(datasetTableFieldDTO, ele);
                datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                return datasetTableFieldDTO;
            }).collect(Collectors.toList());

            dto.setAllFields(allFields);

            if ("preview".equalsIgnoreCase(type)) {
                // 请求数据
                Map<String, Object> map = datasetDataManage.previewDataWithLimit(dto, 0, 100, true, false);
                // 获取data,sql
                Map<String, List> data = (Map<String, List>) map.get("data");
                String sql = (String) map.get("sql");
                Long total = (Long) map.get("total");
                dto.setData(data);
                dto.setSql(Base64.getEncoder().encodeToString(sql.getBytes()));
                dto.setTotal(total);
            }
        }
        return dto;
    }

    public List<DatasetTableDTO> getDetail(List<Long> ids) {
        if (ObjectUtils.isEmpty(ids)) {
            DEException.throwException(Translator.get("i18n_table_id_can_not_empty"));
        }
        List<DatasetTableDTO> list = new ArrayList<>();
        for (Long id : ids) {
            requireDatasetAccess(id);
            CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(id);
            if (coreDatasetGroup == null) {
                list.add(null);
            } else {
                DatasetTableDTO dto = new DatasetTableDTO();
                BeanUtils.copyBean(dto, coreDatasetGroup);
                Map<String, List<DatasetTableFieldDTO>> listByDQ = datasetTableFieldManage.listByDQ(id);
                dto.setFields(listByDQ);
                list.add(dto);
            }
        }
        return list;
    }

    public List<SqlVariableDetails> getSqlParams(List<Long> ids) {
        List<SqlVariableDetails> list = new ArrayList<>();
        if (ObjectUtils.isEmpty(ids)) {
            return list;
        }
        TypeReference<List<SqlVariableDetails>> listTypeReference = new TypeReference<List<SqlVariableDetails>>() {
        };
        for (Long id : ids) {
            requireDatasetAccess(id);
            List<CoreDatasetTable> datasetTables = datasetTableManage.selectByDatasetGroupId(id);
            for (CoreDatasetTable datasetTable : datasetTables) {
                if (StringUtils.isNotEmpty(datasetTable.getSqlVariableDetails())) {
                    List<SqlVariableDetails> defaultsSqlVariableDetails = JsonUtil.parseList(datasetTable.getSqlVariableDetails(), listTypeReference);
                    if (CollectionUtils.isNotEmpty(defaultsSqlVariableDetails)) {
                        List<String> fullName = new ArrayList<>();
                        geFullName(id, fullName);
                        Collections.reverse(fullName);
                        List<String> finalFullName = fullName;
                        defaultsSqlVariableDetails.forEach(sqlVariableDetails -> {
                            sqlVariableDetails.setDatasetGroupId(id);
                            sqlVariableDetails.setDatasetTableId(datasetTable.getId());
                            sqlVariableDetails.setDatasetFullName(String.join("/", finalFullName));
                        });
                    }

                    list.addAll(defaultsSqlVariableDetails);
                }
            }
        }
        list.forEach(sqlVariableDetail -> {
            sqlVariableDetail.setId(sqlVariableDetail.getDatasetTableId() + "|DE|" + sqlVariableDetail.getVariableName());
            sqlVariableDetail.setDeType(FieldUtils.transType2DeType(sqlVariableDetail.getType().get(0).contains("DATETIME") ? "DATETIME" : sqlVariableDetail.getType().get(0)));
        });
        return list;
    }

    public void checkMove(DatasetGroupInfoDTO datasetGroupInfoDTO) {
        if (Objects.equals(datasetGroupInfoDTO.getId(), datasetGroupInfoDTO.getPid())) {
            DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
        }
        List<Long> ids = new ArrayList<>();
        getParents(datasetGroupInfoDTO.getPid(), ids);
        if (ids.contains(datasetGroupInfoDTO.getId())) {
            DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
        }
    }

    private void getParents(Long pid, List<Long> ids) {
        CoreDatasetGroup parent = coreDatasetGroupMapper.selectById(pid);// 查找父级folder
        ids.add(parent.getId());
        if (parent.getPid() != null && parent.getPid() != 0) {
            getParents(parent.getPid(), ids);
        }
    }

    public void geFullName(Long pid, List<String> fullName) {
        CoreDatasetGroup parent = coreDatasetGroupMapper.selectById(pid);// 查找父级folder
        if (parent == null) {
            return;
        }
        fullName.add(parent.getName());
        if (parent.getId().equals(parent.getPid())) {
            return;
        }
        if (parent.getPid() != null && parent.getPid() != 0) {
            geFullName(parent.getPid(), fullName);
        }
    }

    public List<DatasetTableDTO> getDetailWithPerm(List<Long> ids) {
        var result = new ArrayList<DatasetTableDTO>();
        if (CollectionUtils.isNotEmpty(ids)) {
            var dsList = coreDatasetGroupMapper.selectBatchIds(ids);
            if (CollectionUtils.isNotEmpty(dsList)) {
                SQLObj tableObj = new SQLObj();
                tableObj.setTableAlias("");
                dsList.forEach(ds -> {
                    CrestPermissionUtils.requireCreator(ds.getCreateBy());
                    DatasetTableDTO dto = new DatasetTableDTO();
                    BeanUtils.copyBean(dto, ds);
                    var fields = datasetTableFieldManage.listFieldsWithPermissions(ds.getId());
                    var p_fields = fields.stream().filter(ele -> {
                        boolean flag = true;
                        if (Objects.equals(ele.getExtField(), ExtFieldConstant.EXT_CALC)) {
                            String originField = Utils.calcFieldRegex(ele, tableObj, fields, true, null, Utils.mergeParam(Utils.getParams(fields), null), pluginManage);
                            for (String func : FunctionConstant.AGG_FUNC) {
                                if (Utils.matchFunction(func, originField)) {
                                    flag = false;
                                    break;
                                }
                            }
                        }
                        return flag;
                    }).toList();
                    List<DatasetTableFieldDTO> dimensionList = p_fields.stream().filter(ele -> Strings.CI.equals(ele.getGroupType(), "d")).toList();
                    List<DatasetTableFieldDTO> quotaList = p_fields.stream().filter(ele -> Strings.CI.equals(ele.getGroupType(), "q")).toList();
                    Map<String, List<DatasetTableFieldDTO>> map = new LinkedHashMap<>();
                    DatasetUtils.listEncode(dimensionList);
                    DatasetUtils.listEncode(quotaList);
                    map.put("dimensionList", dimensionList);
                    map.put("quotaList", quotaList);
                    dto.setFields(map);
                    result.add(dto);
                });
            }
        }
        return result;
    }

    public List<DatasetGroupInfoDTO> getAllList() {
        List<CoreDatasetGroup> coreDatasetGroupList = coreDatasetGroupMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isEmpty(coreDatasetGroupList)) {
            return new ArrayList<>();
        }
        List<DatasetGroupInfoDTO> list = new ArrayList<>();
        for (CoreDatasetGroup coreDatasetGroup : coreDatasetGroupList) {
            if (!CrestPermissionUtils.canAccessCreator(coreDatasetGroup.getCreateBy())) {
                continue;
            }
            DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
            BeanUtils.copyBean(dto, coreDatasetGroup);
            dto.setUnionSql(null);
            if (Strings.CI.equals(dto.getNodeType(), "dataset")) {
                List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
                });
                dto.setUnion(unionDTOList);

                // 获取field
                List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(coreDatasetGroup.getId());
                List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                    DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                    BeanUtils.copyBean(datasetTableFieldDTO, ele);
                    datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                    return datasetTableFieldDTO;
                }).collect(Collectors.toList());

                DatasetUtils.listEncode(allFields);

                dto.setAllFields(allFields);

                list.add(dto);
            }
        }
        return list;
    }

    private CoreDatasetGroup requireDatasetAccess(Long datasetId) {
        CoreDatasetGroup dataset = coreDatasetGroupMapper.selectById(datasetId);
        if (dataset == null) {
            DEException.throwException("resource not exist");
        }
        CrestPermissionUtils.requireCreator(dataset.getCreateBy());
        return dataset;
    }

    private CoreDatasource requireDatasourceAccess(Long datasourceId) {
        CoreDatasource datasource = coreDatasourceMapper.selectById(datasourceId);
        if (datasource == null) {
            DEException.throwException(Translator.get("i18n_datasource_not_exists"));
        }
        CrestPermissionUtils.requireCreator(datasource.getCreateBy());
        return datasource;
    }

    private void requireDatasetTreeAccess(Long id) {
        requireDatasetAccess(id);
        QueryWrapper<CoreDatasetGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", id);
        List<CoreDatasetGroup> children = coreDatasetGroupMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(children)) {
            for (CoreDatasetGroup child : children) {
                requireDatasetTreeAccess(child.getId());
            }
        }
    }
}
