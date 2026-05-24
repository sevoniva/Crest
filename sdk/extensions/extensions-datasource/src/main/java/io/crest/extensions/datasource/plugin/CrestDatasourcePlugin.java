package io.crest.extensions.datasource.plugin;

import io.crest.exception.DEException;
import io.crest.extensions.datasource.dto.DatasourceRequest;
import io.crest.extensions.datasource.factory.ProviderFactory;
import io.crest.extensions.datasource.provider.Provider;
import io.crest.extensions.datasource.vo.PluginDatasourceVO;
import io.crest.plugins.template.CrestPlugin;
import io.crest.plugins.vo.CrestPluginVO;
import io.crest.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author Junjun
 */
@SuppressWarnings("deprecation")
public abstract class CrestDatasourcePlugin extends Provider implements CrestPlugin {
    private final String DEFAULT_FILE_PATH = "/opt/crest/drivers/plugin";


    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) {
        return new ArrayList<>();
    }


    @Override
    public void loadPlugin() {
        PluginDatasourceVO datasourceConfig = getConfig();
        ProviderFactory.loadPlugin(datasourceConfig.getType(), this);
        try {
            loadDriver();
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    private void loadDriver() throws Exception {
        PluginDatasourceVO config = getConfig();
        String localPath = StringUtils.isEmpty(config.getDriverPath()) ? DEFAULT_FILE_PATH : config.getDriverPath();
        ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
        URI uri = protectionDomain.getCodeSource().getLocation().toURI();
        try (JarFile jarFile = new JarFile(new File(uri))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (Strings.CS.endsWith(name, ".jar")) {
                    File file = new File(localPath, Paths.get(name).getFileName().toString());
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    try (InputStream inputStream = jarFile.getInputStream(entry);
                         FileOutputStream outputStream = new FileOutputStream(file)) {
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = inputStream.read(bytes)) >= 0) {
                            outputStream.write(bytes, 0, length);
                        }
                    }
                }
            }
        }
    }

    public PluginDatasourceVO getConfig() {
        CrestPluginVO pluginInfo = null;
        try {
            pluginInfo = getPluginInfo();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        String config = pluginInfo.getConfig();
        PluginDatasourceVO vo = JsonUtil.parseObject(config, PluginDatasourceVO.class);
        vo.setIcon(pluginInfo.getIcon());
        return vo;
    }

    @Override
    public void unloadPlugin() {
        try {
            ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
            URI uri = protectionDomain.getCodeSource().getLocation().toURI();
            try (JarFile jarFile = new JarFile(new File(uri))) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (Strings.CS.endsWith(name, ".jar")) {
                        File file = new File(DEFAULT_FILE_PATH, Paths.get(name).getFileName().toString());
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }
}
