package io.dataease.plugins.factory;

import io.dataease.plugins.template.DataEasePlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataEasePluginFactory {
    private static final Map<String, DataEasePlugin> PLUGINS = new ConcurrentHashMap<>();

    public static void loadTemplate(String moduleName, DataEasePlugin plugin) {
        if (moduleName != null && plugin != null) {
            PLUGINS.put(moduleName, plugin);
        }
    }

    public static DataEasePlugin getTemplate(String moduleName) {
        return PLUGINS.get(moduleName);
    }

    public static void unloadTemplate(String moduleName) {
        PLUGINS.remove(moduleName);
    }
}
