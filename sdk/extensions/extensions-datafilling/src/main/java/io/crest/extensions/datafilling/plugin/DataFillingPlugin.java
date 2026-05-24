package io.crest.extensions.datafilling.plugin;

import io.crest.exception.DEException;
import io.crest.extensions.datafilling.factory.ExtDDLProviderFactory;
import io.crest.extensions.datafilling.provider.ExtDDLProvider;
import io.crest.extensions.datafilling.vo.PluginDataFillingVO;
import io.crest.plugins.template.CrestPlugin;
import io.crest.plugins.vo.CrestPluginVO;
import io.crest.utils.JsonUtil;

public abstract class DataFillingPlugin extends ExtDDLProvider implements CrestPlugin {

    @Override
    public void loadPlugin() {
        PluginDataFillingVO viewConfig = getConfig();
        ExtDDLProviderFactory.loadPlugin(viewConfig.getType(), this);
    }


    public PluginDataFillingVO getConfig() {
        CrestPluginVO pluginInfo = null;
        try {
            pluginInfo = getPluginInfo();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        String config = pluginInfo.getConfig();
        PluginDataFillingVO vo = JsonUtil.parseObject(config, PluginDataFillingVO.class);
        vo.setIcon(pluginInfo.getIcon());
        return vo;
    }

    @Override
    public void unloadPlugin() {

    }
}
