package io.crest.plugins.template;

import io.crest.plugins.vo.CrestPluginVO;

public interface CrestPlugin {
    CrestPluginVO getPluginInfo() throws Exception;

    void loadPlugin();

    default void unloadPlugin() {
    }
}
