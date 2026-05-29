package io.crest.datasource.server;

import io.crest.api.ds.EngineApi;
import io.crest.datasource.dao.auto.entity.CoreDeEngine;
import io.crest.datasource.dao.auto.mapper.CoreDeEngineMapper;
import io.crest.datasource.manage.EngineManage;
import io.crest.datasource.provider.CalciteProvider;
import io.crest.exception.DEException;
import io.crest.extensions.datasource.dto.DatasourceDTO;
import io.crest.result.ResultCode;
import io.crest.utils.AuthUtils;
import io.crest.utils.BeanUtils;
import io.crest.utils.IDUtils;
import io.crest.utils.RsaUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/engine")
@Transactional(rollbackFor = Exception.class)
public class EngineServer implements EngineApi {
    @Resource
    private CoreDeEngineMapper deEngineMapper;
    @Resource
    private EngineManage engineManage;
    @Resource
    private CalciteProvider calciteProvider;

    @Override
    public DatasourceDTO getEngine() {
        if (!AuthUtils.getUser().getUserId().equals(1L)) {
            DEException.throwException("非管理员，无权访问！");
        }
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(null);
        if (CollectionUtils.isEmpty(deEngines)) {
            return datasourceDTO;
        }
        BeanUtils.copyBean(datasourceDTO, deEngines.get(0));
        datasourceDTO.setConfiguration(RsaUtils.symmetricEncrypt(datasourceDTO.getConfiguration()));
        return datasourceDTO;
    }

    @Override
    public void save(DatasourceDTO datasourceDTO) {
        if (!AuthUtils.getUser().getUserId().equals(1L)) {
            DEException.throwException("非管理员，无权访问！");
        }
        if (StringUtils.isNotEmpty(datasourceDTO.getConfiguration())) {
            datasourceDTO.setConfiguration(decodeBase64RequestValue(datasourceDTO.getConfiguration(), "引擎配置"));
        }
        CoreDeEngine coreDeEngine = new CoreDeEngine();
        BeanUtils.copyBean(coreDeEngine, datasourceDTO);
        if (coreDeEngine.getId() == null) {
            coreDeEngine.setId(IDUtils.snowID());
            datasourceDTO.setId(coreDeEngine.getId());
            deEngineMapper.insert(coreDeEngine);
        } else {
            deEngineMapper.updateById(coreDeEngine);
        }
        calciteProvider.update(datasourceDTO);
    }

    @Override
    public void validate(DatasourceDTO datasourceDTO) throws Exception {
        if (!AuthUtils.getUser().getUserId().equals(1L)) {
            DEException.throwException("非管理员，无权访问！");
        }
        CoreDeEngine coreDeEngine = new CoreDeEngine();
        BeanUtils.copyBean(coreDeEngine, datasourceDTO);
        coreDeEngine.setConfiguration(decodeBase64RequestValue(coreDeEngine.getConfiguration(), "引擎配置"));
        engineManage.validate(coreDeEngine);
    }

    @Override
    public void validateById(Long id) throws Exception {
        if (!AuthUtils.getUser().getUserId().equals(1L)) {
            DEException.throwException("非管理员，无权访问！");
        }
        engineManage.validate(deEngineMapper.selectById(id));
    }

    @Override
    public boolean supportSetKey() throws Exception {
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(null);
        if (CollectionUtils.isEmpty(deEngines)) {
            return false;
        } else {
            return !deEngines.getFirst().getType().equalsIgnoreCase("h2");
        }

    }

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
}
