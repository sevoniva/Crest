package io.crest.system.server;

import io.crest.api.system.SysParameterApi;
import io.crest.api.system.request.SQLBotConfigCreator;
import io.crest.api.system.vo.SQLBotConfigVO;
import io.crest.api.system.vo.SettingItemVO;
import io.crest.api.system.vo.ShareBaseVO;
import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.constant.StaticResourceConstants;
import io.crest.constant.SystemSettingConstants;
import io.crest.exception.DEException;
import io.crest.log.DeLog;
import io.crest.system.dao.auto.entity.CoreSysSetting;
import io.crest.system.manage.SysParameterManage;
import io.crest.utils.CrestPermissionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sysParameter")
public class SysParameterServer implements SysParameterApi {

    @Resource
    private SysParameterManage sysParameterManage;

    @Value("${crest.internal-lite.enabled:false}")
    private boolean internalLiteEnabled;

    @Override
    public String singleVal(String key) {
        return sysParameterManage.singleVal(key);
    }

    @Override
    public List<SettingItemVO> queryBasicSetting() {
        String key = "basic.";
        List<CoreSysSetting> coreSysSettings = sysParameterManage.groupList(key);
        return sysParameterManage.convert(coreSysSettings);
    }

    @Override
    @DeLog(ot = LogOT.MODIFY, st = LogST.DATA)
    public void saveBasicSetting(List<SettingItemVO> settingItemVOS) {
        CrestPermissionUtils.requireAdmin();
        sysParameterManage.saveBasic(settingItemVOS);
    }

    @Override
    public Integer RequestTimeOut() {
        int frontTimeOut = 60;
        List<SettingItemVO> settingItemVOS = queryBasicSetting();
        for (SettingItemVO settingItemVO : settingItemVOS) {
            if (StringUtils.isNotBlank(settingItemVO.getPkey()) && settingItemVO.getPkey().equalsIgnoreCase(SystemSettingConstants.Front_Time_Out) && StringUtils.isNotBlank(settingItemVO.getPval())) {
                frontTimeOut = Integer.parseInt(settingItemVO.getPval());
            }
        }
        return frontTimeOut;
    }

    @Override
    public Map<String, Object> defaultSettings() {
        Map<String, Object> map = new HashMap<>();
        map.put(SystemSettingConstants.DEFAULT_SORT, "1");

        List<SettingItemVO> settingItemVOS = queryBasicSetting();
        for (SettingItemVO settingItemVO : settingItemVOS) {
            if (StringUtils.isNotBlank(settingItemVO.getPkey()) && settingItemVO.getPkey().equalsIgnoreCase(SystemSettingConstants.DEFAULT_SORT) && StringUtils.isNotBlank(settingItemVO.getPval())) {
                map.put(SystemSettingConstants.DEFAULT_SORT, settingItemVO.getPval());
            }
            if (StringUtils.isNotBlank(settingItemVO.getPkey()) && settingItemVO.getPkey().equalsIgnoreCase(SystemSettingConstants.DEFAULT_OPEN) && StringUtils.isNotBlank(settingItemVO.getPval())) {
                map.put(SystemSettingConstants.DEFAULT_OPEN, settingItemVO.getPval());
            }
        }
        return map;
    }

    @Override
    public List<Object> ui() {
        return sysParameterManage.getUiList();
    }

    @Override
    public Integer defaultLogin() {
        return sysParameterManage.defaultLogin();
    }

    @Override
    public ShareBaseVO shareBase() {
        return sysParameterManage.shareBase();
    }

    @Override
    public Map<String, String> i18nOptions() {
        File dir = new File(StaticResourceConstants.I18N_DIR);
        File[] files = null;
        if (!dir.exists() || ObjectUtils.isEmpty(files = dir.listFiles())) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (File file : files) {
            String name = file.getName();
            int start = name.indexOf("custom_") + 7;
            int end = name.indexOf("front");
            String i18nName = name.substring(start, end - 1).replace("_", "-");
            String languageName = name.substring(end + 6, name.lastIndexOf("."));
            result.put(i18nName, languageName);
        }
        return result;
    }

    @Override
    public SQLBotConfigVO sqlBotConfig() {
        assertFullModeFeature();
        String key = "sqlbot.";
        List<CoreSysSetting> coreSysSettings = sysParameterManage.groupList(key);
        if (CollectionUtils.isNotEmpty(coreSysSettings)) {
            SQLBotConfigVO vo = new SQLBotConfigVO();
            coreSysSettings.forEach(sysSetting -> {
                if (sysSetting.getPkey().equalsIgnoreCase(key + "domain")) {
                    vo.setDomain(sysSetting.getPval());
                } else if (sysSetting.getPkey().equalsIgnoreCase(key + "id")) {
                    vo.setId(sysSetting.getPval());
                } else if (sysSetting.getPkey().equalsIgnoreCase(key + "enabled")) {
                    vo.setEnabled(StringUtils.isNotBlank(sysSetting.getPval()) && Strings.CI.equals(sysSetting.getPval(), "true"));
                } else if (sysSetting.getPkey().equalsIgnoreCase(key + "valid")) {
                    vo.setValid(StringUtils.isNotBlank(sysSetting.getPval()) && Strings.CI.equals(sysSetting.getPval(), "true"));
                }
            });
            return vo;
        }
        return null;
    }

    @Override
    @DeLog(ot = LogOT.MODIFY, st = LogST.DATA)
    public void saveSqlBotConfig(SQLBotConfigCreator creator) {
        assertFullModeFeature();
        sysParameterManage.saveSqlBotConfig(creator);
    }

    private void assertFullModeFeature() {
        if (internalLiteEnabled) {
            DEException.throwException("当前版本未启用该功能");
        }
    }
}
