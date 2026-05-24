package io.crest.datasource.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.crest.datasource.dao.auto.entity.CoreDatasource;
import io.crest.datasource.dao.auto.entity.CoreDeEngine;
import io.crest.datasource.dao.auto.mapper.CoreDeEngineMapper;
import io.crest.datasource.type.H2;
import io.crest.datasource.type.Mysql;
import io.crest.exception.DEException;
import io.crest.extensions.datasource.dto.DatasourceDTO;
import io.crest.extensions.datasource.dto.DatasourceRequest;
import io.crest.extensions.datasource.factory.ProviderFactory;
import io.crest.result.ResultMessage;
import io.crest.utils.BeanUtils;
import io.crest.utils.JsonUtil;
import io.crest.utils.ModelUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Transactional(rollbackFor = Exception.class)
@SuppressWarnings("unchecked")
public class EngineManage {
    @Resource
    private Environment env;
    @Resource
    private CoreDeEngineMapper deEngineMapper;

    @Value("${crest.path.engine:jdbc:h2:/opt/crest/desktop_data;AUTO_SERVER=TRUE;AUTO_RECONNECT=TRUE;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DATABASE_TO_UPPER=FALSE}")
    private String engineUrl;

    public CoreDeEngine info() throws DEException {
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(null);
        if (CollectionUtils.isEmpty(deEngines)) {
            DEException.throwException("未完整设置数据引擎");
        }
        return deEngines.get(0);
    }

    public CoreDatasource getDeEngine() {
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(null);
        if (CollectionUtils.isEmpty(deEngines)) {
            DEException.throwException("未完整设置数据引擎");
        }
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, deEngines.get(0));
        return coreDatasource;
    }


    public CoreDatasource deEngine() {
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(null);
        CoreDatasource coreDatasource = new CoreDatasource();
        if (CollectionUtils.isEmpty(deEngines)) {
            return null;
        }
        BeanUtils.copyBean(coreDatasource, deEngines.get(0));
        return coreDatasource;
    }

    public void validate(CoreDeEngine engine) throws Exception {
        if (StringUtils.isEmpty(engine.getType()) || StringUtils.isEmpty(engine.getConfiguration())) {
            throw new Exception("未完整设置数据引擎");
        }
        try {

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            DatasourceDTO datasource = new DatasourceDTO();
            BeanUtils.copyBean(datasource, engine);
            datasourceRequest.setDatasource(datasource);
            ProviderFactory.getProvider(engine.getType()).checkStatus(datasourceRequest);
        } catch (Exception e) {
            DEException.throwException("校验失败：" + e.getMessage());
        }
    }

    public ResultMessage save(CoreDeEngine engine) throws Exception {
        if (engine.getId() == null) {
            deEngineMapper.insert(engine);
        } else {
            deEngineMapper.updateById(engine);
        }
        return ResultMessage.success(engine);
    }

    public void initSimpleEngine() throws Exception {
        initLocalDataSource();
        QueryWrapper<CoreDeEngine> queryWrapper = new QueryWrapper<>();
        if (ModelUtils.isDesktop()) {
            queryWrapper.eq("type", engineType.h2.name());
        } else {
            queryWrapper.eq("type", engineType.mysql.name());
        }
        List<CoreDeEngine> deEngines = deEngineMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(deEngines)) {
            return;
        }

        CoreDeEngine engine = new CoreDeEngine();
        if (ModelUtils.isDesktop()) {
            engine.setType(engineType.h2.name());
            H2 h2 = new H2();
            h2.setJdbc(engineUrl);
            h2.setDataBase("PUBLIC");
            h2.setUsername(env.getProperty("spring.datasource.username"));
            h2.setPassword(env.getProperty("spring.datasource.password"));
            engine.setConfiguration(JsonUtil.toJSONString(h2).toString());
        } else {
            engine.setType(engineType.mysql.name());
            Mysql mysqlConfiguration = new Mysql();
            Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
            Matcher matcher = WITH_SQL_FRAGMENT.matcher(env.getProperty("spring.datasource.url"));
            if (!matcher.find()) {
                return;
            }
            mysqlConfiguration.setHost(matcher.group(1));
            mysqlConfiguration.setPort(Integer.valueOf(matcher.group(2)));
            String[] databasePrams = matcher.group(3).split("\\?");
            mysqlConfiguration.setDataBase(databasePrams[0]);
            if (databasePrams.length == 2) {
                mysqlConfiguration.setExtraParams(databasePrams[1]);
            }
            mysqlConfiguration.setUsername(env.getProperty("spring.datasource.username"));
            mysqlConfiguration.setPassword(env.getProperty("spring.datasource.password"));
            engine.setConfiguration(JsonUtil.toJSONString(mysqlConfiguration).toString());
        }
        engine.setName("默认引擎");
        engine.setDescription("默认引擎");
        deEngineMapper.insert(engine);
    }


    public enum engineType {
        mysql("Mysql"),
        h2("h2");
        private String alias;

        private engineType(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }
    }

    public void initLocalDataSource() {
        // Crest V1.1 keeps a clean initial workspace. Users add their own datasource after installation.
    }
}
