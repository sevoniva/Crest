package io.dataease.plugins.template;

import io.dataease.plugins.vo.DataEasePluginVO;

public interface DataEasePlugin {
    DataEasePluginVO getPluginInfo() throws Exception;

    void loadPlugin();

    default void unloadPlugin() {
    }
}
