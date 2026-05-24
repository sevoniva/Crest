package io.crest.system.dao.ext.mapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.crest.system.dao.auto.entity.CoreSysSetting;
import io.crest.system.dao.auto.mapper.CoreSysSettingMapper;
import org.springframework.stereotype.Component;

@Component("extCoreSysSettingMapper")
public class ExtCoreSysSettingMapper extends ServiceImpl<CoreSysSettingMapper, CoreSysSetting> {
}
