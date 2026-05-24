package io.crest.extensions.datasource.factory;

import io.crest.exception.DEException;
import io.crest.extensions.datasource.plugin.CrestDatasourcePlugin;
import io.crest.extensions.datasource.provider.Provider;
import io.crest.extensions.datasource.utils.SpringContextUtil;
import io.crest.extensions.datasource.vo.DatasourceConfiguration;
import io.crest.extensions.datasource.vo.PluginDatasourceVO;
import io.crest.plugins.factory.CrestPluginFactory;
import io.crest.utils.LogUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Junjun
 */
public class ProviderFactory {

    public static Provider getProvider(String type) throws DEException {
        if (type.equalsIgnoreCase("es")) {
            return SpringContextUtil.getApplicationContext().getBean("esProvider", Provider.class);
        }
        List<String> list = Arrays.stream(DatasourceConfiguration.DatasourceType.values()).map(DatasourceConfiguration.DatasourceType::getType).toList();
        if (list.contains(type)) {
            return SpringContextUtil.getApplicationContext().getBean("calciteProvider", Provider.class);
        }
        Provider instance = getInstance(type);
        if (instance == null) {
            DEException.throwException("插件异常，请检查插件");
        }
        return instance;
    }

    public static Provider getDefaultProvider() {
        return SpringContextUtil.getApplicationContext().getBean("calciteProvider", Provider.class);
    }


    private static final Map<String, CrestDatasourcePlugin> templateMap = new ConcurrentHashMap<>();

    public static Provider getInstance(String type) {
        String key = type;
        return templateMap.get(key);
    }

    public static void loadPlugin(String type, CrestDatasourcePlugin plugin) {
        String key = type;
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

    public static List<PluginDatasourceVO> getDsConfigList() {
        return templateMap.values().stream().map(CrestDatasourcePlugin::getConfig).toList();
    }
}
