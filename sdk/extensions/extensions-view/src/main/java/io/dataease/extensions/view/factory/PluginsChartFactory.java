package io.dataease.extensions.view.factory;

import io.dataease.exception.DEException;
import io.dataease.extensions.view.plugin.AbstractChartPlugin;
import io.dataease.extensions.view.plugin.DataEaseChartPlugin;
import io.dataease.extensions.view.vo.XpackPluginsViewVO;
import io.dataease.plugins.factory.DataEasePluginFactory;
import io.dataease.utils.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginsChartFactory {

    private static final Map<String, DataEaseChartPlugin> templateMap = new ConcurrentHashMap<>();


    public static AbstractChartPlugin getInstance(String render, String type) {
        String key = render + "_" + type;
        return templateMap.get(key);
    }

    public static void loadPlugin(String render, String type, DataEaseChartPlugin plugin) {
        String key = render + "_" + type;
        if (templateMap.containsKey(key)) return;
        templateMap.put(key, plugin);
        try {
            String moduleName = plugin.getPluginInfo().getModuleName();
            DataEasePluginFactory.loadTemplate(moduleName, plugin);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), new Throwable(e));
            DEException.throwException(e);
        }
    }

    public static List<XpackPluginsViewVO> getViewConfigList() {
        return templateMap.values().stream().map(DataEaseChartPlugin::getConfig).toList();
    }
}
