package io.crest.extensions.view.plugin;

import io.crest.exception.DEException;
import io.crest.extensions.view.factory.PluginsChartFactory;
import io.crest.extensions.view.vo.PluginViewVO;
import io.crest.plugins.template.CrestPlugin;
import io.crest.plugins.vo.CrestPluginVO;
import io.crest.utils.JsonUtil;

public abstract class CrestChartPlugin extends AbstractChartPlugin implements CrestPlugin {

    @Override
    public void loadPlugin() {
        PluginViewVO viewConfig = getConfig();
        PluginsChartFactory.loadPlugin(viewConfig.getRender(), viewConfig.getChartValue(), this);
    }

    public PluginViewVO getConfig() {
        CrestPluginVO pluginInfo = null;
        try {
            pluginInfo = getPluginInfo();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        String config = pluginInfo.getConfig();
        PluginViewVO vo = JsonUtil.parseObject(config, PluginViewVO.class);
        vo.setIcon(pluginInfo.getIcon());
        return vo;
    }
}
