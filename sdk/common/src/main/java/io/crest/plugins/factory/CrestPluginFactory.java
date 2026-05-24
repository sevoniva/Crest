package io.crest.plugins.factory;

import io.crest.plugins.template.CrestPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrestPluginFactory {
    private static final Map<String, CrestPlugin> PLUGINS = new ConcurrentHashMap<>();

    public static void loadTemplate(String moduleName, CrestPlugin plugin) {
        if (moduleName != null && plugin != null) {
            PLUGINS.put(moduleName, plugin);
        }
    }

    public static CrestPlugin getTemplate(String moduleName) {
        return PLUGINS.get(moduleName);
    }

    public static void unloadTemplate(String moduleName) {
        PLUGINS.remove(moduleName);
    }
}
