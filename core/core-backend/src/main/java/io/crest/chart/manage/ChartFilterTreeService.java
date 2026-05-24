package io.crest.chart.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.crest.dataset.dao.auto.entity.CoreDatasetTableField;
import io.crest.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.crest.extensions.datasource.dto.CalParam;
import io.crest.extensions.datasource.dto.DatasetTableFieldDTO;
import io.crest.extensions.datasource.dto.FieldGroupDTO;
import io.crest.extensions.view.filter.FilterTreeItem;
import io.crest.extensions.view.filter.FilterTreeObj;
import io.crest.utils.BeanUtils;
import io.crest.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Junjun
 */
@Service
public class ChartFilterTreeService {
    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;

    public void searchFieldAndSet(FilterTreeObj tree) {
        if (ObjectUtils.isNotEmpty(tree)) {
            if (ObjectUtils.isNotEmpty(tree.getItems())) {
                for (FilterTreeItem item : tree.getItems()) {
                    if (ObjectUtils.isNotEmpty(item)) {
                        if (Strings.CI.equals(item.getType(), "item") || ObjectUtils.isEmpty(item.getSubTree())) {
                            CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldMapper.selectById(item.getFieldId());
                            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
                            BeanUtils.copyBean(dto, coreDatasetTableField);
                            if (StringUtils.isNotEmpty(coreDatasetTableField.getParams())) {
                                TypeReference<List<CalParam>> tokenType = new TypeReference<>() {
                                };
                                List<CalParam> calParams = JsonUtil.parseList(coreDatasetTableField.getParams(), tokenType);
                                dto.setParams(calParams);
                            }
                            if (StringUtils.isNotEmpty(coreDatasetTableField.getGroupList())) {
                                TypeReference<List<FieldGroupDTO>> groupTokenType = new TypeReference<>() {
                                };
                                List<FieldGroupDTO> fieldGroups = JsonUtil.parseList(coreDatasetTableField.getGroupList(), groupTokenType);
                                dto.setGroupList(fieldGroups);
                            }
                            item.setField(dto);
                        } else if (Strings.CI.equals(item.getType(), "tree") || (ObjectUtils.isNotEmpty(item.getSubTree()) && StringUtils.isNotEmpty(item.getSubTree().getLogic()))) {
                            searchFieldAndSet(item.getSubTree());
                        }
                    }
                }
            }
        }
    }

    public FilterTreeObj charReplace(FilterTreeObj tree) {
        if (ObjectUtils.isNotEmpty(tree)) {
            if (ObjectUtils.isNotEmpty(tree.getItems())) {
                for (FilterTreeItem item : tree.getItems()) {
                    if (ObjectUtils.isNotEmpty(item)) {
                        if (Strings.CI.equals(item.getType(), "tree") || (ObjectUtils.isNotEmpty(item.getSubTree()) && StringUtils.isNotEmpty(item.getSubTree().getLogic()))) {
                            charReplace(item.getSubTree());
                        }
                    }
                }
            }
        }
        return tree;
    }
}
