package io.crest.extensions.view.factory;

import io.crest.exception.DEException;
import io.crest.extensions.view.plugin.AbstractChartPlugin;
import io.crest.extensions.view.plugin.CrestChartPlugin;
import io.crest.extensions.view.vo.PluginViewVO;
import io.crest.plugins.factory.CrestPluginFactory;
import io.crest.utils.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginsChartFactory {

    private static final Map<String, CrestChartPlugin> templateMap = new ConcurrentHashMap<>();


    public static AbstractChartPlugin getInstance(String render, String type) {
        String key = render + "_" + type;
        return templateMap.get(key);
    }

    public static void loadPlugin(String render, String type, CrestChartPlugin plugin) {
        String key = render + "_" + type;
        if (templateMap.containsKey(key)) return;
        templateMap.put(key, plugin);
        try {
            String moduleName = plugin.getPluginInfo().getModuleName();
            CrestPluginFactory.loadTemplate(moduleName, plugin);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), new Throwable(e));
            DEException.throwException(e);
        }
    }

    public static List<PluginViewVO> getViewConfigList() {
        return templateMap.values().stream().map(CrestChartPlugin::getConfig).toList();
    }
}
